package com.pineypiney.mtt.client.render.entity.shapes

import com.pineypiney.mtt.dnd.spells.SpellShape
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.util.math.MatrixStack
import org.joml.Vector3f

class CircleCommandRenderer(shape: SpellShape.Circle, colour: Int) :
	ShapeCommandRenderer<SpellShape.Circle>(shape, colour) {

	override fun render(matricesEntry: MatrixStack.Entry, vertexConsumer: VertexConsumer) {
		val radius = shape.blocks.toFloat()
		quad(
			matricesEntry,
			vertexConsumer,
			Vector3f(-radius, 0.01f, -radius),
			Vector3f(0f, 0f, radius * 2f),
			Vector3f(radius * 2f, 0f, 0f),
			colour
		)
	}
}