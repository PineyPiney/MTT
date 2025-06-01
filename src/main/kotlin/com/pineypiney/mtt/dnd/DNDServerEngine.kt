package com.pineypiney.mtt.dnd

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.species.Species
import com.pineypiney.mtt.entity.DNDPlayerEntity
import com.pineypiney.mtt.entity.MTTEntities
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import com.pineypiney.mtt.network.payloads.s2c.CharacterS2CPayload
import com.pineypiney.mtt.network.payloads.s2c.DNDEngineUpdateS2CPayload
import com.pineypiney.mtt.network.payloads.s2c.SpeciesS2CPayload
import com.pineypiney.mtt.serialisation.MTTCodecs
import io.netty.buffer.Unpooled
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.Entity
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.packet.CustomPayload
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.WorldSavePath
import java.io.File
import java.util.*
import kotlin.experimental.and
import kotlin.io.path.notExists

class DNDServerEngine(private val server: MinecraftServer): DNDEngine() {

	override var running: Boolean
		get() = super.running
		set(value) {
			super.running = value
			addStringPayload("running", (if(value) 1.toChar() else 0.toChar()).toString())
		}
	var showCharacters = true

	override var DM: UUID?
		get() = super.DM
		set(value) {
			super.DM = value
			addStringPayload("dm", value.toString())
		}

	override val playerEntities: List<DNDPlayerEntity> get() = server.worlds.flatMap { it.getEntitiesByType(MTTEntities.PLAYER){ true } }

	init {
		val allSpeciesFiles = server.resourceManager.findResources("species"){ id ->
			id.path.endsWith(".json")
		}
		println("Found Species: ${allSpeciesFiles.keys.joinToString{it.path}}")

		for((id, resource) in allSpeciesFiles){
			try {
				val json = Json.parseToJsonElement(resource.reader.readText()).jsonObject
				val species = Species.parse(json)
				Species.set.add(species)
			}
			catch (e: Exception){
				MTT.logger.warn("Couldn't parse species json $id: ${e.message}")
			}
		}
		MTT.logger.info("Successfully loaded ${Species.set.size} DND species: [${Species.set.joinToString{it.id}}]")
	}

	fun onPlayerConnect(player: ServerPlayerEntity){
		Species.set.forEach{ species ->
			val payload = SpeciesS2CPayload(species)
			ServerPlayNetworking.send(player, payload)
		}

		ServerPlayNetworking.send(player, DNDEngineUpdateS2CPayload("running", (if(running) 1.toChar() else 0.toChar()).toString()))
		ServerPlayNetworking.send(player, DNDEngineUpdateS2CPayload("dm", DM?.toString() ?: ""))
		for(character in characters){
			ServerPlayNetworking.send(player, CharacterS2CPayload(character))
		}
		for((playerUUID, characterUUID) in playerCharacters) {
			ServerPlayNetworking.send(player, DNDEngineUpdateS2CPayload("player", "$playerUUID/$characterUUID"))
		}
	}

	override fun addCharacter(character: Character) {
		super.addCharacter(character)
		sendPayload(CharacterS2CPayload(character))
		if(showCharacters) createCharacterEntity(character)
	}

	fun createCharacterEntity(character: Character){
		val world = server.getWorld(character.world) ?: return
		world.spawnEntity(character.createEntity(world))
	}

	override fun associatePlayer(player: UUID, character: UUID) {
		super.associatePlayer(player, character)
		sendPayload(DNDEngineUpdateS2CPayload("player", "$player/$character"))
	}

	override fun dissociatePlayer(player: UUID) {
		super.dissociatePlayer(player)
		sendPayload(DNDEngineUpdateS2CPayload("player", "$player"))
	}

	fun tickServer(server: MinecraftServer){

		while (!updates.isEmpty()) {
			val payload: CustomPayload = updates.removeFirst()
			sendPayload(payload)
		}

		for(player in playerEntities){
			player.screenHandler.sendContentUpdates()
		}
	}

	fun removeCharacter(name: String): Boolean{
		for(character in characters){
			if(character.name == name){
				characters.remove(character)
			}
		}
		for (world in server.worlds) {
			world.getEntitiesByType(MTTEntities.PLAYER){ true }.forEach {
				if(it.name == name) it.remove(Entity.RemovalReason.DISCARDED)
			}
		}
		return false
	}

	fun addStringPayload(id: String, data: String){
		updates.add(DNDEngineUpdateS2CPayload(id, data))
	}

	fun sendPayload(payload: CustomPayload){
		for (player in server.playerManager.playerList) {
			ServerPlayNetworking.send(player, payload)
		}
	}

	fun showCharacters(){
		killCharacters()
		showCharacters = true
		for(character in characters) createCharacterEntity(character)
	}

	fun killCharacters(){
		showCharacters = false
		server.worlds.forEach { world -> world.getEntitiesByType(MTTEntities.PLAYER){ true }.forEach { it.kill(world) } }
	}

	fun save(suppressLogs: Boolean): Boolean{
		val savePath = server.getSavePath(WorldSavePath.ROOT)
		if(savePath.notExists()) return false

		val mttDir = File(savePath.toString(), "mtt")
		if(!mttDir.exists()) mttDir.mkdir()

		val charactersFile = File(mttDir, "characters.bin")
		if(!charactersFile.exists()) charactersFile.createNewFile()

		val buf = RegistryByteBuf(Unpooled.buffer(), server.registryManager)
		val bools = MTTCodecs.createBoolByte(running, showCharacters)
		buf.writeBytes(byteArrayOf(bools))
		MTTPacketCodecs.encodeCollection(buf, characters, MTTPacketCodecs.CHARACTER_CODEC)
		MTTPacketCodecs.encodeMap(buf, playerCharacters, MTTPacketCodecs.UUID_CODEC, MTTPacketCodecs.UUID_CODEC)
		charactersFile.writeBytes(buf.nioBuffer().array())

		return true
	}

	fun load(): Boolean{
		val savePath = server.getSavePath(WorldSavePath.ROOT)
		if(savePath.notExists()) return false

		val mttDir = File(savePath.toString(), "mtt")
		if(!mttDir.exists()) return false

		val charactersFile = File(mttDir, "characters.bin")
		if(charactersFile.exists()) {
			val buf = RegistryByteBuf(Unpooled.buffer(), server.registryManager)
			buf.writeBytes(charactersFile.readBytes())
			if(buf.readableBytes() == 0) return true

			val boolByte = buf.readByte()
			running = boolByte and 1 > 0
			showCharacters = boolByte and 2 > 0

			characters.clear()
			playerCharacters.clear()
			MTTPacketCodecs.decodeCollection(buf, MTTPacketCodecs.CHARACTER_CODEC, characters)
			MTTPacketCodecs.decodeMap(buf, MTTPacketCodecs.UUID_CODEC, MTTPacketCodecs.UUID_CODEC, playerCharacters)
		}


		return true
	}
}