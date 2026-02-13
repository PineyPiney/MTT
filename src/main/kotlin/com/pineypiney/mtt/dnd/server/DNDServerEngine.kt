package com.pineypiney.mtt.dnd.server

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.DNDEngine
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.characters.CharacterTypeRegistry
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.entity.DNDEntity
import com.pineypiney.mtt.entity.MTTEntities
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import com.pineypiney.mtt.network.payloads.s2c.DNDEngineUpdateS2CPayload
import com.pineypiney.mtt.network.payloads.s2c.RaceS2CPayload
import com.pineypiney.mtt.serialisation.MTTCodecs
import com.pineypiney.mtt.util.toInts
import io.netty.buffer.Unpooled
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.packet.CustomPayload
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.WorldSavePath
import java.io.File
import java.util.*
import kotlin.experimental.and
import kotlin.io.path.notExists

class DNDServerEngine(val server: MinecraftServer) : DNDEngine() {

	override var running: Boolean
		get() = super.running
		set(value) {
			super.running = value
			addIntPayload("running", listOf(if (value) 1 else 0))
		}
	var showCharacters = true

	override var DM: UUID?
		get() = super.DM
		set(value) {
			super.DM = value
			addIntPayload("dm", value?.toInts() ?: emptyList())
		}

	override val playerEntities: List<DNDEntity> get() = server.worlds.flatMap { it.getEntitiesByType(MTTEntities.DND_ENTITY) { true } }

	val characterManager = CharacterManager(this)

	init {
		val allRaceFiles = server.resourceManager.findResources("races") { id: Identifier ->
			id.path.endsWith(".json")
		}
		println("Found Races: ${allRaceFiles.keys.joinToString { it.path }}")

		for ((id, resource) in allRaceFiles) {
			try {
				val json = Json.parseToJsonElement(resource.reader.readText()).jsonObject
				val race = Race.parse(json)
				Race.set.add(race)
			} catch (e: Exception) {
				MTT.logger.warn("Couldn't parse race json $id: ${e.message}")
			}
		}
		MTT.logger.info("Successfully loaded ${Race.set.size} DND races: [${Race.set.joinToString { it.id }}]")
	}

	fun onPlayerConnect(player: ServerPlayerEntity) {
		Race.set.forEach { race ->
			val payload = RaceS2CPayload(race)
			ServerPlayNetworking.send(player, payload)
		}

		ServerPlayNetworking.send(player, DNDEngineUpdateS2CPayload("running", listOf(if (running) 1 else 0), ""))
		val dm = DM
		if (dm != null) ServerPlayNetworking.send(player, DNDEngineUpdateS2CPayload("dm", dm))
		else ServerPlayNetworking.send(player, DNDEngineUpdateS2CPayload("dm"))
		for (character in characters) {
			ServerPlayNetworking.send(player, character.createPayload(server.registryManager))
		}
		for ((playerUUID, characterUUID) in playerCharacters) {
			ServerPlayNetworking.send(player, DNDEngineUpdateS2CPayload("player", playerUUID, characterUUID))
		}

		characterManager.connectPlayer(player)
	}

	fun onPlayerDisconnect(player: ServerPlayerEntity) {
		characterManager.disconnectPlayer(player)
	}

	override fun addCharacter(character: Character) {
		super.addCharacter(character)
		sendPayload(character.createPayload(server.registryManager))
		if (showCharacters) createCharacterEntity(character)
	}

	fun createCharacterEntity(character: Character) {
		val world = server.getWorld(character.world) ?: return
		world.spawnEntity(character.createEntity(world))
	}

	override fun getControllingPlayer(character: UUID): PlayerEntity? {
		val playerUUID = playerCharacters.entries.firstOrNull { it.value == character }?.key ?: return null
		return server.playerManager.getPlayer(playerUUID)
	}

	override fun associatePlayer(player: UUID, character: UUID) {
		super.associatePlayer(player, character)
		sendPayload(DNDEngineUpdateS2CPayload("player", player, character))
		characterManager.updateControlling(server.playerManager.getPlayer(player) ?: return)
	}

	override fun dissociatePlayer(player: UUID) {
		super.dissociatePlayer(player)
		sendPayload(DNDEngineUpdateS2CPayload("player", player))
		characterManager.updateControlling(server.playerManager.getPlayer(player) ?: return)
	}

