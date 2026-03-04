package com.pineypiney.mtt.mixin;

import com.pineypiney.mtt.dnd.DNDEngine;
import com.pineypiney.mtt.dnd.characters.Character;
import com.pineypiney.mtt.util.ExtensionsKt;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.entity.EntityLike;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerChunkLoadingManager.class)
public class ServerChunkLoadingManagerMixin {

	@Redirect(method = "updateWatchedSection", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/ChunkSectionPos;from(Lnet/minecraft/world/entity/EntityLike;)Lnet/minecraft/util/math/ChunkSectionPos;"))
	private ChunkSectionPos updateDndWatchedSection(EntityLike entity) {
		return getChunkPos(entity);
	}

	@Redirect(method = "updatePosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/ChunkSectionPos;from(Lnet/minecraft/world/entity/EntityLike;)Lnet/minecraft/util/math/ChunkSectionPos;"))
	private ChunkSectionPos updateDndPosition(EntityLike entity) {
		return getChunkPos(entity);
	}

	@Redirect(method = "handlePlayerAddedOrRemoved", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/ChunkSectionPos;from(Lnet/minecraft/world/entity/EntityLike;)Lnet/minecraft/util/math/ChunkSectionPos;"))
	private ChunkSectionPos handleDndPlayerAddedOrRemoved(EntityLike entity) {
		return getChunkPos(entity);
	}

	@Redirect(method = "sendWatchPackets(Lnet/minecraft/server/network/ServerPlayerEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getChunkPos()Lnet/minecraft/util/math/ChunkPos;"))
	private ChunkPos sendDndWatchPackets(ServerPlayerEntity player) {
		Character character = getCharacter(player);
		return character == null ? player.getChunkPos() : new ChunkPos(BlockPos.ofFloored(character.getPos()));
	}

	@Unique
	private ChunkSectionPos getChunkPos(EntityLike entity) {
		Character controlee = getCharacter(entity);
		return ChunkSectionPos.from(controlee != null ? controlee.getPos() : entity.getBlockPos().toBottomCenterPos());
	}

	@Unique
	@Nullable
	private Character getCharacter(EntityLike entity) {
		ServerPlayerEntity player = (ServerPlayerEntity) entity;
		DNDEngine<?> engine = ExtensionsKt.getEngine(player.getEntityWorld());
		if (!engine.getRunning()) return null;
		return engine.getCharacterFromPlayer(player.getUuid());
	}
}
