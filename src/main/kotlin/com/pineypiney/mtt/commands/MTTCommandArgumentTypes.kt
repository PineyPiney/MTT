package com.pineypiney.mtt.commands

import com.google.gson.JsonObject
import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.commands.arguments.*
import com.pineypiney.mtt.commands.arguments.serialiser.SingleSerialiser
import com.pineypiney.mtt.util.nullify
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.util.Identifier

object MTTCommandArgumentTypes {
	fun registerArgumentTypes() {
		ArgumentTypeRegistry.registerArgumentType(
			Identifier.of(MTT.MOD_ID, "roll_formula"),
			DiceFormulaArgumentType::class.java,
			ConstantArgumentSerializer.of(::DiceFormulaArgumentType)
		)
		ArgumentTypeRegistry.registerArgumentType(
			Identifier.of(MTT.MOD_ID, "character"),
			DNDCharacterArgumentType::class.java,
			SingleSerialiser(PacketCodecs.BOOLEAN, ::DNDCharacterArgumentType, DNDCharacterArgumentType::playerCharactersOnly, JsonObject::addProperty)
		)
		ArgumentTypeRegistry.registerArgumentType(
			Identifier.of(MTT.MOD_ID, "game_items"),
			DNDGameItemArgumentType::class.java,
			ConstantArgumentSerializer.of(::DNDGameItemArgumentType)
		)
		ArgumentTypeRegistry.registerArgumentType(
			Identifier.of(MTT.MOD_ID, "condition"),
			DNDConditionArgumentType::class.java,
			SingleSerialiser(PacketCodecs.STRING.nullify(), ::DNDConditionArgumentType, DNDConditionArgumentType::characterArgument) { str, value ->
				addProperty(str, value)
			}
		)
		ArgumentTypeRegistry.registerArgumentType(
			Identifier.of(MTT.MOD_ID, "condition_state"),
			DNDConditionStateArgumentType::class.java,
			ConstantArgumentSerializer.of(::DNDConditionStateArgumentType)
		)
	}
}