package com.pineypiney.mtt.mixin.client;

import com.pineypiney.mtt.mixin_interfaces.VertexConsumerProviderGetter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DrawContext.class)
public class DrawContextMixin implements VertexConsumerProviderGetter {

	@Shadow @Final private VertexConsumerProvider.Immediate vertexConsumers;

	@Override
	public VertexConsumerProvider getProvider() {
		return vertexConsumers;
	}
}
