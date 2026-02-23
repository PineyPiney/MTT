package com.pineypiney.mtt.commands

import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.pineypiney.mtt.commands.arguments.*
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.characters.CharacterSheet
import com.pineypiney.mtt.dnd.classes.Wizard
import com.pineypiney.mtt.dnd.conditions.Condition
import com.pineypiney.mtt.dnd.network.ServerCharacter
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.dnd.server.ServerDNDEngine
import com.pineypiney.mtt.dnd.spells.Spells
import com.pineypiney.mtt.dnd.traits.Source
import com.pineypiney.mtt.mixin_interfaces.MTTServerPlayer
import com.pineypiney.mtt.network.payloads.s2c.DNDEngineUpdateS2CPayload
import com.pineypiney.mtt.screen.CharacterMakerScreenHandler
import com.pineypiney.mtt.util.getEngine
import com.pineypiney.mtt.util.toInts
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.command.argument.Vec3ArgumentType
import net.minecraft.command.permission.Permission
import net.minecraft.command.permission.PermissionLevel
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import java.util.*

object MTTCommands {

	private fun reply(ctx: CommandContext<ServerCommandSource>, message: String, broadcast: Boolean = false) {
		ctx.source.sendFeedback({ Text.literal(message) }, broadcast)
	}

	private val DICE_COMMAND = literal("roll")
		.then(argument("rollFormula", DiceFormulaArgumentType()).executes { ctx ->
			val rollResult = ctx.getArgument("rollFormula", Int::class.java)
			reply(ctx, "Roll Result: $rollResult")
			0
		})

