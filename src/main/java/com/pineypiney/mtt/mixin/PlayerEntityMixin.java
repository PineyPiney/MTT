package com.pineypiney.mtt.mixin;

import com.pineypiney.mtt.util.ExtensionsKt;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void updateDNDEntity(CallbackInfo ci){
		var engine = ExtensionsKt.getEngine(getWorld());
		var entity = engine.getPlayerEntity(getUuid());
		if(engine.getRunning() && entity != null) {
			entity.setHeadYaw(headYaw);
			entity.setBodyYaw(bodyYaw);
		}
	}
}
