package com.pineypiney.mtt.mixin.client;

import com.pineypiney.mtt.dnd.DNDClientEngine;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldRenderer.class)
public abstract class PlayerEntityRendererMixin {

	@Shadow protected abstract void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers);

	@Redirect(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderEntity(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V"))
	private void dontRenderPlayer(WorldRenderer instance, Entity entity, double cameraX, double cameraY, double cameraZ, float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers){
		if(entity instanceof ClientPlayerEntity) {
			var engine = DNDClientEngine.Companion.getInstance();
			// Don't render the players while the engine is running
			if(engine.getRunning()) return;
		}
		renderEntity(entity, cameraX, cameraY, cameraZ, tickProgress, matrices, vertexConsumers);
	}
}
