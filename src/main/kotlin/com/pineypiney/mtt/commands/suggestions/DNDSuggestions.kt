package com.pineypiney.mtt.commands.suggestions

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.pineypiney.mtt.commands.MTTCommands
import com.pineypiney.mtt.dnd.characters.Character
import net.minecraft.server.command.ServerCommandSource
import java.util.*

object DNDSuggestions {

	val ALL_NAMES = DNDPlayerNameSuggestionProvider(false)
	val PLAYER_NAMES = DNDPlayerNameSuggestionProvider(true)

	fun getCharacter(ctx: CommandContext<ServerCommandSource>, name: String): Character?{
		val queryName = StringArgumentType.getString(ctx, name)
		val engine = MTTCommands.getEngine(ctx)
		val firstDash = queryName.indexOf('-')
		return if (firstDash == -1) {
			engine.getCharacter(queryName)
		}
		else {
			val uuid = UUID.fromString(queryName.substring(firstDash + 1))
			engine.getCharacter(uuid)
		}
	}
}