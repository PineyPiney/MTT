package com.pineypiney.mtt.mixin;

import com.pineypiney.mtt.dnd.server.ServerDNDEngine;
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerWorld.class)
public class ServerWorldMixin implements DNDEngineHolder<ServerDNDEngine> {


	@Shadow @Final private MinecraftServer server;

	@Override
	public ServerDNDEngine mtt$getDNDEngine() {
		return (ServerDNDEngine) ((DNDEngineHolder<?>) server).mtt$getDNDEngine();
	}
}