	fun tickServer(server: MinecraftServer) {

		while (!updates.isEmpty()) {
			val payload: CustomPayload = updates.removeFirst()
			sendPayload(payload)
		}

		for (player in playerEntities) {
			player.screenHandler.sendContentUpdates()
		}
	}

	/**
	 * Remove all characters with name [name] and their representing entities
	 *
	 * @return If any characters were successfully removed
	 */
	fun removeCharacter(name: String): Boolean {
		if (!characters.removeAll { it.name == name }) return false

		for (world in server.worlds) {
			world.getEntitiesByType(MTTEntities.DND_ENTITY) { true }.forEach {
				if (it.name == name) it.remove(Entity.RemovalReason.DISCARDED)
			}
		}
		return true
	}

	/**
	 * Remove the first character with UUID [uuid] and it's representing entity
	 *
	 * @return If a character was successfully removed
	 */
	fun removeCharacter(uuid: UUID): Boolean {
		val character = getCharacter(uuid) ?: return false
		characters.remove(character)

		for (world in server.worlds) {
			world.getEntitiesByType(MTTEntities.DND_ENTITY) { true }.forEach {
				if (it.character == character) it.remove(Entity.RemovalReason.DISCARDED)
			}
		}
		return true
	}

	fun addStringPayload(id: String, data: String) {
		updates.add(DNDEngineUpdateS2CPayload(id, emptyList(), data))
	}

	fun addIntPayload(id: String, ints: List<Int>) {
		updates.add(DNDEngineUpdateS2CPayload(id, ints, ""))
	}

	fun sendPayload(payload: CustomPayload) {
		for (player in server.playerManager.playerList) {
			ServerPlayNetworking.send(player, payload)
		}
	}

	fun showCharacters() {
		killCharacters()
		showCharacters = true
		for (character in characters) createCharacterEntity(character)
	}

	fun killCharacters() {
		showCharacters = false
		server.worlds.forEach { world ->
			world.getEntitiesByType(MTTEntities.DND_ENTITY) { true }.forEach { it.kill(world) }
		}
	}

	fun save(suppressLogs: Boolean): Boolean {
		val savePath = server.getSavePath(WorldSavePath.ROOT)
		if (savePath.notExists()) return false

		val mttDir = File(savePath.toString(), "mtt")
		if (!mttDir.exists()) mttDir.mkdir()

		val charactersFile = File(mttDir, "characters.bin")
		if (!charactersFile.exists()) charactersFile.createNewFile()

		val buf = RegistryByteBuf(Unpooled.buffer(), server.registryManager)
		val bools = MTTCodecs.createBoolByte(running, showCharacters)
		buf.writeBytes(byteArrayOf(bools))

		MTTPacketCodecs.int.encode(buf, characters.size)
		for (character in characters) CharacterTypeRegistry.save(character, buf)

		MTTPacketCodecs.encodeMap(buf, playerCharacters, MTTPacketCodecs.UUID_CODEC, MTTPacketCodecs.UUID_CODEC)
		charactersFile.writeBytes(buf.nioBuffer().array())

		return true
	}

	fun load(): Boolean {
		val savePath = server.getSavePath(WorldSavePath.ROOT)
		if (savePath.notExists()) return false

		val mttDir = File(savePath.toString(), "mtt")
		if (!mttDir.exists()) return false

		val charactersFile = File(mttDir, "characters.bin")
		if (charactersFile.exists()) {
			val buf = RegistryByteBuf(Unpooled.buffer(), server.registryManager)
			buf.writeBytes(charactersFile.readBytes())
			if (buf.readableBytes() == 0) return true

			val boolByte = buf.readByte()
			running = boolByte and 1 > 0
			showCharacters = boolByte and 2 > 0

			characters.clear()
			playerCharacters.clear()

			val numCharacters = MTTPacketCodecs.int.decode(buf)
			repeat(numCharacters) {
				val char = CharacterTypeRegistry.load(buf, this)
				if (char != null) characters.add(char)
			}
			MTTPacketCodecs.decodeMap(buf, MTTPacketCodecs.UUID_CODEC, MTTPacketCodecs.UUID_CODEC, playerCharacters)
		}


		return true
	}
}