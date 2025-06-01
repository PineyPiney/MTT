package com.pineypiney.mtt.commands

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.pineypiney.mtt.commands.suggestions.DNDSuggestions
import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.DNDServerEngine
import com.pineypiney.mtt.dnd.characters.SheetCharacter
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder
import com.pineypiney.mtt.screen.CharacterMakerScreenHandler
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.command.argument.ItemStackArgumentType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import java.util.*

class MTTCommands {

	companion object {

		fun reply(ctx: CommandContext<ServerCommandSource>, message: String, broadcast: Boolean = false){
			ctx.source.sendFeedback({ Text.literal(message) }, broadcast)
		}

		private val DICE_COMMAND = literal("roll")
			.then(argument("rollFormula", DiceFormulaArgumentType()).executes { ctx ->
				val rollResult = ctx.getArgument("rollFormula", Int::class.java)
				reply(ctx, "Roll Result: $rollResult")
				0
			})

		private val TEST_COMMAND = literal("mtt_test")
			.then(literal("default").then(argument("testInt",IntegerArgumentType.integer()).executes { ctx ->
				val int = ctx.getArgument("testInt", Integer::class.java).toInt()
				ctx.source.sendFeedback({Text.literal("Test Int: ${int * int}")}, false)
				0
			}))
			.then(literal("custom").then(argument("testType", TestArgumentType()).executes { ctx ->
				val int = ctx.getArgument("testType", Int::class.java)
				ctx.source.sendFeedback({Text.literal("Test Int: ${int * int}")}, false)
				0
			}))

		@Suppress("UNCHECKED_CAST")
		fun getEngine(ctx: CommandContext<ServerCommandSource>): DNDServerEngine {
			return (ctx.source.server as DNDEngineHolder<DNDServerEngine>).`mtt$getDNDEngine`()
		}

		private fun DND_COMMANDS(access: CommandRegistryAccess) = literal("dnd")

			// GAME
			.then(literal("game")
				.then(literal("running")
					.then(argument("isRunning", BoolArgumentType.bool()).executes { ctx ->
						val engine = getEngine(ctx)
						engine.running = BoolArgumentType.getBool(ctx, "isRunning")
						reply(ctx, "DND Game Running set to ${engine.running}")
						1
					})
					.executes { ctx ->
						val engine = getEngine(ctx)
						reply(ctx, "DND Game Running is ${engine.running}")
						1
					}
				)
			)
			// DM COMMANDS
			.then(literal("dm")
				.then(literal("set").requires{ source -> source.hasPermissionLevel(4) }.then(argument("player", EntityArgumentType.player()).executes { ctx ->
					val player = EntityArgumentType.getPlayer(ctx, "player")
					getEngine(ctx).DM = player.uuid
					reply(ctx, "Set DM to ${player.name}")
					1
				})).executes { ctx ->
					val player = ctx.source.server.playerManager.getPlayer(getEngine(ctx).DM)

					if(player == null) reply(ctx, "No DM currently set")
					else reply(ctx, "Current DM is ${player.name}")
					1
				}
			)
			// CHARACTER COMMANDS
			.then(literal("characters")
				.then(literal("create").executes { ctx ->
					val player = ctx.source.player ?: return@executes 0
					player.openHandledScreen(object : NamedScreenHandlerFactory {
						override fun getDisplayName(): Text = Text.translatable("mtt.screen.character_maker")
						override fun createMenu(syncId: Int, playerInventory: PlayerInventory?, player: PlayerEntity?): ScreenHandler = CharacterMakerScreenHandler(syncId)
					})
					1
				})
				.then(literal("list").executes { ctx ->
					val engine = getEngine(ctx)
					val names = engine.getAllPlayerCharacters().joinToString{ it.name }
					reply(ctx, "Current DND Player Characters: $names")
					1
				})
				.then(argument("character", StringArgumentType.string()).suggests(DNDSuggestions.PLAYER_NAMES)
					.then(literal("controlling")
						// Set the controlling player
						.then(argument("controller", EntityArgumentType.player()).executes { ctx ->
							val queryName = StringArgumentType.getString(ctx, "character")
							val engine = getEngine(ctx)
							val character = engine.getCharacter(queryName) ?: return@executes 0
							val player = EntityArgumentType.getPlayer(ctx, "controller")
							engine.associatePlayer(player.uuid, character.uuid)
							reply(ctx, "DND Character $queryName now being controlled by ${player.displayName?.string}")
							1
						})
						// Get the controlling player
						.executes { ctx ->
							val queryName = StringArgumentType.getString(ctx, "character")
							val engine = getEngine(ctx)
							val character = engine.getCharacter(queryName) ?: return@executes 0
							val uuid = engine.getControlling(character)
							val player = ctx.source.server.playerManager.getPlayer(uuid)
							if(player == null) reply(ctx, "DND Character $queryName is not currently being controlled by any character")
							else reply(ctx, "DND Character $queryName is currently being controlled by ${player.name?.string}")
							1
						}
					)
					// Open the inventory of the character
					.then(literal("inventory").executes { ctx ->
						val player = ctx.source.player ?: return@executes 0
						val queryName = StringArgumentType.getString(ctx, "character")
						val character = getEngine(ctx).getCharacter(queryName)

						if(character != null){
							player.openHandledScreen(character)
							1
						}
						else 0
					})
					// Give the character an item
					.then(literal("give").then(argument("item", ItemStackArgumentType(access)).executes { ctx ->
						val queryName = StringArgumentType.getString(ctx, "character")
						val character = getEngine(ctx).getCharacter(queryName)
						if(character == null) return@executes 0

						val stack = ItemStackArgumentType.getItemStackArgument(ctx, "item").createStack(1, false)
						character.addItemStack(stack)
						1
					}))
					// Delete the character
					.then(literal("delete").executes { ctx ->
						val queryName = StringArgumentType.getString(ctx, "character")
						if(getEngine(ctx).removeCharacter(queryName)) {
							reply(ctx, "Removed DND character $queryName")
						}
						else reply(ctx, "There is no DND Character with the name $queryName to delete")
						1
					})
				)
				.then(literal("show").executes { ctx ->
					getEngine(ctx).showCharacters()
					1
				})
				.then(literal("kill").executes { ctx ->
					getEngine(ctx).killCharacters()
					1
				})
			)
			.then(literal("test").executes { ctx ->
				val engine = getEngine(ctx)
				val player = ctx.source.player ?: return@executes 0

				engine.running = true
				val character = SheetCharacter(CharacterSheet(), UUID.randomUUID())
				engine.addCharacter(character)
				engine.associatePlayer(player.uuid, character.uuid)
				1
			})

		fun registerCommands(){
			registerArgumentTypes()
			CommandRegistrationCallback.EVENT.register { dispatcher, access, _ ->
				val roll = dispatcher.register(DICE_COMMAND)
				dispatcher.register(literal("r").redirect(roll))
				dispatcher.register(TEST_COMMAND)
				dispatcher.register(DND_COMMANDS(access))
			}
		}
	}
}