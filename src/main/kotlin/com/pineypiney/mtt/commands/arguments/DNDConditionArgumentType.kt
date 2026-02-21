package com.pineypiney.mtt.commands.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.conditions.Condition
import com.pineypiney.mtt.dnd.conditions.Conditions
import com.pineypiney.mtt.mixin_interfaces.MTTCommandSource
import net.minecraft.command.CommandSource
import net.minecraft.text.Text
import java.util.concurrent.CompletableFuture

class DNDConditionArgumentType(val characterArgument: String?) : ArgumentType<Condition<*>> {
	override fun parse(reader: StringReader): Condition<*> {
		val id = MTT.identifier(reader.readString())
		try {
			return Conditions.findById(id)
		} catch (_: IllegalArgumentException) {
			throw EXCEPTION.createWithContext(reader)
		}
	}

	override fun <S> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
		val source = context.source as? CommandSource
		if (source == null || !listCharacterConditions(context, builder, source as MTTCommandSource)) {
			Conditions.forEach { condition -> builder.suggest(MTT.identifierString(condition.id)) }
		}
		return builder.buildFuture()
	}

	fun listCharacterConditions(context: CommandContext<*>, builder: SuggestionsBuilder, source: MTTCommandSource): Boolean {
		val character = if (characterArgument != null) DNDCharacterArgumentType.getArgument(context, characterArgument).getCharacter(source)
		else {
			val player = source.`mTT$getPlayer`() ?: return false
			source.`mTT$getEngine`().getCharacterFromPlayer(player.uuid) ?: return false
		}
		var added = false
		character.conditions.forEachCondition { condition ->
			builder.suggest(MTT.identifierString(condition.id))
			added = true
		}
		return added
	}

	companion object {

		val EXCEPTION = SimpleCommandExceptionType(Text.literal("Failed to parse condition"))

		fun getCondition(ctx: CommandContext<*>, arg: String): Condition<*> {
			return ctx.getArgument(arg, Condition::class.java)
		}
	}
}