package com.pineypiney.mtt.mixin;

import com.pineypiney.mtt.dnd.DNDServerEngine;
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerWorld.class)
public class ServerWorldMixin implements DNDEngineHolder<DNDServerEngine> {


	@Shadow @Final private MinecraftServer server;

	@Override
	public DNDServerEngine mtt$getDNDEngine() {
		return (DNDServerEngine)((DNDEngineHolder<?>)server).mtt$getDNDEngine();
	}
}
