package com.pineypiney.mtt.mixin;

import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
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
		var entity = engine.getCharacterEntity(character.getUuid());
		if(entity != null) {
			//entity.updatePositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
			character.setPos(player.getPos());
		}
	}
}
