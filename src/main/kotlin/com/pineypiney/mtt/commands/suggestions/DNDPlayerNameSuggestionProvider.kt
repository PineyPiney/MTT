package com.pineypiney.mtt.commands.suggestions

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.pineypiney.mtt.commands.MTTCommands
import net.minecraft.server.command.ServerCommandSource
import java.util.concurrent.CompletableFuture

class DNDPlayerNameSuggestionProvider: SuggestionProvider<ServerCommandSource> {

	override fun getSuggestions(
		context: CommandContext<ServerCommandSource>,
		builder: SuggestionsBuilder
	): CompletableFuture<Suggestions> {
		val engine = MTTCommands.getEngine(context)

		// Add all player names to the builder.
		for (player in engine.getAllPlayerCharacters()) {
			builder.suggest(player.name)
		}

		// Lock the suggestions after modifying them.
		return builder.buildFuture()
	}
}