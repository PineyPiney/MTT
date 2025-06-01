package com.pineypiney.mtt.mixin;

import com.pineypiney.mtt.dnd.DNDServerEngine;
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

	@Inject(method = "onPlayerConnect", at = @At("TAIL"))
	public void sendDNDPayloads(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci){
		var engine = ((DNDEngineHolder<?>) player.server).mtt$getDNDEngine();
		if(engine instanceof DNDServerEngine) ((DNDServerEngine) engine).onPlayerConnect(player);
	}
}
