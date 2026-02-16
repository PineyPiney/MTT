package com.pineypiney.mtt.mixin.client;

import com.pineypiney.mtt.client.dnd.ClientDNDEngine;
import com.pineypiney.mtt.network.payloads.c2s.UpdateSelectedDNDSlotC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

	@Unique
	private int lastSelectedDNDSlot;

	@Inject(method = "syncSelectedSlot", at = @At("RETURN"))
	private void syncSelectedDNDSlot(CallbackInfo ci) {
		var character = ClientDNDEngine.Companion.getClientCharacter();
		if (character != null) {
			int slot = character.getInventory().getSelectedSlot();
			if (slot != lastSelectedDNDSlot) {
				lastSelectedDNDSlot = slot;
				ClientPlayNetworking.send(new UpdateSelectedDNDSlotC2SPayload(slot));
			}
		}
	}
}
