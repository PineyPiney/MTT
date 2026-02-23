package com.pineypiney.mtt.commands.arguments

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.network.ServerCharacter
import com.pineypiney.mtt.mixin_interfaces.MTTCommandSource
import net.minecraft.command.CommandSource
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.ServerCommandSource
import java.util.*
import java.util.concurrent.CompletableFuture

class DNDCharacterArgumentType(val playerCharactersOnly: Boolean) : ArgumentType<DNDCharacterArgumentType.CharacterArgument> {

	override fun parse(reader: StringReader): CharacterArgument {
		val queryName = reader.readString()

		val firstDash = queryName.indexOf('-')
		return if (firstDash == -1) {
			CharacterArgument(queryName, null)
		} else {
			val uuid = UUID.fromString(queryName.substring(firstDash + 1))
			CharacterArgument(queryName.substring(0, firstDash), uuid)
		}
	}

	override fun <S> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {

		val source = context.source as? MTTCommandSource ?: return Suggestions.empty()
		val characters = source.getCharacterSuggestions(playerCharactersOnly)
		val names = mutableMapOf<String, MutableSet<Character>>()
		val playerPos = source.`mTT$getPosition`()

		// Add all player names to the builder.
		for (character in characters) {
			if (!character.name.lowercase().startsWith(builder.remainingLowerCase)) continue
			val charactersWithName = names[character.name]
			if (charactersWithName == null) {
				names[character.name] = mutableSetOf(character)
			} else charactersWithName.add(character)
		}

		for ((name, characters) in names) {
			if (characters.size == 1) {
				val suggestion = if (name.contains(' ')) "\"$name\"" else name
				builder.suggest(suggestion)
			} else {
				for (character in characters) {
					val suggestion = "\"$name-${character.uuid}\""
					if (playerPos == null) {
						builder.suggest(suggestion)
					} else {

						val distance = (playerPos.subtract(character.pos)).length()
						val distStr = "%.3f".format(distance)
						val tooltip = LiteralMessage("$name who is $distStr blocks away")
						builder.suggest(suggestion, tooltip)
					}
				}
			}
		}

		// Lock the suggestions after modifying them.
		return builder.buildFuture()
	}

	companion object {
		fun getArgument(ctx: CommandContext<*>, arg: String) = ctx.getArgument(arg, CharacterArgument::class.java)

		fun getCharacter(ctx: CommandContext<ServerCommandSource>, arg: String): ServerCharacter {
			return getArgument(ctx, arg).getCharacter(ctx.source) as ServerCharacter
		}

		fun getPlayableCharacter(ctx: CommandContext<ServerCommandSource>, arg: String): ServerCharacter {
			return getArgument(ctx, arg).getCharacter(ctx.source) as ServerCharacter
		}
	}

	class CharacterArgument(val name: String, val uuid: UUID?) {
		fun getCharacter(src: CommandSource): Character = getCharacter(src as MTTCommandSource)
		fun getCharacter(src: MTTCommandSource): Character {
			val engine = src.`mTT$getEngine`()
			return if (uuid == null) engine.getCharacter(name) ?: throw EntityArgumentType.ENTITY_NOT_FOUND_EXCEPTION.create()
			else engine.getCharacter(uuid) ?: throw EntityArgumentType.ENTITY_NOT_FOUND_EXCEPTION.create()
		}
	}
}