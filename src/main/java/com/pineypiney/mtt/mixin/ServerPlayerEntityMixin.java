package com.pineypiney.mtt.mixin;

import com.mojang.authlib.GameProfile;
import com.pineypiney.mtt.dnd.characters.Character;
import com.pineypiney.mtt.mixin_interfaces.MTTServerPlayer;
import com.pineypiney.mtt.network.payloads.s2c.OpenDNDScreenS2CPayload;
import com.pineypiney.mtt.screen.DNDScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
abstract class ServerPlayerEntityMixin extends PlayerEntity implements MTTServerPlayer {

	@Shadow
	private int screenHandlerSyncId;

	public ServerPlayerEntityMixin(World world, GameProfile profile) {
		super(world, profile);
	}

	@Shadow
	protected abstract void incrementScreenHandlerSyncId();

	@Shadow
	protected abstract void onScreenHandlerOpened(ScreenHandler screenHandler);

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(MinecraftServer server, ServerWorld world, GameProfile profile, SyncedClientOptions clientOptions, CallbackInfo ci) {
	}

	@Override
	public void mTT$openCharacterInventory(Character character) {
		if (this.currentScreenHandler != this.playerScreenHandler) {
			this.closeHandledScreen();
		}

		this.incrementScreenHandlerSyncId();
		ServerPlayNetworking.send((ServerPlayerEntity) (Object) this, new OpenDNDScreenS2CPayload(this.screenHandlerSyncId, character.getUuid()));
		this.currentScreenHandler = new DNDScreenHandler(this.screenHandlerSyncId, character);
		this.onScreenHandlerOpened(this.currentScreenHandler);
	}
}
