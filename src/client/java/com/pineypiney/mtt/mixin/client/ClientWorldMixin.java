package com.pineypiney.mtt.mixin.client;

import com.pineypiney.mtt.client.dnd.ClientDNDEngine;
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientWorld.class)
public class ClientWorldMixin implements DNDEngineHolder<ClientDNDEngine> {
	@Shadow @Final private MinecraftClient client;

	@Override
	public ClientDNDEngine mtt$getDNDEngine() {
		return (ClientDNDEngine) ((DNDEngineHolder<?>) client).mtt$getDNDEngine();
	}
}