	fun getEngine(ctx: CommandContext<ServerCommandSource>): ServerDNDEngine {
		return ctx.source.server.getEngine()
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
			.then(argument("player", EntityArgumentType.player()).requires { source ->
				source.permissions.hasPermission(Permission.Level(PermissionLevel.OWNERS))
			}.executes { ctx ->
				val player = EntityArgumentType.getPlayer(ctx, "player")
				getEngine(ctx).DM = player.uuid
				reply(ctx, "Set DM to ${player.stringifiedName}")
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
		// Rename the character
		.then(defaultCharacter(literal("rename"), argument("name", StringArgumentType.greedyString())) { ctx, character ->
			val engine = getEngine(ctx)
			character.name = StringArgumentType.getString(ctx, "name")
			engine.getEntityOfCharacter(character.uuid)?.name = character.name
			engine.updates.add(DNDEngineUpdateS2CPayload("rename", character.uuid.toInts(), character.name))
			1
		})
		// Open the inventory of the character
		.then(defaultCharacter(literal("inventory")) { ctx, character ->
			val player = ctx.source.player
			if (player != null) {
				(player as? MTTServerPlayer)?.`mTT$openCharacterInventory`(character)
				1
			} else 0
		})
		.then(defaultCharacter(literal("give"), argument("item", DNDGameItemArgumentType(access))) { ctx, character ->
			val stack = ItemStack(DNDGameItemArgumentType.getItem(ctx, "item"), 1)
			character.addItemStack(stack)
			1
		})
		.then(
			literal("controlling").then(
				argument("character", DNDCharacterArgumentType(true))
					// Stop character from being controlled by any player
					.then(literal("None").executes { ctx ->
						val engine = getEngine(ctx)
						val character = DNDCharacterArgumentType.getPlayableCharacter(ctx, "character")
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
						val character = DNDCharacterArgumentType.getPlayableCharacter(ctx, "character")
						val player = EntityArgumentType.getPlayer(ctx, "controller")
						engine.associatePlayer(player.uuid, character.uuid)
						reply(ctx, "DND Character ${character.name} now being controlled by ${player.displayName?.string}")
						1
					})
					// Get the controlling player
					.executes { ctx ->
						val engine = getEngine(ctx)
						val character = DNDCharacterArgumentType.getPlayableCharacter(ctx, "character")
						val uuid = engine.getControlling(character)
						val player = ctx.source.server.playerManager.getPlayer(uuid)
						if (player == null) reply(ctx, "DND Character ${character.name} is not currently being controlled by any character")
						else reply(ctx, "DND Character ${character.name} is currently being controlled by ${player.name?.string}")
						1
					}
			))
		// Set and query character conditions
		.then(
			literal("condition")
				.then(defaultCharacter(literal("give"), argument("condition", DNDConditionStateArgumentType())) { ctx, character ->
					val DM = getEngine(ctx).DM ?: ctx.source.player?.uuid ?: return@defaultCharacter 0
					val condition = DNDConditionStateArgumentType.getCondition(ctx, "condition")
					character.conditions.apply(Source.DMOverrideSource(DM), condition)
					reply(ctx, "Given ${character.name} $condition")
					1
				}).then(defaultCharacter(literal("query")) { ctx, character ->
					reply(ctx, "${character.name} has the following conditions: " + character.conditions.mapConditions<String>(Condition.ConditionState<*>::toString).joinToString(", "), false)
					1
				}).then(
					defaultCharacter(
						literal("remove"),
						CharacterCommand(literal("all")) { _, character ->
							character.conditions.clear()
							1
						},
						CharacterCommand(
							argument("condition", DNDConditionArgumentType("character")),
							argument("condition", DNDConditionArgumentType(null))
						) { ctx, character ->
							val condition = DNDConditionArgumentType.getCondition(ctx, "condition")
							character.conditions.cleanse(condition)
							1
						}
					))
		)
		.then(defaultCharacter(literal("flee")) { ctx, character ->
			val engine = getEngine(ctx)
			val combat = engine.getCombat(character) ?: return@defaultCharacter 0
			combat.exitCharacter(character)
			1
		})

		// Teleport the character
		.then(defaultCharacter(literal("tp"), argument("location", Vec3ArgumentType.vec3())) { ctx, character ->
			val engine = getEngine(ctx)
			val entity = engine.getEntityOfCharacter(character.uuid)
			val pos = Vec3ArgumentType.getVec3(ctx, "location")
			if (entity == null) character.pos = pos
			else entity.setPosition(pos)
			1
		})

		// Delete the character
		.then(literal("delete").then(argument("character", DNDCharacterArgumentType(false)).executes { ctx ->
			val character = DNDCharacterArgumentType.getCharacter(ctx, "character")
			if (getEngine(ctx).removeCharacter(character.uuid)) {
				reply(ctx, "Removed DND character ${character.name}")
			} else reply(ctx, "There is no DND Character with the name ${character.name} to delete")
			1
		}))

		// Create a test character controlled by the player who called the command
		.then(literal("test").executes { ctx ->
			val engine = getEngine(ctx)
			val player = ctx.source.player ?: return@executes 0

			engine.running = true
			val sheet = CharacterSheet()
			sheet.name = "Test"
			sheet.race = Race.findById("orc")
			sheet.classes[Wizard] = 1
			sheet.maxHealth = Wizard.healthDie
			sheet.health = sheet.maxHealth
			val spells = setOf(Spells.CHILL_TOUCH, Spells.BURNING_HANDS)
			sheet.learnedSpells.addAll(Source.ClassSource(Wizard), spells)
			sheet.preparedSpells.addAll(Source.ClassSource(Wizard), spells)
			val character = ServerCharacter(sheet, UUID.randomUUID(), engine)
			engine.addCharacter(character)
			engine.associatePlayer(player.uuid, character.uuid)
			1
		})

	private fun <T : ArgumentBuilder<ServerCommandSource, T>> defaultCharacter(builder: T, func: (ctx: CommandContext<ServerCommandSource>, character: Character) -> Int): T {
		return builder.then(argument("character", DNDCharacterArgumentType(false)).executes { ctx ->
			val character = DNDCharacterArgumentType.getCharacter(ctx, "character")
			func(ctx, character)
		}).executes { ctx ->
			val player = ctx.source.player ?: return@executes 0
			val character = getEngine(ctx).getCharacterFromPlayer(player.uuid) ?: return@executes 0
			func(ctx, character)
		}
	}

	private fun <T : ArgumentBuilder<ServerCommandSource, T>> defaultCharacter(
		builder: T,
		argument: RequiredArgumentBuilder<ServerCommandSource, *>,
		func: (ctx: CommandContext<ServerCommandSource>, character: Character) -> Int
	): T {
		return builder.then(argument("character", DNDCharacterArgumentType(false)).then(argument.executes { ctx ->
			val character = DNDCharacterArgumentType.getCharacter(ctx, "character")
			func(ctx, character)
		})).then(argument.executes { ctx ->
			val player = ctx.source.player ?: return@executes 0
			val character = getEngine(ctx).getCharacterFromPlayer(player.uuid) ?: return@executes 0
			func(ctx, character)
		})
	}

	private fun <T : ArgumentBuilder<ServerCommandSource, T>> defaultCharacter(builder: T, vararg commands: CharacterCommand): T {
		return builder.then(argument("character", DNDCharacterArgumentType(false)).apply {
			for ((argument, _, func) in commands) {
				then(argument.executes { ctx ->
					val character = DNDCharacterArgumentType.getCharacter(ctx, "character")
					func(ctx, character)
				})
			}
		}).apply {
			for ((_, argument, func) in commands) {
				then(argument.executes { ctx ->
					val player = ctx.source.player ?: return@executes 0
					val character = getEngine(ctx).getCharacterFromPlayer(player.uuid) ?: return@executes 0
					func(ctx, character)
				})
			}
		}
	}

	fun registerCommands(){
		MTTCommandArgumentTypes.registerArgumentTypes()
		CommandRegistrationCallback.EVENT.register { dispatcher, access, _ ->
			val roll = dispatcher.register(DICE_COMMAND)
			dispatcher.register(literal("r").redirect(roll))
			dispatcher.register(DND_COMMANDS(access))
		}
	}

	class CharacterCommand(
		val withCharacterArgument: ArgumentBuilder<ServerCommandSource, *>,
		val withoutCharacterArgument: ArgumentBuilder<ServerCommandSource, *>,
		val func: (ctx: CommandContext<ServerCommandSource>, character: Character) -> Int
	) {
		constructor(
			characterArgument: ArgumentBuilder<ServerCommandSource, *>,
			func: (ctx: CommandContext<ServerCommandSource>, character: Character) -> Int
		) : this(characterArgument, characterArgument, func)

		operator fun component1() = withCharacterArgument
		operator fun component2() = withoutCharacterArgument
		operator fun component3() = func
	}
}