package com.pineypiney.mtt.mixin;

import com.pineypiney.mtt.dnd.server.DNDServerEngine;
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
		if (player.getEntityWorld().getServer() == null) return;
		var engine = ((DNDEngineHolder<?>) player.getEntityWorld().getServer()).mtt$getDNDEngine();
		if(engine instanceof DNDServerEngine) ((DNDServerEngine) engine).onPlayerConnect(player);
	}

	@Inject(method = "remove", at = @At("TAIL"))
	public void onDisconnect(ServerPlayerEntity player, CallbackInfo ci) {
		if (player.getEntityWorld().getServer() == null) return;
		var engine = ((DNDEngineHolder<?>) player.getEntityWorld().getServer()).mtt$getDNDEngine();
		if (engine instanceof DNDServerEngine) ((DNDServerEngine) engine).onPlayerDisconnect(player);
	}
}
