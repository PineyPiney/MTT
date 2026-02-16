package com.pineypiney.mtt.client.render.entity.shapes

import com.pineypiney.mtt.dnd.spells.SpellShape
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.util.math.MatrixStack
import org.joml.Vector3f

class SquareCommandRenderer(shape: SpellShape.Square, colour: Int) :
	ShapeCommandRenderer<SpellShape.Square>(shape, colour) {

	override fun render(matricesEntry: MatrixStack.Entry, vertexConsumer: VertexConsumer) {
		val length = shape.blocksHalfSide.toFloat()
		quad(
			matricesEntry,
			vertexConsumer,
			Vector3f(-length, 0.01f, -length),
			Vector3f(0f, 0f, length * 2f),
			Vector3f(length * 2f, 0f, 0f),
			colour
		)
	}
}