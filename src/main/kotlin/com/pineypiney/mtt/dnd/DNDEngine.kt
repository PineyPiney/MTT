package com.pineypiney.mtt.dnd

import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.characters.SheetCharacter
import com.pineypiney.mtt.entity.DNDPlayerEntity
import net.minecraft.network.packet.CustomPayload
import java.util.*

abstract class DNDEngine {

	open var running: Boolean = false
	open var DM: UUID? = null

	protected val characters = mutableListOf<Character>()

	// Maps players UUIDs to characters UUIDs
	protected val playerCharacters = mutableMapOf<UUID, UUID>()

	abstract val playerEntities: List<DNDPlayerEntity>

	open fun addCharacter(character: Character){
		characters.add(character)
	}
	fun getCharacter(name: String): Character? = characters.firstOrNull { it.name == name }
	fun getCharacter(uuid: UUID): Character? = characters.firstOrNull { it.uuid == uuid }
	fun getPlayerCharacter(player: UUID): SheetCharacter? {
		val characterUUID = playerCharacters[player] ?: return null
		return getCharacter(characterUUID) as? SheetCharacter
	}

	open fun associatePlayer(player: UUID, character: UUID){
		playerCharacters[player] = character
	}
	fun getControlling(character: Character): UUID?{
		return playerCharacters.entries.firstOrNull { it.value == character.uuid }?.key
	}
	open fun dissociatePlayer(player: UUID){
		playerCharacters.remove(player)
	}

	fun getAllPlayerCharacters() = playerCharacters.mapNotNull { getPlayerCharacter(it.key) }

	fun isInCombat(character: Character) = false

	fun tick(){

	}

	val updates = mutableListOf<CustomPayload>()
}