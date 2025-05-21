package com.pineypiney.mtt

import com.pineypiney.mtt.dice.DiceFormula
import kotlin.math.max
import kotlin.math.min

fun main() {
	val string = "2d6mi3kh1 + 2d6mi3kh1 + 9 + 2d8mi3kh1"
	debugRolls(string)
}

fun debugRolls(string: String){
	val value = DiceFormula.parse(string, true, true)
	println("Finished Value is $value")
}

fun timeRolls(string: String){
	val t = System.nanoTime()
	var min = Int.MAX_VALUE
	var max = Int.MIN_VALUE
	for(i in 0..99) {
		val value = DiceFormula.parse(string)
		min = min(min, value)
		max = max(max, value)
	}
	val endTime = System.nanoTime()
	println("Min: $min, Max: $max")
	println("Time per iteration: ${(endTime - t) * 1e-7}ns")
}