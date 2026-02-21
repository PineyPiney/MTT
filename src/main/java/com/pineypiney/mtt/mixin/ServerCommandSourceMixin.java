package com.pineypiney.mtt.mixin;

import com.pineypiney.mtt.dnd.DNDEngine;
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder;
import com.pineypiney.mtt.mixin_interfaces.MTTCommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerCommandSource.class)
public abstract class ServerCommandSourceMixin implements MTTCommandSource {

	@Shadow
	@Final
	private MinecraftServer server;

	@Shadow
	public abstract @Nullable ServerPlayerEntity getPlayer();

	@Override
	public DNDEngine mTT$getEngine() {
		return ((DNDEngineHolder<?>) server).mtt$getDNDEngine();
	}

	@Override
	public PlayerEntity mTT$getPlayer() {
		return getPlayer();
	}

	@Override
	public @Nullable Vec3d mTT$getPosition() {
		return getPlayer() == null ? null : getPlayer().getEntityPos();
	}
}
