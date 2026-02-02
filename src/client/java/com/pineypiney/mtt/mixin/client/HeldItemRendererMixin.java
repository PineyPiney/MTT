package com.pineypiney.mtt.mixin.client;

import com.pineypiney.mtt.dnd.DNDClientEngine;
import com.pineypiney.mtt.render.entity.DNDPlayerEntityRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

	@Shadow @Final private MinecraftClient client;
	@Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;

	@Shadow protected abstract void renderFirstPersonItem(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);

	@Inject(method = "renderArmHoldingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;getRenderer(Lnet/minecraft/entity/Entity;)Lnet/minecraft/client/render/entity/EntityRenderer;"), cancellable = true)
	private void renderCharacterArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm, CallbackInfo ci){
		var player = client.player;
		var characterEntity = DNDClientEngine.Companion.getRunningAndPlayerCharacterEntity(player);
		if (player == null || characterEntity == null || characterEntity.getCharacter() == null) return;

		var renderer = (DNDPlayerEntityRenderer) this.entityRenderDispatcher.getRenderer(characterEntity);
		renderer.renderArm(matrices, vertexConsumers, light, arm, characterEntity.getCharacter());
		ci.cancel();
	}

	@Redirect(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
	public void renderCharacterFirstPersonItem(HeldItemRenderer instance, AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light){
		var character = DNDClientEngine.Companion.getRunningAndPlayerCharacter(player);
		if(character == null) renderFirstPersonItem(player, tickProgress, pitch, hand, swingProgress, item, equipProgress, matrices, vertexConsumers, light);

		else {
			var characterItem = hand == Hand.MAIN_HAND ? character.getInventory().getHeldStack() : character.getInventory().getOffhandSlotStack();
			renderFirstPersonItem(player, tickProgress, pitch, hand, swingProgress, characterItem, equipProgress, matrices, vertexConsumers, light);
		}
	}
}
