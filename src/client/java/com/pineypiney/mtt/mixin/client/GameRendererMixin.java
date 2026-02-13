package com.pineypiney.mtt.mixin.client;

import com.pineypiney.mtt.client.dnd.network.ClientDNDEntity;
import net.minecraft.client.MinecraftClient;
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

	/*
	@Unique
	private static EntityHitResult raycast(World world, Character player, Vec3d min, Vec3d max, Box box, double maxDistance) {
		double d = maxDistance;
		Entity entity2 = null;
		Vec3d vec3d = null;

		for (Entity entity3 : world.getOtherEntities(entity, box, predicate)) {
			Box box2 = entity3.getBoundingBox().expand(entity3.getTargetingMargin());
			Optional<Vec3d> optional = box2.raycast(min, max);
			if (box2.contains(min)) {
				if (d >= 0.0) {
					entity2 = entity3;
					vec3d = optional.orElse(min);
					d = 0.0;
				}
			} else if (optional.isPresent()) {
				Vec3d vec3d2 = optional.get();
				double e = min.squaredDistanceTo(vec3d2);
				if (e < d || d == 0.0) {
					if (entity3.getRootVehicle() == entity.getRootVehicle()) {
						if (d == 0.0) {
							entity2 = entity3;
							vec3d = vec3d2;
						}
					} else {
						entity2 = entity3;
						vec3d = vec3d2;
						d = e;
					}
				}
			}
		}

		return entity2 == null ? null : new EntityHitResult(entity2, vec3d);
	}

	@Unique
	private static List<Entity> getOtherEntities(World world, @Nullable Entity except, Box box, Predicate<? super Entity> predicate) {
		Profilers.get().visit("getEntities");
		List<Entity> list = Lists.<Entity>newArrayList();
		world.getEntitiesByType(MTTEntities.Companion.getPLAYER(), box)
		world.getEntityLookup().forEachIntersects(box, entity -> {
			if (entity != except && predicate.test(entity)) {
				list.add(entity);
			}
		});

		return list;
	}

	 */
}
