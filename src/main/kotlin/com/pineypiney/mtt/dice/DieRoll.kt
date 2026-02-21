package com.pineypiney.mtt.dice

class DieRoll(val sides: Int) {
	var adv = false
	var dis = false
	var multiplier = 1f
	var bonus = 0

	fun roll(): Int {
		val roll = if (adv == dis) Dice.roll(sides)
		else if (adv) Dice.adv(sides)
		else Dice.dis(sides)

		return (roll * multiplier).toInt() + bonus
	}

	companion object {
		fun adv() = DieRoll(20).apply { adv = true }
		fun dis() = DieRoll(20).apply { dis = true }
	}
}