package com.pineypiney.mtt.dnd

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.species.Species
import com.pineypiney.mtt.entity.DNDPlayerEntity
import com.pineypiney.mtt.entity.MTTEntities
import com.pineypiney.mtt.network.payloads.s2c.DNDEngineUpdateS2CPayload
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.Entity
import net.minecraft.network.packet.CustomPayload
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*

class DNDServerEngine(private val server: MinecraftServer): DNDEngine() {

	override var running: Boolean
		get() = super.running
		set(value) {
			super.running = value
			addStringPayload("running", (if(value) 1.toChar() else 0.toChar()).toString())
		}

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

	fun tickServer(server: MinecraftServer){

		while (!updates.isEmpty()) {
			val payload: CustomPayload = updates.removeFirst()
			for (player in server.playerManager.playerList) {
				ServerPlayNetworking.send(player, payload)
			}
		}

		for(player in playerEntities){
			player.screenHandler.sendContentUpdates()
		}
	}

	fun addPlayer(name: String, world: World, position: Vec3d, controlling: UUID? = null): Boolean{
		if(playerEntities.any { it.name == name }){
			return false
		}
		val newEntity = DNDPlayerEntity(MTTEntities.PLAYER, world)
		try{ world.spawnEntity(newEntity) }
		catch (e: Exception){
			e.printStackTrace()
			return false
		}

		newEntity.setPosition(position)
		newEntity.apply { controllingPlayer = controlling; this.name = name }
		return true
	}

	fun removePlayer(name: String): Boolean{
		for(player in playerEntities){
			if (player.name == name){
				player.remove(Entity.RemovalReason.DISCARDED)
				return true
			}
		}
		return false
	}

	fun addStringPayload(id: String, data: String){
		updates.add(DNDEngineUpdateS2CPayload(id, data))
	}
}