package com.pineypiney.mtt.mixin.client;

import com.pineypiney.mtt.client.dnd.DNDClientEngine;
import com.pineypiney.mtt.client.render.MTTRenderers;
import com.pineypiney.mtt.dnd.characters.SheetCharacter;
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder;
import com.pineypiney.mtt.network.payloads.c2s.OpenDNDScreenC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.packet.CustomPayload;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin implements DNDEngineHolder<DNDClientEngine> {
	@Shadow @Nullable public ClientPlayerEntity player;

	@Shadow public abstract void setScreen(@Nullable Screen screen);

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(RunArgs args, CallbackInfo ci) {
		MTTRenderers.Companion.registerBipedModels();
		MTTRenderers.Companion.registerEquipmentModels();
	}

	@Debug(export = true)
	@Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V", ordinal = 1))
	private void openDNDInventory(MinecraftClient instance, Screen screen){
		SheetCharacter character = getDNDCharacter();
		if(!dndEngine.getRunning() || character == null) setScreen(screen);
		else if (player != null) {
			CustomPayload payload = new OpenDNDScreenC2SPayload(0);
			ClientPlayNetworking.send(payload);
		}
	}
	@Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;setSelectedSlot(I)V"))
	private void setDNDInventorySlot(PlayerInventory instance, int slot){
		var character = DNDClientEngine.Companion.getRunningAndPlayerCharacter(instance.player);
		if(character == null) instance.setSelectedSlot(slot);
		else if(slot < character.getInventory().getHotbarSize()) character.getInventory().setSelectedSlot(slot);
	}

	@Unique
	DNDClientEngine dndEngine = new DNDClientEngine((MinecraftClient)(Object)this);

	@Override
	public DNDClientEngine mtt$getDNDEngine() {
		return dndEngine;
	}

	@Unique
	@Nullable
	public SheetCharacter getDNDCharacter(){
		ClientPlayerEntity player = this.player;
		if(player == null) return null;
		return dndEngine.getCharacterFromPlayer(player.getUuid());
	}
}