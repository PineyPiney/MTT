package com.pineypiney.mtt.client.render.entity

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.client.render.entity.shapes.*
import com.pineypiney.mtt.client.render.entity.state.ShapeEntityRenderState
import com.pineypiney.mtt.dnd.spells.ShapeType
import com.pineypiney.mtt.dnd.spells.SpellShape
import com.pineypiney.mtt.entity.ShapeEntity
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderSetup
import net.minecraft.client.render.command.OrderedRenderCommandQueue
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.state.CameraRenderState
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier

class ShapeEntityRenderer(ctx: EntityRendererFactory.Context) :
	EntityRenderer<ShapeEntity, ShapeEntityRenderState>(ctx) {

	override fun createRenderState(): ShapeEntityRenderState {
		return ShapeEntityRenderState()
	}

	override fun updateRenderState(entity: ShapeEntity, state: ShapeEntityRenderState, tickProgress: Float) {
		super.updateRenderState(entity, state, tickProgress)
		state.shape = entity.shape
		state.colour = entity.colour
	}

	override fun render(
		renderState: ShapeEntityRenderState,
		matrices: MatrixStack,
		queue: OrderedRenderCommandQueue,
		cameraState: CameraRenderState
	) {
		if (renderState.shape == SpellShape.Single) return
		val renderer = prepareRenderer(renderState.shape, renderState.colour) ?: return
		queue.submitCustom(matrices, RENDER_LAYER(renderState.shape.type.texture), renderer)

	}

	fun <S : SpellShape> prepareRenderer(shape: S, colour: Int): ShapeCommandRenderer<S>? {
		@Suppress("UNCHECKED_CAST")
		val factory = registry.firstOrNull { it.type == shape.type }?.factory as? RendererFactory<S> ?: return null
		return factory.invoke(shape, colour)
	}

	companion object {

		class Entry<S : SpellShape>(val type: ShapeType<S>, val factory: (S, Int) -> ShapeCommandRenderer<S>)

		val registry = mutableListOf<Entry<*>>()

		fun <S : SpellShape> register(
			shape: ShapeType<S>,
			renderer: (S, Int) -> ShapeCommandRenderer<S>
		): (S, Int) -> ShapeCommandRenderer<S> {
			registry.add(Entry(shape, renderer))
			return renderer
		}

		val CIRCLE = register(ShapeType.CIRCLE, ::CircleCommandRenderer)
		val SQUARE = register(ShapeType.SQUARE, ::SquareCommandRenderer)
		val SPHERE = register(ShapeType.SPHERE, ::SphereCommandRenderer)
		val CUBE = register(ShapeType.CUBE, ::CubeCommandRenderer)

		val PIPELINE = RenderPipelines.register(
			RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
				.withLocation(MTT.identifier("pipeline/shape_entity"))
				.withVertexShader("core/entity")
				.withFragmentShader(MTT.identifier("core/shape_entity"))
				.withBlend(BlendFunction.TRANSLUCENT)
				.withCull(false)
				.build()
		)

		val RENDER_LAYER = { texture: Identifier ->
			val renderSetup = RenderSetup.builder(PIPELINE)
				.texture("Sampler0", texture)
				.translucent()
				.build()
			RenderLayer.of("shape_entity", renderSetup)
		}
	}
}
typealias RendererFactory<S> = (S, Int) -> ShapeCommandRenderer<S>