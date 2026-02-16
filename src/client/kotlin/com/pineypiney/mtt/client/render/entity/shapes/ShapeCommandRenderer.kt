package com.pineypiney.mtt.client.render.entity.shapes

import com.google.common.collect.Interners
import com.pineypiney.mtt.dnd.spells.SpellShape
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.command.OrderedRenderCommandQueue
import net.minecraft.client.render.model.Baker
import net.minecraft.client.util.math.MatrixStack
import org.joml.Vector3f
import org.joml.Vector3fc
import org.joml.plus
import org.joml.times

abstract class ShapeCommandRenderer<S : SpellShape>(val shape: S, val colour: Int) : OrderedRenderCommandQueue.Custom {

	fun quad(
		matricesEntry: MatrixStack.Entry,
		vertexConsumer: VertexConsumer,
		rawOrigin: Vector3f,
		rawSide1: Vector3f,
		rawSide2: Vector3f,
		colour: Int = -1
	) {

		val posMat = matricesEntry.positionMatrix
		val rotMat = matricesEntry.normalMatrix
		val origin = posMat.transformPosition(rawOrigin)
		val side1 = rotMat * rawSide1
		val side2 = rotMat * rawSide2
		val normal = side1.cross(side2, Vector3f()).normalize()
		vertex(vertexConsumer, origin, 0f, 0f, normal, colour)
		vertex(vertexConsumer, origin + side1, 1f, 0f, normal, colour)
		vertex(vertexConsumer, origin + side1 + side2, 1f, 1f, normal, colour)
		vertex(vertexConsumer, origin + side2, 0f, 1f, normal, colour)
	}

	fun vertex(vertexConsumer: VertexConsumer, pos: Vector3f, u: Float, v: Float, normal: Vector3f, colour: Int = -1) {
		vertexConsumer.vertex(pos.x, pos.y, pos.z, colour, u, v, 655360, -1, normal.x, normal.y, normal.z)
	}

	class Interner : Baker.Vec3fInterner {
		private val INTERNER = Interners.newStrongInterner<Vector3fc>()

		override fun intern(vec: Vector3fc): Vector3fc {
			return INTERNER.intern(vec)
		}
	}
}