package com.pineypiney.mtt.dnd

import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.characters.SheetCharacter
import com.pineypiney.mtt.dnd.combat.CombatManager
import com.pineypiney.mtt.entity.DNDEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.CustomPayload
import java.util.*

/**
 * Note: When dealing with entity and character UUIDs, there are 3 main names used.
 *
 * Player means the UUID of a [PlayerEntity]
 *
 * Character means the UUID of a [Character]
 *
 * Entity means the UUID of a [com.pineypiney.mtt.entity.DNDEntity]
 */
abstract class DNDEngine {

	open var running: Boolean = false
	open var DM: UUID? = null

	protected val characters = mutableListOf<Character>()

	// Maps players UUIDs to characters UUIDs
	protected val playerCharacters = mutableMapOf<UUID, UUID>()

	val combats = mutableSetOf<CombatManager>()

	abstract val playerEntities: List<DNDEntity>

	open fun addCharacter(character: Character){
		characters.add(character)
	}
	open fun removeCharacter(character: Character) {
		characters.remove(character)
	}
	fun getCharacter(name: String): Character? = characters.firstOrNull { it.name == name }
	fun getCharacter(uuid: UUID): Character? = characters.firstOrNull { it.uuid == uuid }

	fun getCharacterUUIDFromPlayer(player: UUID): UUID? = playerCharacters[player]
	fun getCharacterFromPlayer(player: UUID): SheetCharacter? {
		val characterUUID = playerCharacters[player] ?: return null
		return getCharacter(characterUUID) as? SheetCharacter
	}

	fun getEntityFromPlayer(player: UUID): DNDEntity? {
		val characterUUID = getCharacterUUIDFromPlayer(player) ?: return null
		return playerEntities.firstOrNull { it.character?.uuid == characterUUID }
	}

	fun getEntityOfCharacter(character: UUID) = playerEntities.firstOrNull { it.character?.uuid == character }

	abstract fun getControllingPlayer(character: UUID): PlayerEntity?

	open fun associatePlayer(player: UUID, character: UUID){
		val previous = playerCharacters.entries.firstOrNull { it.value == character }
		if (previous != null) playerCharacters.remove(previous.key)
		playerCharacters[player] = character
	}
	fun getControlling(character: Character): UUID?{
		return playerCharacters.entries.firstOrNull { it.value == character.uuid }?.key
	}
	open fun dissociatePlayer(player: UUID){
		playerCharacters.remove(player)
	}

	fun getAllCharacters() = characters
	fun getAllPlayableCharacters() = characters.filterIsInstance<SheetCharacter>()
	fun getAllPlayerCharacters() = playerCharacters.mapNotNull { getCharacter(it.value) as SheetCharacter }

	fun getCombat(character: Character) = combats.firstOrNull { it.combatants.contains(character) }
	fun isInCombat(character: Character) = combats.any { it.combatants.contains(character) }

	fun tick(){

	}

	open fun isClient(): Boolean = false

	val updates = mutableListOf<CustomPayload>()
}