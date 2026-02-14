package com.pineypiney.mtt.mixin.client;

import com.pineypiney.mtt.client.dnd.DNDClientEngine;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

	@Shadow @Final private static Identifier HOTBAR_TEXTURE;

	@Shadow @Final private static Identifier HOTBAR_SELECTION_TEXTURE;

	@Shadow @Final private static Identifier HOTBAR_OFFHAND_LEFT_TEXTURE;

	@Shadow @Final private static Identifier HOTBAR_OFFHAND_RIGHT_TEXTURE;

	@Shadow protected abstract void renderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed);

	@Shadow @Final private MinecraftClient client;

	@Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
	private void renderDNDHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci){
		var player = client.player;
		var character = DNDClientEngine.Companion.getRunningAndPlayerCharacter(player);
		if(player == null || character == null) return; // If this player doesn't have a character then render as normal

		Arm offhandArm = player.getMainArm().getOpposite();
		int i = context.getScaledWindowWidth() / 2;
		context.getMatrices().pushMatrix();
		context.getMatrices().translate(0.0F, 0.0F);
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HOTBAR_TEXTURE, i - 91, context.getScaledWindowHeight() - 22, 182, 22);
		context.drawGuiTexture(
				RenderPipelines.GUI_TEXTURED,
				HOTBAR_SELECTION_TEXTURE,
				i - 92 + character.getInventory().getSelectedSlot() * 20,
				context.getScaledWindowHeight() - 23,
				24,
				23
		);
		var offhandStack = character.getInventory().getOffhandSlotIcon();
		if (!offhandStack.isEmpty()) {
			if (offhandArm == Arm.LEFT)
				context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_TEXTURE, i - 91 - 29, context.getScaledWindowHeight() - 23, 29, 24);
			else
				context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_RIGHT_TEXTURE, i + 91, context.getScaledWindowHeight() - 23, 29, 24);
		}
		context.getMatrices().popMatrix();

		var stackY = context.getScaledWindowHeight() - 19;
		for(int j = 0; j < 4;){
			int n = i - 88 + j * 20;
			renderHotbarItem(context, n, stackY, tickCounter, player, character.getInventory().getHotbarSlotStack(j), ++j);
		}

		if(!offhandStack.isEmpty()){
			if (offhandArm == Arm.LEFT) renderHotbarItem(context, i - 117, stackY, tickCounter, player, offhandStack, 4);
			else renderHotbarItem(context, i + 101, stackY, tickCounter, player, offhandStack, 4);
		}

		ci.cancel();
	}

	@ModifyVariable(method = "tick()V", at = @At("STORE"), ordinal = 0)
	private ItemStack getHeldStack(ItemStack playerStack){
		var character = DNDClientEngine.Companion.getRunningAndPlayerCharacter(client.player);
		return character == null ? playerStack : character.getInventory().getHeldStack();
	}
}
