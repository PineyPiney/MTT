package com.pineypiney.mtt.mixin.client;

import com.pineypiney.mtt.client.dnd.DNDClientEngine;
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientWorld.class)
public class ClientWorldMixin implements DNDEngineHolder<DNDClientEngine> {
	@Shadow @Final private MinecraftClient client;

	@Override
	public DNDClientEngine mtt$getDNDEngine() {
		return (DNDClientEngine)((DNDEngineHolder<?>)client).mtt$getDNDEngine();
	}
}
