package com.pineypiney.mtt.mixin;

import com.pineypiney.mtt.util.ExtensionsKt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

	@Shadow public abstract float getPitch();

	@Shadow public abstract float getYaw();

	@Inject(method = "changeLookDirection", at = @At("TAIL"))
	private void updatePlayerCharactersEntity(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci){
		if(((Object)this) instanceof PlayerEntity player){
			var engine = ExtensionsKt.getEngine(player.getEntityWorld());
			var entity = engine.getEntityFromPlayer(player.getUuid());
			if(engine.getRunning() && entity != null){
//				entity.setPitch(this.getPitch());
				//entity.setYaw(this.getYaw());
			}
		}
	}

//	@Inject(method = "setPos", at = @At("TAIL"))
//	private void updateCharacterEntity(double x, double y, double z, CallbackInfo ci){
//		if(((Object)this) instanceof PlayerEntity player){
//			var engine = ExtensionsKt.getEngine(player.getEntityWorld());
//			var entity = engine.getPlayerEntity(player.getUuid());
//			if(engine.getRunning() && entity != null){
//				entity.setPos(x, y, z);
//			}
//		}
//	}

//	@Inject(method = "setLastPosition", at = @At("TAIL"))
//	private void updateCharacterEntity(Vec3d pos, CallbackInfo ci){
//		if(((Object)this) instanceof PlayerEntity player){
//			var engine = ExtensionsKt.getEngine(player.getEntityWorld());
//			var entity = engine.getPlayerEntity(player.getUuid());
//			if(engine.getRunning() && entity != null){
//				entity.lastX = entity.lastRenderX = pos.x;
//				entity.lastY = entity.lastRenderY = pos.y;
//				entity.lastZ = entity.lastRenderZ = pos.z;
//			}
//		}
//	}
}
