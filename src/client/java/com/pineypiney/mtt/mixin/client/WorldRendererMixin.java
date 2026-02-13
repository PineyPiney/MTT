package com.pineypiney.mtt.mixin.client;

import com.pineypiney.mtt.client.dnd.DNDClientEngine;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

	@Redirect(method = "pushEntityRenders", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderManager;render(Lnet/minecraft/client/render/entity/state/EntityRenderState;Lnet/minecraft/client/render/state/CameraRenderState;DDDLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;)V"))
	private <S extends EntityRenderState> void doNotRenderPlayer(EntityRenderManager instance, S renderState, CameraRenderState cameraState, double offsetX, double offsetY, double offsetZ, MatrixStack matrices, OrderedRenderCommandQueue queue) {
		var engine = DNDClientEngine.Companion.getInstance();
		if(engine.getRunning()){
			// Don't render the players while the engine is running
			if (renderState.entityType == EntityType.PLAYER) return;
		}
		instance.render(renderState, cameraState, offsetX, offsetY, offsetZ, matrices, queue);
	}
}
