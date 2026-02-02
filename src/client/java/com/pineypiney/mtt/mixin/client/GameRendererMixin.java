package com.pineypiney.mtt.mixin.client;

import com.pineypiney.mtt.dnd.DNDClientEngine;
import com.pineypiney.mtt.entity.DNDEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

	@Redirect(method = "findCrosshairTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"))
	private @Nullable EntityHitResult raycastDNDEntities(Entity entity, Vec3d min, Vec3d max, Box box, Predicate<Entity> predicate, double maxDistance) {
		var engine = DNDClientEngine.Companion.getInstance();
		if (engine.getRunning() && entity instanceof PlayerEntity player) {
			var character = engine.getPlayerCharacter(player.getUuid());
			if (character != null) {
				return ProjectileUtil.raycast(entity, min, max, box, e -> e instanceof DNDEntity dndEntity && dndEntity.canBeHit(character), maxDistance);
			}
		}
		return ProjectileUtil.raycast(entity, min, max, box, predicate, maxDistance);
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
