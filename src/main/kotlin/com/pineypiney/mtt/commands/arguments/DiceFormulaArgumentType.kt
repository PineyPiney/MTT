package com.pineypiney.mtt.commands.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.pineypiney.mtt.dice.DiceFormula

class DiceFormulaArgumentType : ArgumentType<Int> {

	override fun parse(reader: StringReader): Int {
		val fullString = reader.run { string.substring(cursor) }
		reader.cursor = reader.totalLength
		return DiceFormula.parse(fullString)
	}
}