package com.pineypiney.mtt.dice

import kotlin.random.Random

class Dice {
	companion object{
		fun roll(sides: Int) = Random.nextInt(1, sides + 1)
	}
}