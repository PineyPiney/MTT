package com.pineypiney.mtt.dnd

import com.pineypiney.mtt.dnd.characters.Character
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
abstract class DNDEngine<C : Character> {

	open var running: Boolean = false
	open var DM: UUID? = null

	protected val characters: MutableList<C> = mutableListOf()

	// Maps players UUIDs to characters UUIDs
	protected val playerCharacters = mutableMapOf<UUID, UUID>()

	val combats = mutableSetOf<CombatManager>()

	abstract val playerEntities: List<DNDEntity>

	open fun addCharacter(character: C) {
		characters.add(character)
	}

	open fun removeCharacter(character: C) {
		characters.remove(character)
	}

	/**
	 * Remove the first character with UUID [uuid] and it's representing entity
	 *
	 * @return If a character was successfully removed
	 */
	fun removeCharacter(uuid: UUID): Boolean {
		val character = getCharacter(uuid) ?: return false
		removeCharacter(character)
		return true
	}

	fun getCharacter(name: String): C? = characters.firstOrNull { it.name == name }
	fun getCharacter(uuid: UUID): C? = characters.firstOrNull { it.uuid == uuid }

	fun getCharacterUuidFromPlayer(player: UUID): UUID? = playerCharacters[player]
	fun getCharacterFromPlayer(player: UUID): C? {
		val characterUUID = playerCharacters[player] ?: return null
		return getCharacter(characterUUID)
	}

	fun getEntityFromPlayer(player: UUID): DNDEntity? {
		val characterUuid = getCharacterUuidFromPlayer(player) ?: return null
		return playerEntities.firstOrNull { it.character?.uuid == characterUuid }
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
	fun getAllPlayableCharacters() = characters.filter(Character::isPlayable)
	fun getAllPlayerCharacters() = playerCharacters.mapNotNull { getCharacter(it.value) }

	fun startCombat(combatId: Int, initialCharacters: Collection<Character>) {
		val combat = CombatManager(combatId, this)
		combat.enterCharacters(initialCharacters)
		combats.add(combat)
	}

	fun startCombat(combatId: Int, initialCharacters: Map<out Character, Int>) {
		val combat = CombatManager(combatId, this)
		combat.enterCharacters(initialCharacters)
		combats.add(combat)
	}

	open fun onCharactersEnterCombat(manager: CombatManager, characters: Map<out Character, Int>) {}
	open fun onCharactersExitCombat(manager: CombatManager, characters: List<UUID>) {
		if (manager.isEmpty()) combats.remove(manager)
	}

	fun getCombat(character: Character) = combats.firstOrNull { combat -> combat.containsCharacter(character) }
	fun isInCombat(character: Character) = combats.any { combat -> combat.containsCharacter(character) }

	fun tick(){

	}

	fun reset() {
		running = false
		DM = null
		characters.clear()
		playerCharacters.clear()
		combats.clear()
	}

	open fun isClient(): Boolean = false

	val updates = mutableListOf<CustomPayload>()
}