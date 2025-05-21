package com.pineypiney.mtt.item.dnd.equipment

import com.pineypiney.mtt.dnd.DamageType

open class 	WeaponType(val damageType: DamageType, val numDice: Int, val sides: Int, val martial: Boolean, val ranged: Boolean, val finesse: Boolean, val heavy: Boolean, val light: Boolean, val reach: Boolean, val twoHanded: Boolean, val versatile: Boolean, val nearDistance: Float, val farDistance: Float) {

	companion object {
		fun melee(damageType: DamageType, numDice: Int, sides: Int, martial: Boolean = true, finesse: Boolean = false, heavy: Boolean = false, light: Boolean = false, reach: Boolean = false, twoHanded: Boolean = false, versatile: Boolean = false, nearDistance: Float = 0f, farDistance: Float = 0f): WeaponType{
			return WeaponType(damageType, numDice, sides, martial, false, finesse, heavy, light, reach, twoHanded, versatile, nearDistance, farDistance)
		}
		fun ranged(sides: Int, martial: Boolean, nearDistance: Float, farDistance: Float, finesse: Boolean = false, heavy: Boolean = false, light: Boolean = false, twoHanded: Boolean = false): WeaponType {
			return WeaponType(DamageType.PIERCING, 1, sides, martial, true, finesse, heavy, light, false, twoHanded, false, nearDistance, farDistance)
		}

		// Simple Melee
		val CLUB = melee(DamageType.BLUDGEONING, 1, 4, false, light = true)
		val GREAT_CLUB = melee(DamageType.BLUDGEONING, 1, 8, false, twoHanded = true)
		val DAGGER = melee(DamageType.PIERCING, 1, 4, false, finesse = true, light = true, nearDistance = 4f, farDistance = 12f)
		val HAND_AXE = melee(DamageType.SLASHING, 1, 6, false, light = true, nearDistance = 4f, farDistance = 12f)
		val JAVELIN = melee(DamageType.PIERCING, 1, 6, false, nearDistance = 6f, farDistance = 24f)
		val LIGHT_HAMMER = melee(DamageType.BLUDGEONING, 1, 4, false, light = true, nearDistance = 4f, farDistance = 12f)
		val MACE = melee(DamageType.BLUDGEONING, 1, 6, false)
		val QUARTER_STAFF = melee(DamageType.BLUDGEONING, 1, 6, false, versatile = true)
		val SICKLE = melee(DamageType.SLASHING, 1, 4, false, light = true)
		val SPEAR = melee(DamageType.PIERCING, 1, 6, false, versatile = true, nearDistance = 4f, farDistance = 12f)

		// Martial Melee
		val SHORT_SWORD = melee(DamageType.PIERCING, 1, 6, finesse = true, light = true)
		val LONG_SWORD = melee(DamageType.SLASHING, 1, 8, versatile = true)
		val GREAT_SWORD = melee(DamageType.SLASHING, 2, 6, heavy = true, twoHanded = true)

		// Simple Ranged
		val LIGHT_CROSSBOW = ranged(8, false, 16f, 64f, twoHanded = true)
		val DART = ranged(4, false, 6f, 12f, finesse = true)
		val SHORTBOW = ranged(6, false, 16f, 64f, twoHanded = true)
		val SLING = ranged(4, false, 6f, 24f)

		// Martial Ranged
		val BLOWGUN = ranged(1, true, 5f, 20f)
		val HAND_CROSSBOW = ranged(6, true, 6f, 24f, light = true)
		val HEAVY_CROSSBOW = ranged(10, true, 20f, 80f, heavy = true, twoHanded = true)
		val LONGBOW = ranged(8, true, 30f, 120f, heavy = true, twoHanded = true)
		val NET = ranged(0, true, 1f, 3f)
	}







}