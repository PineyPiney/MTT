package com.pineypiney.mtt.commands.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.Duration
import com.pineypiney.mtt.dnd.conditions.Condition
import com.pineypiney.mtt.dnd.conditions.Conditions
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class DNDConditionStateArgumentType : ArgumentType<Condition.ConditionState<*>> {
	override fun parse(reader: StringReader): Condition.ConditionState<*> {
		val start = reader.cursor
		val argsStart = reader.string.indexOf('[', reader.cursor + 1)
		val argsEnd = reader.string.indexOf(']', argsStart + 1)
		if (argsStart == -1 || argsEnd == -1) throw EXCEPTION.createWithContext(reader)

		val id = MTT.identifier(reader.string.substring(start, argsStart))
		val args = reader.string.substring(argsStart + 1, argsEnd)
		reader.cursor = argsEnd + 1

		val condition = Conditions.findById(id)
		val conClass = condition::class
		val stateClass = conClass.nestedClasses.firstOrNull { it.simpleName == "State" } ?: throw EXCEPTION.createWithContext(reader)
		val constructor = stateClass.primaryConstructor ?: throw EXCEPTION.createWithContext(reader)
		val argMap: MutableMap<KParameter, Any?> = constructor.parameters.associateWith { _ -> null }.toMutableMap()

		val argsStrings = args.split(",")
		for (argString in argsStrings) {
			val (keyName, value) = argString.split("=")
			val key = argMap.keys.firstOrNull { it.name == keyName } ?: continue
			when (keyName.lowercase()) {
				"duration" -> parseDuration(reader, key, value, argMap)
			}
		}
		return constructor.callBy(argMap) as Condition.ConditionState<*>
	}

	fun parseDuration(reader: StringReader, key: KParameter, value: String, args: MutableMap<KParameter, Any?>) {
		if (value.isEmpty()) throw EXCEPTION.createWithContext(reader)
		args[key] = when (value[0]) {
			't' -> Duration.Time(value.substring(1).toInt())
			's' -> Duration.ShortRest(false)
			'l' -> Duration.LongRest(false)
			else -> throw EXCEPTION.createWithContext(reader)
		}
	}

	companion object {

		val EXCEPTION = SimpleCommandExceptionType(Text.literal("Failed to parse condition"))

		fun getCondition(ctx: CommandContext<ServerCommandSource>, arg: String): Condition.ConditionState<*> {
			return ctx.getArgument(arg, Condition.ConditionState::class.java)
		}
	}
}