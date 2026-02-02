package com.pineypiney.mtt.util

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object D20 {
	val random = Random(System.currentTimeMillis())
	fun roll() = random.nextInt(20)
	fun adv() = max(roll(), roll())
	fun dis() = min(roll(), roll())
}