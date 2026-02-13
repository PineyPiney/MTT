package com.pineypiney.mtt.mixin.client;

import com.google.common.base.MoreObjects;
import com.llamalad7.mixinextras.sugar.Local;
import com.pineypiney.mtt.client.dnd.DNDClientEngine;
import com.pineypiney.mtt.client.render.entity.DNDPlayerEntityRenderer;
import com.pineypiney.mtt.mixin_interfaces.CharacterController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

	@Shadow @Final private MinecraftClient client;
	@Shadow
	@Final
	private EntityRenderManager entityRenderDispatcher;

	@Shadow
	@Final
	private ItemModelManager itemModelManager;

	@Shadow
	private ItemStack mainHand;

	@Shadow
	private float lastEquipProgressMainHand;
	@Shadow
	private float equipProgressMainHand;
	@Shadow
	private ItemStack offHand;
	@Shadow
	private float lastEquipProgressOffHand;
	@Shadow
	private float equipProgressOffHand;

	@Shadow
	protected abstract void renderFirstPersonItem(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, int light);

	@ModifyArgs(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F", ordinal = 0))
	private void characterPitch(Args args) {
		var character = DNDClientEngine.Companion.getRunningAndPlayerCharacterEntity(client.player);
		if (character == null) return;
		CharacterController entity = (CharacterController) client.player;
		if (entity == null) return;
		args.set(1, entity.mTT$getLastPitch());
		args.set(2, entity.mTT$getPitch());
	}

	@ModifyArgs(
			method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F", ordinal = 1)
	)
	private void characterYaw(Args args) {
		var character = DNDClientEngine.Companion.getRunningAndPlayerCharacterEntity(client.player);
		if (character == null) return;
		CharacterController entity = (CharacterController) client.player;
		if (entity == null) return;
		args.set(1, entity.mTT$getLastYaw());
		args.set(2, entity.mTT$getYaw());
	}

	@Inject(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At("HEAD"), cancellable = true)
	private void renderItem(float tickProgress, MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, ClientPlayerEntity player, int light, CallbackInfo ci) {
		var entity = DNDClientEngine.Companion.getRunningAndPlayerCharacterEntity(client.player);
		if (entity == null) return;
		CharacterController characterPlayer = (CharacterController) client.player;
		if (characterPlayer == null) return;

		ci.cancel();

		float f = player.getHandSwingProgress(tickProgress);
		Hand hand = MoreObjects.firstNonNull(player.preferredHand, Hand.MAIN_HAND);
		float g = entity.getLerpedPitch(tickProgress);
		HeldItemRenderer.HandRenderType handRenderType = HeldItemRenderer.getHandRenderType(player);
		float h = MathHelper.lerp(tickProgress, characterPlayer.mTT$getLastPitch(), characterPlayer.mTT$getPitch());
		float i = MathHelper.lerp(tickProgress, characterPlayer.mTT$getLastYaw(), characterPlayer.mTT$getYaw());
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((entity.getPitch(tickProgress) - h) * 0.1F));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((entity.getYaw(tickProgress) - i) * 0.1F));
		if (handRenderType.renderMainHand) {
			float j = hand == Hand.MAIN_HAND ? f : 0.0F;
			float k = this.itemModelManager.getSwapAnimationScale(this.mainHand)
					* (1.0F - MathHelper.lerp(tickProgress, this.lastEquipProgressMainHand, this.equipProgressMainHand));
			this.renderFirstPersonItem(player, tickProgress, g, Hand.MAIN_HAND, j, this.mainHand, k, matrices, orderedRenderCommandQueue, light);
		}

		if (handRenderType.renderOffHand) {
			float j = hand == Hand.OFF_HAND ? f : 0.0F;
			float k = this.itemModelManager.getSwapAnimationScale(this.offHand)
					* (1.0F - MathHelper.lerp(tickProgress, this.lastEquipProgressOffHand, this.equipProgressOffHand));
			this.renderFirstPersonItem(player, tickProgress, g, Hand.OFF_HAND, j, this.offHand, k, matrices, orderedRenderCommandQueue, light);
		}

		this.client.gameRenderer.getEntityRenderDispatcher().render();
		this.client.getBufferBuilders().getEntityVertexConsumers().draw();
	}

	@Inject(method = "renderArmHoldingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderManager;getPlayerRenderer(Lnet/minecraft/client/network/AbstractClientPlayerEntity;)Lnet/minecraft/client/render/entity/PlayerEntityRenderer;"), cancellable = true)
	private void renderCharacterArm(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, float equipProgress, float swingProgress, Arm arm, CallbackInfo ci) {
		var player = client.player;
		var characterEntity = DNDClientEngine.Companion.getRunningAndPlayerCharacterEntity(player);
		if (player == null || characterEntity == null || characterEntity.getCharacter() == null) return;

		var renderer = (DNDPlayerEntityRenderer) this.entityRenderDispatcher.getRenderer(characterEntity);
		renderer.renderArm(matrices, queue, light, arm, characterEntity.getCharacter());
		ci.cancel();
	}

	@ModifyVariable(method = "renderFirstPersonItem", at = @At(value = "HEAD"), argsOnly = true)
	public ItemStack renderCharacterFirstPersonItem(ItemStack value, @Local(argsOnly = true) Hand hand) {
		var engine = DNDClientEngine.Companion.getInstance();
		if (!engine.getRunning() || client.player == null) return value;
		var character = engine.getCharacterFromPlayer(client.player.getUuid());
		return character == null ? value : hand == Hand.MAIN_HAND ? character.getInventory().getHeldStack() : character.getInventory().getOffhandSlotStack();
	}
}
