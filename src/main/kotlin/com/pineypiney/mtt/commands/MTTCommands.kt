package com.pineypiney.mtt.commands

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.pineypiney.mtt.commands.suggestions.DNDSuggestions
import com.pineypiney.mtt.dnd.DNDServerEngine
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
			return (ctx.source.server as DNDEngineHolder<DNDServerEngine>).dndEngine
		}

		private val DM_COMMAND = literal("dm")
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

		private fun PLAYER_COMMAND(access: CommandRegistryAccess) = literal("dnd_player")
			.then(literal("create").then(argument("name", StringArgumentType.string()).executes{ ctx ->
				val engine = getEngine(ctx)
				val name = StringArgumentType.getString(ctx, "name")
				ctx.source.position
				if(!engine.addPlayer(name, ctx.source.world, ctx.source.position)){
					reply(ctx, "Failed to create new DND Player character $name")
				}
				else reply(ctx, "Created new DND Player Character $name")
				1
			})).executes { ctx ->
				val player = ctx.source.player ?: return@executes 0
				player.openHandledScreen(object : NamedScreenHandlerFactory {
					override fun getDisplayName(): Text = Text.translatable("mtt.screen.character_maker")
					override fun createMenu(syncId: Int, playerInventory: PlayerInventory?, player: PlayerEntity?): ScreenHandler = CharacterMakerScreenHandler(syncId)
				})
				1
			}
			.then(literal("list").executes { ctx ->
				val engine = getEngine(ctx)
				val names = engine.getPlayerCharacters().joinToString{ it.customName?.string ?: "Unamed Player" }
				reply(ctx, "Current DND Player Characters: $names")
				1
			})
			.then(argument("dnd_player", StringArgumentType.string()).suggests(DNDSuggestions.PLAYER_NAMES)
				.then(literal("controlling")
					.then(argument("controller", EntityArgumentType.player()).executes { ctx ->
						val query_name = StringArgumentType.getString(ctx, "dnd_player")
						val engine = getEngine(ctx)
						val dnd_player = engine.getPlayerCharacters().firstOrNull { it.name == query_name }
						val player = EntityArgumentType.getPlayer(ctx, "controller")
						dnd_player?.controllingPlayer = player.uuid
						engine.addStringPayload("set controlling", "$query_name;${player.uuid}")
						reply(ctx, "DND Character $query_name now being controlled by ${player.displayName?.string}")
						1
					})
					.executes { ctx ->
						val query_name = StringArgumentType.getString(ctx, "dnd_player")
						val dnd_player = getEngine(ctx).getPlayerCharacters().firstOrNull { it.name == query_name }
						val uuid = dnd_player?.controllingPlayer
						val player = ctx.source.server.playerManager.getPlayer(uuid)
						if(player == null) reply(ctx, "DND Character $query_name is not currently being controlled by any character")
						else reply(ctx, "DND Character $query_name is currently being controlled by ${player.name?.string}")
						1
					}
				)
				.then(literal("inventory").executes { ctx ->
					val player = ctx.source.player ?: return@executes 0
					val queryName = StringArgumentType.getString(ctx, "dnd_player")
					val character = getEngine(ctx).getPlayer(queryName)

					if(character != null){
						player.openHandledScreen(character)
						1
					}
					else 0
				})
				.then(literal("give").then(argument("item", ItemStackArgumentType(access)).executes { ctx ->
					val queryName = StringArgumentType.getString(ctx, "dnd_player")
					val player = getEngine(ctx).getPlayer(queryName)
					if(player == null) return@executes 0

					val stack = ItemStackArgumentType.getItemStackArgument(ctx, "item").createStack(1, false)
					player.addItemStack(stack)
					1
				}))
				.then(literal("delete").executes { ctx ->
					val queryName = StringArgumentType.getString(ctx, "dnd_player")
					if(getEngine(ctx).removePlayer(queryName)) {
						reply(ctx, "Removed DND player character $queryName")
					}
					else reply(ctx, "There is no DND Player Character with the name $queryName to delete")
					1
				})
			)

		private val GAME_COMMANDS = literal("dnd")
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
			.then(literal("test").executes { ctx ->
				val engine = getEngine(ctx)
				val player = ctx.source.player ?: return@executes 0
				engine.running = true
				engine.addPlayer("b", ctx.source.world, ctx.source.position, player.uuid)
				1
			})

		fun registerCommands(){
			registerArgumentTypes()
			CommandRegistrationCallback.EVENT.register { dispatcher, access, _ ->
				val roll = dispatcher.register(DICE_COMMAND)
				dispatcher.register(literal("r").redirect(roll))
				dispatcher.register(DM_COMMAND)
				dispatcher.register(PLAYER_COMMAND(access))
				dispatcher.register(GAME_COMMANDS)

				dispatcher.register(TEST_COMMAND)
			}
		}
	}
}