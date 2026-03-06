package com.pineypiney.mtt.dice

class DieRoll(val die: Die) {
	var adv = false
	var dis = false
	var multiplier = 1f
	var bonus = 0

	fun roll(): RollResult {
		val roll = if (adv == dis) Dice.roll(die.sides)
		else if (adv) Dice.adv(die.sides)
		else Dice.dis(die.sides)

		return RollResult(die, (roll * multiplier).toInt() + bonus, roll == 20, roll == 1)
	}

	companion object {
		fun d20() = DieRoll(Die.D20)
	}
}