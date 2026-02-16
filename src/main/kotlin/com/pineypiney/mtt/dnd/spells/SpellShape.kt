package com.pineypiney.mtt.dnd.spells

import com.pineypiney.mtt.MTT
import net.minecraft.util.Identifier
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import kotlin.math.abs
import kotlin.math.sqrt

abstract class SpellShape(val id: Identifier, val type: ShapeType<*>) {

	abstract fun isIn(origin: Vec3d, direction: Float, pos: Vec3d): Boolean

	abstract fun getContainingBox(position: Vec3d, direction: Float): Box
	abstract fun getHitBox(position: Vec3d, direction: Float): Box

	object Single : SpellShape(MTT.identifier("single"), ShapeType.SINGLE) {
		override fun isIn(origin: Vec3d, direction: Float, pos: Vec3d) = origin == pos
		override fun getContainingBox(position: Vec3d, direction: Float): Box {
			return Box(position, position)
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
			val relative = pos.subtract(origin)
			return abs(relative.x) <= blocksHalfSide
					&& abs(relative.y) < blocksHalfSide
					&& abs(relative.z) < blocksHalfSide
		}

		override fun getContainingBox(position: Vec3d, direction: Float): Box {
			return Box(position.add(-blocksHalfSide), position.add(blocksHalfSide))
		}

		override fun getHitBox(position: Vec3d, direction: Float): Box {
			return Box(position.add(-blocksHalfSide), position.add(blocksHalfSide))
		}
	}
}