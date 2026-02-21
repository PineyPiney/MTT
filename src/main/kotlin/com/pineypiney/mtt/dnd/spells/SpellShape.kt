package com.pineypiney.mtt.dnd.spells

import com.pineypiney.mtt.MTT
import net.minecraft.util.Identifier
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

abstract class SpellShape(val id: Identifier, val type: ShapeType<*>) {

	abstract fun isIn(origin: Vec3d, direction: Float, pos: Vec3d): Boolean

	abstract fun getContainingBox(position: Vec3d, direction: Float): Box
	abstract fun getHitBox(position: Vec3d, direction: Float): Box

	open fun placeAtCentre(position: Vec3d, direction: Float) = position

	object Single : SpellShape(MTT.identifier("single"), ShapeType.SINGLE) {
		override fun isIn(origin: Vec3d, direction: Float, pos: Vec3d) = origin == pos || origin.squaredDistanceTo(pos) < 1e-9
		override fun getContainingBox(position: Vec3d, direction: Float): Box {
			return Box(position.add(-1e-3), position.add(1e-3))
		}

		override fun getHitBox(position: Vec3d, direction: Float): Box {
			return Box(position.add(-.25), position.add(.25))
		}
	}

	class Circle(val radius: Int) : SpellShape(MTT.identifier("circle"), ShapeType.CIRCLE) {
		val blocks = radius * .2
		val blocksSquared = radius * radius * .04
		override fun isIn(origin: Vec3d, direction: Float, pos: Vec3d): Boolean {
			return origin.horizontal.squaredDistanceTo(
				pos.x,
				0.0,
				pos.z
			) <= blocksSquared && abs(origin.y - pos.y) <= 1.0
		}

		override fun getContainingBox(position: Vec3d, direction: Float): Box {
			return Box(position.subtract(blocks, 0.0, blocks), position.add(blocks, 1.0, blocks))
		}

		override fun getHitBox(position: Vec3d, direction: Float): Box {
			val side = blocks / sqrt(2.0)
			return Box(position.add(Vec3d(-side, -.25, -side)), position.add(Vec3d(side, .5, side)))
		}
	}

	class Cone(val length: Int) : SpellShape(MTT.identifier("cone"), ShapeType.CONE) {
		val blocks = length * .2

		override fun isIn(origin: Vec3d, direction: Float, pos: Vec3d): Boolean {
			if (origin == pos) return false
			val vec = pos.subtract(origin)
			val posAngle = vec.yawAndPitch.y
			val relativeAngle = abs(MathHelper.wrapDegrees(posAngle - direction))
			if (relativeAngle > DEGREES) return false

			val unit = vec3(direction, 1.0)
			val dist = vec.dotProduct(unit)
			return dist < blocks
		}

		override fun getContainingBox(position: Vec3d, direction: Float): Box {
			val point1 = position.add(vec3(direction + DEGREES, blocks))
			val point2 = position.add(vec3(direction - DEGREES, blocks))

			return Box(position, point1).union(Box(point2.offset(Direction.UP, 1.0), point2))
		}

		override fun getHitBox(position: Vec3d, direction: Float): Box {
			return getContainingBox(position, direction)
		}

		companion object {
			// arctan(.5)
			val DEGREES = 26.5650511771f
		}
	}

	class Square(val side: Int) : SpellShape(MTT.identifier("square"), ShapeType.SQUARE) {
		val blocksHalfSide = side * .1
		override fun isIn(origin: Vec3d, direction: Float, pos: Vec3d): Boolean {
			val relative = pos.subtract(origin)
			return abs(relative.x) <= blocksHalfSide
					&& relative.y > 0.0 && relative.y < 1.0
					&& abs(relative.z) < blocksHalfSide
		}

		override fun getContainingBox(position: Vec3d, direction: Float): Box {
			return Box(
				position.subtract(blocksHalfSide, 0.0, blocksHalfSide),
				position.add(blocksHalfSide, 1.0, blocksHalfSide)
			)
		}

		override fun getHitBox(position: Vec3d, direction: Float): Box {
			return Box(
				position.add(Vec3d(-blocksHalfSide, -.25, -blocksHalfSide)),
				position.add(Vec3d(blocksHalfSide, .5, blocksHalfSide))
			)
		}
	}

	class Sphere(val radius: Int) : SpellShape(MTT.identifier("sphere"), ShapeType.SPHERE) {
		val blocks = radius * .2
		val blocksSquared = radius * radius * .04
		override fun isIn(origin: Vec3d, direction: Float, pos: Vec3d): Boolean {
			return origin.squaredDistanceTo(pos) <= blocksSquared
		}

		override fun getContainingBox(position: Vec3d, direction: Float): Box {
			return Box(position.subtract(blocks, blocks, blocks), position.add(blocks, blocks, blocks))
		}

		override fun getHitBox(position: Vec3d, direction: Float): Box {
			val side = blocks / sqrt(2.0)
			return Box(position.add(-side), position.add(side))
		}
	}

	class Cube(val side: Int) : SpellShape(MTT.identifier("cube"), ShapeType.CUBE) {
		val blocksHalfSide = side * .1
		override fun isIn(origin: Vec3d, direction: Float, pos: Vec3d): Boolean {
			val vec = pos.subtract(origin)
			val rel = vec.rotateY(MathHelper.RADIANS_PER_DEGREE * direction)
			return abs(rel.x) <= blocksHalfSide
					&& abs(rel.y) < blocksHalfSide
					&& abs(rel.z - blocksHalfSide) < blocksHalfSide
		}

		override fun getContainingBox(position: Vec3d, direction: Float): Box {
			val centre = position.add(vec3(direction, blocksHalfSide))
			val angle = 45f - abs(direction.mod(90f) - 45f)
			val halfSide = blocksHalfSide / cos(angle)
			return Box(centre.add(-halfSide), centre.add(halfSide))
		}

		override fun getHitBox(position: Vec3d, direction: Float): Box {
			val centre = position.add(vec3(direction, blocksHalfSide))
			val angle = 45f - abs(direction.mod(90f) - 45f)
			val halfSide = blocksHalfSide * cos(angle * MathHelper.RADIANS_PER_DEGREE)
			return Box(centre.add(-halfSide), centre.add(halfSide))
		}

		override fun placeAtCentre(position: Vec3d, direction: Float): Vec3d {
			return position.subtract(vec3(direction, blocksHalfSide))
		}
	}

	companion object {
		fun vec3(yaw: Float, length: Double): Vec3d {
			return vec3(Math.toRadians(yaw.toDouble()), length)
		}

		fun vec3(yaw: Double, length: Double): Vec3d {
			return Vec3d(-sin(yaw) * length, 0.0, cos(yaw) * length)
		}
	}
}