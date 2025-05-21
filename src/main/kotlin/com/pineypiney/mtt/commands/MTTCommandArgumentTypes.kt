package com.pineypiney.mtt.commands

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.pineypiney.mtt.dice.DiceFormula
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer
import net.minecraft.util.Identifier

class DiceFormulaArgumentType() : ArgumentType<Int>{

	override fun parse(reader: StringReader?): Int? {
		val fullString = reader?.run { string.substring(cursor) } ?: return null
		reader.cursor = reader.totalLength
		return DiceFormula.parse(fullString)
	}

	companion object {
		fun create() = DiceFormulaArgumentType()
	}
}

class TestArgumentType: ArgumentType<Int>{
	override fun parse(reader: StringReader?): Int? {
		return reader?.readInt()
	}

	companion object {
		fun create() = TestArgumentType()
	}
}

fun registerArgumentTypes() {
	ArgumentTypeRegistry.registerArgumentType(
		Identifier.of("mtt", "test"),
		TestArgumentType::class.java,
		ConstantArgumentSerializer.of(TestArgumentType::create)
	)
	ArgumentTypeRegistry.registerArgumentType(
		Identifier.of("mtt", "roll_formula"),
		DiceFormulaArgumentType::class.java,
		ConstantArgumentSerializer.of(DiceFormulaArgumentType::create)
	)
}