package com.pineypiney.mtt.commands.suggestions

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.pineypiney.mtt.commands.MTTCommands
import com.pineypiney.mtt.dnd.characters.Character
import net.minecraft.server.command.ServerCommandSource
import java.util.concurrent.CompletableFuture

class DNDPlayerNameSuggestionProvider(val onlyPlayerCharacters: Boolean): SuggestionProvider<ServerCommandSource> {

	override fun getSuggestions(
		context: CommandContext<ServerCommandSource>,
		builder: SuggestionsBuilder
	): CompletableFuture<Suggestions> {
		val engine = MTTCommands.getEngine(context)
		val characters = if(onlyPlayerCharacters) engine.getAllPlayerCharacters() else engine.getAllCharacters()
		val names = mutableMapOf<String, MutableSet<Character>>()
		val player = context.source.player

		// Add all player names to the builder.
		for (character in characters) {
			if (!character.name.lowercase().startsWith(builder.remainingLowerCase)) continue
			val charactersWithName = names[character.name]
			if(charactersWithName == null) { names[character.name] = mutableSetOf(character) }
			else charactersWithName.add(character)
		}

		for((name, characters) in names){
			if(characters.size == 1) {
				val suggestion = if(name.contains(' ')) "\"$name\"" else name
				builder.suggest(suggestion)
			}
			else {
				for(character in characters) {
					val suggestion = "\"$name-${character.uuid}\""
					if(player == null){
						builder.suggest(suggestion)
					}
					else {

						val distance = (player.entityPos.subtract(character.pos)).length()
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
}