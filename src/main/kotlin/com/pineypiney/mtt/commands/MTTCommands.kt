package com.pineypiney.mtt.commands

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.pineypiney.mtt.commands.suggestions.DNDSuggestions
import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.DNDServerEngine
import com.pineypiney.mtt.dnd.characters.SheetCharacter
import com.pineypiney.mtt.dnd.classes.Barbarian
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder
import com.pineypiney.mtt.network.payloads.s2c.DNDEngineUpdateS2CPayload
import com.pineypiney.mtt.screen.CharacterMakerScreenHandler
import com.pineypiney.mtt.util.toInts
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

object MTTCommands {

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
				// Set if the game is running
				.then(argument("isRunning", BoolArgumentType.bool()).executes { ctx ->
						val engine = getEngine(ctx)
						engine.running = BoolArgumentType.getBool(ctx, "isRunning")
						reply(ctx, "DND Game Running set to ${engine.running}")
						1
					}
				)
				// Query if the game is running
				.executes { ctx ->
					val engine = getEngine(ctx)
					reply(ctx, "DND Game Running is ${engine.running}")
					1
				}
			)
		)
		// DM COMMANDS
		.then(literal("dm")
			// Set the DM of the game
			.then(argument("player", EntityArgumentType.player()).requires{ source -> source.hasPermissionLevel(4) }.executes { ctx ->
				val player = EntityArgumentType.getPlayer(ctx, "player")
				getEngine(ctx).DM = player.uuid
				reply(ctx, "Set DM to ${player.name}")
				1
			})
			// Query the current DM
			.executes { ctx ->
				val player = ctx.source.server.playerManager.getPlayer(getEngine(ctx).DM)

				if(player == null) reply(ctx, "No DM currently set")
				else reply(ctx, "Current DM is ${player.name}")
				1
			}
		)
		// CHARACTER COMMANDS
		.then(literal("characters")
			// Open the character creation screen
			.then(literal("create").executes { ctx ->
				val player = ctx.source.player ?: return@executes 0
				player.openHandledScreen(object : NamedScreenHandlerFactory {
					override fun getDisplayName(): Text = Text.translatable("mtt.screen.character_maker")
					override fun createMenu(syncId: Int, playerInventory: PlayerInventory?, player: PlayerEntity?): ScreenHandler = CharacterMakerScreenHandler(syncId)
				})
				1
			})
			// List all playable characters
			.then(literal("list").executes { ctx ->
				val engine = getEngine(ctx)
				val names = engine.getAllPlayerCharacters().joinToString{ it.name }
				reply(ctx, "Current DND Player Characters: $names")
				1
			})
			.then(argument("character", StringArgumentType.string()).suggests(DNDSuggestions.ALL_NAMES)
				.then(literal("controlling")
					// Stop character from being controlled by any player
					.then(literal("None").executes { ctx ->
						val engine = getEngine(ctx)
						val character = DNDSuggestions.getCharacter(ctx, "character") ?: return@executes 0
						val playerUUID = engine.getControlling(character) ?: let {
							reply(ctx, "DND Character ${character.name}, was already not being controlled by any player")
							return@executes 0
						}
						engine.dissociatePlayer(playerUUID)
						reply(ctx, "DND Character ${character.name} now not being controlled by any player")
						1
					})
					// Set the controlling player
					.then(argument("controller", EntityArgumentType.player()).executes { ctx ->
						val engine = getEngine(ctx)
						val character = DNDSuggestions.getCharacter(ctx, "character") ?: return@executes 0
						val player = EntityArgumentType.getPlayer(ctx, "controller")
						engine.associatePlayer(player.uuid, character.uuid)
						reply(ctx, "DND Character ${character.name} now being controlled by ${player.displayName?.string}")
						1
					})
					// Get the controlling player
					.executes { ctx ->
						val engine = getEngine(ctx)
						val character = DNDSuggestions.getCharacter(ctx, "character") ?: return@executes 0
						val uuid = engine.getControlling(character)
						val player = ctx.source.server.playerManager.getPlayer(uuid)
						if(player == null) reply(ctx, "DND Character ${character.name} is not currently being controlled by any character")
						else reply(ctx, "DND Character ${character.name} is currently being controlled by ${player.name?.string}")
						1
					}
				)
				// Open the inventory of the character
				.then(literal("inventory").executes { ctx ->
					val player = ctx.source.player ?: return@executes 0
					val character = DNDSuggestions.getCharacter(ctx, "character") ?: return@executes 0

					player.openHandledScreen(character)
					1
				})
				// Give the character an item
				.then(literal("give").then(argument("item", ItemStackArgumentType(access)).executes { ctx ->
					val character = DNDSuggestions.getCharacter(ctx, "character") ?: return@executes 0

					val stack = ItemStackArgumentType.getItemStackArgument(ctx, "item").createStack(1, false)
					character.addItemStack(stack)
					1
				}))
				// Rename the character
				.then(literal("rename").then(argument("name", StringArgumentType.string()).executes { ctx ->
					val engine = getEngine(ctx)
					val character = DNDSuggestions.getCharacter(ctx, "character") ?: return@executes 0
					character.name = StringArgumentType.getString(ctx, "name")
					engine.getCharacterEntity(character.uuid)?.name = character.name
					engine.updates.add(DNDEngineUpdateS2CPayload("rename", character.uuid.toInts(), character.name))
					1
				}))
				// Delete the character
				.then(literal("delete").executes { ctx ->
					val character = DNDSuggestions.getCharacter(ctx, "character") ?: return@executes 0
					if(getEngine(ctx).removeCharacter(character.name)) {
						reply(ctx, "Removed DND character ${character.name}")
					}
					else reply(ctx, "There is no DND Character with the name ${character.name} to delete")
					1
				})
			)
			// Spawn entities to show all characters
			.then(literal("show").executes { ctx ->
				getEngine(ctx).showCharacters()
				1
			})
			// Kill all character entities
			.then(literal("kill").executes { ctx ->
				getEngine(ctx).killCharacters()
				1
			})
		)
		// Create a test character controlled by the player who called the command
		.then(literal("test").executes { ctx ->
			val engine = getEngine(ctx)
			val player = ctx.source.player ?: return@executes 0

			engine.running = true
			val sheet = CharacterSheet()
			sheet.name = "Test"
			sheet.race = Race.findById("orc")
			sheet.classes[Barbarian] = 1
			sheet.maxHealth = Barbarian.healthDie
			sheet.health = sheet.maxHealth
			val character = SheetCharacter(sheet, UUID.randomUUID())
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