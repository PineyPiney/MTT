package com.pineypiney.mtt.dnd

import kotlin.random.Random

class Damage(val roll: Roll, val flat: Int, val type: DamageType) {

	constructor(flat: Int, type: DamageType) : this(Roll(0, 1), flat, type)

	fun roll(crit: Boolean) = (if (crit) roll.crit() else roll.roll()) + flat

	data class Roll(val numDie: Int, val dieSides: Int) {
		fun roll(): Int {
			if (numDie == 0) return 0
			var i = 0
			repeat(numDie) { i += Random.nextInt(1, dieSides + 1) }
			return i
		}

		fun crit(): Int {
			if (numDie == 0) return 0
			var i = 0
			repeat(numDie * 2) { i += Random.nextInt(1, dieSides + 1) }
			return i
		}
	}
}