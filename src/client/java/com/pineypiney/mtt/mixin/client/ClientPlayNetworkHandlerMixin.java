package com.pineypiney.mtt.mixin.client;

import com.pineypiney.mtt.client.dnd.network.ClientDNDEntity;
import com.pineypiney.mtt.entity.MTTEntities;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayNetworkHandler.class)
abstract public class ClientPlayNetworkHandlerMixin extends ClientCommonNetworkHandler {

	@Shadow
	private ClientWorld world;

	protected ClientPlayNetworkHandlerMixin(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
		super(client, connection, connectionState);
	}

	@Inject(method = "createEntity", at = @At("HEAD"), cancellable = true)
	private void createDNDEntity(EntitySpawnS2CPacket packet, CallbackInfoReturnable<Entity> cir) {
		if (packet.getEntityType() == MTTEntities.Companion.getDND_ENTITY()) {
			cir.setReturnValue(new ClientDNDEntity(world));
			cir.cancel();
		}
	}
}
