package com.pineypiney.mtt.dnd

import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.characters.SheetCharacter
import com.pineypiney.mtt.entity.DNDPlayerEntity
import net.minecraft.network.packet.CustomPayload
import java.util.*

abstract class DNDEngine {

	open var running: Boolean = false
	open var DM: UUID? = null

	val characters = mutableListOf<Character>()
	val playerCharacters = mutableMapOf<UUID, SheetCharacter>()

	abstract val playerEntities: List<DNDPlayerEntity>
	fun getPlayerCharacters() = playerEntities

	fun getPlayer(name: String): DNDPlayerEntity? = playerEntities.firstOrNull { it.name == name }
	fun getPlayer(uuid: UUID): DNDPlayerEntity? = playerEntities.firstOrNull { uuid == (it.controllingPlayer) }

	fun tick(){

	}

	val updates = mutableListOf<CustomPayload>()
}