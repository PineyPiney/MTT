package com.pineypiney.mtt.mixin;

import com.mojang.datafixers.DataFixer;
import com.pineypiney.mtt.dnd.DNDServerEngine;
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder;
import net.minecraft.network.QueryableServer;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.*;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.world.ChunkErrorHandler;
import net.minecraft.util.ApiServices;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin extends ReentrantThreadExecutor<ServerTask> implements QueryableServer, ChunkErrorHandler, CommandOutput, DNDEngineHolder<DNDServerEngine> {

	@Shadow private PlayerManager playerManager;

	// The engine has to be defined after all other fields have been set
	// because it uses the resource manager to load custom file types
	@Unique
	DNDServerEngine dndEngine;

	public MinecraftServerMixin(){
		super("Server");
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Proxy proxy, DataFixer dataFixer, ApiServices apiServices, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci){
		dndEngine = new DNDServerEngine((MinecraftServer)(Object)this);
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profilers;get()Lnet/minecraft/util/profiler/Profiler;"))
	private void tickDNDEngine(BooleanSupplier shouldKeepTicking, CallbackInfo ci){
        /*
        DNDEngineUpdateS2CPacket packet = new DNDEngineUpdateS2CPacket(dndEngine.getPlayers().size());
        for(ServerPlayerEntity player : playerManager.getPlayerList()){
            player.networkHandler.sendPacket(packet);
        }
         */
		dndEngine.tickServer((MinecraftServer)(Object) this);
	}

	@Override
	public DNDServerEngine getDNDEngine() {
		return dndEngine;
	}
}
