package com.pineypiney.mtt.dnd.spells

import com.pineypiney.mtt.MTT
import net.minecraft.util.Identifier

class ShapeType<S : SpellShape>(val texture: Identifier) {

	companion object {
		val register = mutableMapOf<Identifier, ShapeType<*>>()

		fun <S : SpellShape> register(id: Identifier, type: ShapeType<S>): ShapeType<S> {
			register[id] = type
			return type
		}

		val SINGLE = register(
			MTT.identifier("single"),
			ShapeType<SpellShape.Single>(MTT.identifier("textures/entity/shape/circle.png"))
		)
		val CIRCLE = register(
			MTT.identifier("circle"),
			ShapeType<SpellShape.Circle>(MTT.identifier("textures/entity/shape/circle.png"))
		)
		val SQUARE = register(
			MTT.identifier("square"),
			ShapeType<SpellShape.Square>(MTT.identifier("textures/entity/shape/square.png"))
		)
		val SPHERE = register(
			MTT.identifier("sphere"),
			ShapeType<SpellShape.Sphere>(MTT.identifier("textures/entity/shape/circle.png"))
		)
		val CUBE = register(
			MTT.identifier("cube"),
			ShapeType<SpellShape.Cube>(MTT.identifier("textures/entity/shape/square.png"))
		)
	}
}