package com.pineypiney.mtt.client.render.entity.shapes

import com.pineypiney.mtt.dnd.spells.SpellShape
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.util.math.MatrixStack
import org.joml.Vector3f

class ConeCommandRenderer(shape: SpellShape.Cone, colour: Int) :
	ShapeCommandRenderer<SpellShape.Cone>(shape, colour) {

	override fun render(matricesEntry: MatrixStack.Entry, vertexConsumer: VertexConsumer) {
		val length = shape.blocks.toFloat()
		quad(
			matricesEntry,
			vertexConsumer,
			Vector3f(-length * .5f, 0.01f, 0f),
			Vector3f(0f, 0f, length),
			Vector3f(length, 0f, 0f),
			colour
		)
	}
}