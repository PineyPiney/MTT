package com.pineypiney.mtt.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.pineypiney.mtt.entity.DNDEntity;
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

	@Shadow public ServerPlayerEntity player;

	@Inject(method = "onPlayerMove", at = @At("TAIL"))
	private void updateDNDCharacter(PlayerMoveC2SPacket packet, CallbackInfo ci){
		var engine = ((DNDEngineHolder<?>) this.player.server).mtt$getDNDEngine();
		if(!engine.getRunning()) return;
		var character = engine.getPlayerCharacter(player.getUuid());
		if(character == null) return;
		var entity = engine.getPlayerCharacterEntity(character.getUuid());
		if(entity != null) {
			//entity.updatePositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
			character.setPos(player.getPos());
		}
	}

	@Inject(method = "onPlayerInteractEntity", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/Entity;getBoundingBox()Lnet/minecraft/util/math/Box;"), cancellable = true)
	private void onPlayerInteractDNDEntity(PlayerInteractEntityC2SPacket packet, CallbackInfo ci, @Local Entity entity, @Local Box box) {
		var engine = ((DNDEngineHolder<?>) player.server).mtt$getDNDEngine();
		if (!engine.getRunning()) return;

		var character = engine.getPlayerCharacter(player.getUuid());
		if (character == null) return;

		if (entity instanceof DNDEntity dndEntity) ci.cancel();
		else return;

		double d = 2.0 - player.getEntityInteractionRange();
		if (player.canInteractWithEntityIn(box, d) && dndEntity.getCharacter() != null) {
			character.attack(dndEntity.getCharacter());
		}
	}
}
