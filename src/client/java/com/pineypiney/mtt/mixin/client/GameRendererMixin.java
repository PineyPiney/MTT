package com.pineypiney.mtt.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.pineypiney.mtt.client.dnd.network.ClientDNDEntity;
import com.pineypiney.mtt.client.dnd.spell_selector.SpellSelector;
import com.pineypiney.mtt.mixin_interfaces.DNDClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerLikeState;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	private float lastFovMultiplier;

	@Shadow
	private float fovMultiplier;

	@Inject(method = "updateFovMultiplier", at = @At("HEAD"), cancellable = true)
	private void updateDNDEntityFovMultiplier(CallbackInfo ci) {
		if (client.getCameraEntity() instanceof ClientDNDEntity entity) {
			ci.cancel();
			GameOptions gameOptions = this.client.options;
			boolean bl = gameOptions.getPerspective().isFirstPerson();
			float f = gameOptions.getFovEffectScale().getValue().floatValue();
			float g = entity.getFovMultiplier(bl, f);

			this.lastFovMultiplier = this.fovMultiplier;
			this.fovMultiplier = this.fovMultiplier + (g - this.fovMultiplier) * 0.5F;
			this.fovMultiplier = MathHelper.clamp(this.fovMultiplier, 0.1F, 1.5F);
		}
	}

//	@Inject(method = "getFov", at = @At("TAIL"), cancellable = true)
//	private void updateDNDEntityFovMultiplier(Camera camera, float tickProgress, boolean changingFov, CallbackInfoReturnable<Float> cir) {
//		if(client.getCameraEntity() instanceof ClientDNDEntity entity) {
//			cir.cancel();
//			GameOptions gameOptions = this.client.options;
//			boolean bl = gameOptions.getPerspective().isFirstPerson();
//			float f = gameOptions.getFovEffectScale().getValue().floatValue();
//			float g = entity.getFovMultiplier(bl, f);
//
//			this.lastFovMultiplier = this.fovMultiplier;
//			this.fovMultiplier = this.fovMultiplier + (g - this.fovMultiplier) * 0.5F;
//			this.fovMultiplier = MathHelper.clamp(this.fovMultiplier, 0.1F, 1.5F);
//		}
//	}

	@Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
	private void getDNDEntityState(MatrixStack matrices, float tickProgress, CallbackInfo ci) {
		if (client.getCameraEntity() instanceof ClientDNDEntity entity) {
			ci.cancel();
			ClientPlayerLikeState clientPlayerLikeState = entity.getState();
			float f = clientPlayerLikeState.getReverseLerpedDistanceMoved(tickProgress);
			float g = clientPlayerLikeState.lerpMovement(tickProgress);
			matrices.translate(MathHelper.sin(f * (float) Math.PI) * g * 0.5F, -Math.abs(MathHelper.cos(f * (float) Math.PI) * g), 0.0F);
			matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin(f * (float) Math.PI) * g * 3.0F));
			matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(Math.abs(MathHelper.cos(f * (float) Math.PI - 0.2F) * g) * 5.0F));
		}
	}

	@Inject(method = "updateCrosshairTarget", at = @At("TAIL"))
	private void updateCrosshairTarget(CallbackInfo ci, @Local(argsOnly = true) float tickProgress) {
		DNDClient dndClient = ((DNDClient) client);
		SpellSelector selector = dndClient.mTT$getSpellSelector();
		double range = selector != null ? selector.getSpell().getSettings().getRange() : 60.0;
		if (client.getCameraEntity() != null)
			dndClient.mTT$setDndCrosshairTarget(ClientPlayerEntity.getCrosshairTarget(client.getCameraEntity(), range, range, tickProgress));
		if (selector != null) selector.update();
	}
}
