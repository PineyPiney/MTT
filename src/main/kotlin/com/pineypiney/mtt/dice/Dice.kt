package com.pineypiney.mtt.dice

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class Dice {
	companion object{
		fun roll(sides: Int) = Random.nextInt(1, sides + 1)
		fun adv(sides: Int) = max(roll(sides), roll(sides))
		fun dis(sides: Int) = min(roll(sides), roll(sides))
	}
}