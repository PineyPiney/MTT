package com.pineypiney.mtt.client.render.entity.shapes

import com.pineypiney.mtt.dnd.spells.SpellShape
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.util.math.MatrixStack
import org.joml.*

class SphereCommandRenderer(shape: SpellShape.Sphere, colour: Int) :
	ShapeCommandRenderer<SpellShape.Sphere>(shape, colour) {

	override fun render(matricesEntry: MatrixStack.Entry, vertexConsumer: VertexConsumer) {
		val viewDir = -matricesEntry.positionMatrix.origin(Vector3f())
		val side1 = viewDir.cross(Vector3f(0f, 1f, 0f), Vector3f()).normalize()
		val side2 = side1.cross(viewDir, Vector3f()).normalize()
		val radius = shape.blocks.toFloat()
		quad(
			matricesEntry,
			vertexConsumer,
			Vector3f(0f) - ((side1 + side2) * radius),
			side1 * radius * 2f,
			side2 * radius * 2f,
			colour
		)
	}
}