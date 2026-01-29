package com.pineypiney.mtt.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.pineypiney.mtt.dnd.DNDClientEngine;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.Scroller;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mouse.class)
public class MouseMixin {

	@Shadow @Final private MinecraftClient client;

	@Redirect(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;setSelectedSlot(I)V"))
	private void scrollDNDHotbar(PlayerInventory instance, int slot, @Local(ordinal = 0) int i){
		var character = DNDClientEngine.Companion.getRunningAndPlayerCharacter(client.player);
		if(character == null) instance.setSelectedSlot(slot);
		else character.getInventory().setSelectedSlot(Scroller.scrollCycling(i, character.getInventory().getSelectedSlot(), character.getInventory().getHotbarSize()));
	}
}
