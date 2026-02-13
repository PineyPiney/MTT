package com.pineypiney.mtt.mixin;

import com.pineypiney.mtt.dnd.DNDEngine;
import com.pineypiney.mtt.dnd.characters.Character;
import com.pineypiney.mtt.entity.DNDEntity;
import com.pineypiney.mtt.util.ExtensionsKt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.world.ServerChunkLoadingManager;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;
import java.util.stream.Collectors;

@Mixin(targets = "net.minecraft.server.world.ServerChunkLoadingManager$EntityTracker")
public abstract class EntityTrackerMixin {

	@Shadow
	@Final
	Entity entity;

//	@Shadow
//	public abstract void stopTracking(ServerPlayerEntity player);

	@Redirect(method = "sendToListeners", at = @At(value = "FIELD", target = "Lnet/minecraft/server/world/ServerChunkLoadingManager$EntityTracker;listeners:Ljava/util/Set;", opcode = Opcodes.GETFIELD))
	private Set<PlayerAssociatedNetworkHandler> getListeners(ServerChunkLoadingManager.EntityTracker instance) {
		if (entity instanceof DNDEntity dndEntity) {
			Character character = dndEntity.getCharacter();
			if (character == null) return instance.listeners;
			DNDEngine engine = ExtensionsKt.getEngine(dndEntity.getEntityWorld());
			PlayerEntity player = engine.getControllingPlayer(character.getUuid());
			return instance.listeners.stream().filter(l -> l.getPlayer() != player).collect(Collectors.toSet());
		} else return instance.listeners;
	}

//	@Inject(method = "updateTrackedStatus(Lnet/minecraft/server/network/ServerPlayerEntity;)V", at = @At("HEAD"), cancellable = true)
//	private void doNotTrackControllingDNDEntity(ServerPlayerEntity player, CallbackInfo ci){
//		DNDEngine engine = ExtensionsKt.getEngine(player.getEntityWorld());
//		if(engine.getRunning() && engine.getEntityFromPlayer(player.getUuid()) == entity) {
//			stopTracking(player);
//			ci.cancel();
//		}
//	}
}
