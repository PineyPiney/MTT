package com.pineypiney.mtt.dnd.traits.proficiencies

import com.pineypiney.mtt.dnd.DamageType

class WeaponType(override val id: String, val damageType: DamageType, val numDice: Int, val sides: Int, val martial: Boolean, val ranged: Boolean, val finesse: Boolean, val heavy: Boolean, val light: Boolean, val reach: Boolean, val twoHanded: Boolean, val versatile: Boolean, val nearDistance: Float, val farDistance: Float) :
	EquipmentType {

	companion object {

		val set = mutableSetOf<WeaponType>()

		fun melee(id: String, damageType: DamageType, numDice: Int, sides: Int, martial: Boolean = true, finesse: Boolean = false, heavy: Boolean = false, light: Boolean = false, reach: Boolean = false, twoHanded: Boolean = false, versatile: Boolean = false, nearDistance: Float = 0f, farDistance: Float = 0f): WeaponType{
			val type = WeaponType(id, damageType, numDice, sides, martial, false, finesse, heavy, light, reach, twoHanded, versatile, nearDistance, farDistance)
			return type.also { set.add(it) }
		}
		fun ranged(id: String, sides: Int, martial: Boolean, nearDistance: Float, farDistance: Float, finesse: Boolean = false, heavy: Boolean = false, light: Boolean = false, twoHanded: Boolean = false): WeaponType {
			val type = WeaponType(
				id,
				DamageType.PIERCING,
				1,
				sides,
				martial,
				true,
				finesse,
				heavy,
				light,
				false,
				twoHanded,
				false,
				nearDistance,
				farDistance
			)
			return type.also { set.add(it) }
		}

		// Simple Melee
		val CLUB = melee("club", DamageType.BLUDGEONING, 1, 4, false, light = true)
		val GREAT_CLUB = melee("great_club", DamageType.BLUDGEONING, 1, 8, false, twoHanded = true)
		val DAGGER = melee(
			"dagger",
			DamageType.PIERCING,
			1,
			4,
			false,
			finesse = true,
			light = true,
			nearDistance = 4f,
			farDistance = 12f
		)
		val HAND_AXE =
			melee("hand_axe", DamageType.SLASHING, 1, 6, false, light = true, nearDistance = 4f, farDistance = 12f)
		val JAVELIN = melee("javelin", DamageType.PIERCING, 1, 6, false, nearDistance = 6f, farDistance = 24f)
		val LIGHT_HAMMER = melee(
			"light_hammer",
			DamageType.BLUDGEONING,
			1,
			4,
			false,
			light = true,
			nearDistance = 4f,
			farDistance = 12f
		)
		val MACE = melee("mace", DamageType.BLUDGEONING, 1, 6, false)
		val QUARTER_STAFF = melee("quarter_staff", DamageType.BLUDGEONING, 1, 6, false, versatile = true)
		val SICKLE = melee("sickle", DamageType.SLASHING, 1, 4, false, light = true)
		val SPEAR =
			melee("spear", DamageType.PIERCING, 1, 6, false, versatile = true, nearDistance = 4f, farDistance = 12f)

		// Martial Melee
		val SHORT_SWORD = melee("short_sword", DamageType.PIERCING, 1, 6, finesse = true, light = true)
		val LONG_SWORD = melee("long_sword", DamageType.SLASHING, 1, 8, versatile = true)
		val GREAT_SWORD = melee("great_sword", DamageType.SLASHING, 2, 6, heavy = true, twoHanded = true)

		// Simple Ranged
		val LIGHT_CROSSBOW = ranged("light_crossbow", 8, false, 16f, 64f, twoHanded = true)
		val DART = ranged("dart", 4, false, 6f, 12f, finesse = true)
		val SHORTBOW = ranged("shortbow", 6, false, 16f, 64f, twoHanded = true)
		val SLING = ranged("sling", 4, false, 6f, 24f)

		// Martial Ranged
		val BLOWGUN = ranged("blowgun", 1, true, 5f, 20f)
		val HAND_CROSSBOW = ranged("hand_crossbow", 6, true, 6f, 24f, light = true)
		val HEAVY_CROSSBOW = ranged("heavy_crossbow", 10, true, 20f, 80f, heavy = true, twoHanded = true)
		val LONGBOW = ranged("longbow", 8, true, 30f, 120f, heavy = true, twoHanded = true)
		val NET = ranged("net", 0, true, 1f, 3f)
	}
}