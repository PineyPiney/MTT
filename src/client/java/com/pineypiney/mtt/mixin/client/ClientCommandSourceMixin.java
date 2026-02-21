package com.pineypiney.mtt.mixin.client;

import com.pineypiney.mtt.dnd.DNDEngine;
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder;
import com.pineypiney.mtt.mixin_interfaces.MTTCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientCommandSource.class)
public abstract class ClientCommandSourceMixin implements MTTCommandSource {

	@Shadow
	@Final
	private MinecraftClient client;

	@Override
	public DNDEngine mTT$getEngine() {
		return ((DNDEngineHolder<?>) client).mtt$getDNDEngine();
	}

	@Override
	public PlayerEntity mTT$getPlayer() {
		return client.player;
	}

	@Override
	public @Nullable Vec3d mTT$getPosition() {
		return client.player == null ? null : client.player.getEntityPos();
	}
}
