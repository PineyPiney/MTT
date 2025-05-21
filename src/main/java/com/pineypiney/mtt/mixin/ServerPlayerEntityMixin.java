package com.pineypiney.mtt.mixin;

import com.mojang.authlib.GameProfile;
import com.pineypiney.mtt.dnd.DNDServerEngine;
import com.pineypiney.mtt.dnd.species.Species;
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder;
import com.pineypiney.mtt.network.payloads.s2c.SpeciesS2CPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(MinecraftServer server, ServerWorld world, GameProfile profile, SyncedClientOptions clientOptions, CallbackInfo ci){
			}
}
