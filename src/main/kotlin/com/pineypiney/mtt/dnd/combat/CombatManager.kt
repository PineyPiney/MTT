package com.pineypiney.mtt.dnd.combat

import com.pineypiney.mtt.dnd.DNDEngine
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import com.pineypiney.mtt.network.payloads.s2c.EnterCombatS2CPayload
import io.netty.buffer.ByteBuf
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.util.math.Vec3d
import java.util.*
import kotlin.jvm.optionals.getOrElse

class CombatManager(val id: Int, val engine: DNDEngine<*>) {

	private val combatants = mutableListOf<Combatant>()
	private var current: Int = 0

	fun enterCharacter(character: Character) {
		val initiative = character.rollInitiative()
		combatants.add(Combatant(character, initiative))
		sort()
		engine.onCharactersEnterCombat(this, mapOf(character to initiative))
	}

	fun enterCharacters(characters: Collection<Character>) {
		val combatants = characters.associateWith(Character::rollInitiative)
		enterCharacters(combatants)
	}

	fun enterCharacters(combatants: Map<out Character, Int>) {
		for ((character, init) in combatants) this.combatants.add(Combatant(character, init))
		sort()
		engine.onCharactersEnterCombat(this, combatants)
	}

	fun exitCharacter(character: Character) {
		combatants.removeAll { it.character == character }
		engine.onCharactersExitCombat(this, listOf(character.uuid))
	}

	fun exitCharacters(characters: Collection<UUID>) {
		var i = 0
		val list = characters.toMutableList()
		while (i < combatants.size) {
			val uuid = combatants[i].character.uuid
			val listIndex = list.indexOf(uuid)
			if (listIndex != -1) {
				combatants.removeAt(i)
				list.removeAt(listIndex)
				if (list.isEmpty()) break
			} else i++
		}
		engine.onCharactersExitCombat(this, characters.filterNot { it in list })
	}

	fun sort() {
		val combatant = getCurrentCombatant()
		combatants.sortDescending()
		if (combatant != null) current = combatants.indexOf(combatant)
	}

	fun getCurrentCombatant(): Combatant? {
		return if (combatants.isEmpty()) null else combatants[current]
	}

	fun containsCharacter(character: Character): Boolean = combatants.any { it.character == character }

	fun getCharacterAt(pos: Vec3d) = combatants.firstOrNull { it.character.pos == pos }?.character

	fun forEachCharacter(action: (Character) -> Unit) {
		combatants.forEach { action(it.character) }
	}

	/**
	 * @return The combat that was consumed by the other so it can be removed from the engine
	 */
	fun merge(other: CombatManager): CombatManager {
		val initiative = getCurrentCombatant()?.initiative ?: -1
		val otherInitiative = other.getCurrentCombatant()?.initiative ?: -1
		return if (otherInitiative < initiative) {
			consume(other)
			other
		} else {
			other.consume(this)
			this
		}
	}

	fun consume(other: CombatManager) {
		combatants.addAll(other.combatants)
		combatants.sortDescending()
		engine.onCharactersEnterCombat(this, other.combatants.associate { (char, init) -> char to init })
		engine.onCharactersExitCombat(other, other.combatants.map { it.character.uuid })
		other.combatants.clear()
		engine.combats.remove(other)
	}

	fun isEmpty() = combatants.isEmpty()

	fun asPayload() = EnterCombatS2CPayload(id, combatants.associate { (char, init) -> char.uuid to init })

	fun writeNbt(nbt: NbtCompound) {
		val combatants = NbtList()
		nbt.put("combatants", combatants)
		for (c in this.combatants) {
			val combatant = NbtCompound()
			combatants.add(combatants)
			c.writeNbt(combatant)
		}
	}

	fun readNbt(nbt: NbtCompound, engine: DNDEngine<*>) {
		for (bNbt in nbt.getListOrEmpty("combatants")) {
			readCombatantNbt(bNbt as NbtCompound, engine)
		}
	}

	fun readCombatantNbt(nbt: NbtCompound, engine: DNDEngine<*>) {
		nbt.getLongArray("uuid").getOrElse { return }
		nbt.getIntArray("init").getOrElse { return }
	}

	fun encode(buf: ByteBuf) {
		MTTPacketCodecs.shtInt.encode(buf, combatants.size)
		for (combatant in combatants) {
			MTTPacketCodecs.UUID_CODEC.encode(buf, combatant.character.uuid)
			MTTPacketCodecs.bytInt.encode(buf, combatant.initiative)
		}
	}

	fun decode(buf: ByteBuf, engine: DNDEngine<*>) {
		combatants.clear()
		repeat(MTTPacketCodecs.shtInt.decode(buf)) {
			val uuid = MTTPacketCodecs.UUID_CODEC.decode(buf)
			val init = MTTPacketCodecs.bytInt.decode(buf)
			val character = engine.getCharacter(uuid)
			if (character != null) combatants.add(Combatant(character, init))
		}
		engine.onCharactersEnterCombat(this, combatants.associate { it.character to it.initiative })
	}

	data class Combatant(val character: Character, val initiative: Int) : Comparable<Combatant> {
		fun writeNbt(nbt: NbtCompound) {
			nbt.putLongArray("uuid", longArrayOf(character.uuid.mostSignificantBits, character.uuid.leastSignificantBits))
			nbt.putInt("init", initiative)
		}

		override fun compareTo(other: Combatant): Int {
			val initDiff = initiative - other.initiative
			return if (initDiff != 0) initDiff
			else character.abilities.dexMod - other.character.abilities.dexMod
		}
	}
}