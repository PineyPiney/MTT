package com.pineypiney.mtt.commands.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.item.dnd.DNDGameItem
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.item.Item
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.server.command.ServerCommandSource
import java.util.concurrent.CompletableFuture

class DNDGameItemArgumentType(val access: CommandRegistryAccess) : ArgumentType<DNDGameItemArgumentType.GameItemArgument> {

	override fun parse(reader: StringReader): GameItemArgument {
		val arg = reader.readString()
		val id = MTT.identifier(arg)
		val registry = access.getOrThrow(RegistryKeys.ITEM)
		val item = registry.getOrThrow(RegistryKey.of(RegistryKeys.ITEM, id))
		return GameItemArgument(item)
	}

	override fun <S> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {

		val registry = access.getOrThrow(RegistryKeys.ITEM)
		for (entry in registry.streamEntries()) {
			val key = entry.registryKey().value
			if (entry.value() is DNDGameItem) {
				val suggestions = if (key.namespace == MTT.MOD_ID) listOf(key.path, key.toString()) else listOf(key.toString())
				for (suggest in suggestions) if (suggest.startsWith(builder.remainingLowerCase)) builder.suggest(suggest)
			}
		}

		return builder.buildFuture()
	}

	companion object {
		fun getItem(ctx: CommandContext<ServerCommandSource>, arg: String): DNDGameItem {
			return ctx.getArgument(arg, GameItemArgument::class.java).getItem()
		}
	}

	class GameItemArgument(val item: RegistryEntry<Item>) {
		fun getItem() = item.value() as DNDGameItem
	}
}