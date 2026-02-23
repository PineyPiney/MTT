package com.pineypiney.mtt.dnd.characters

import com.pineypiney.mtt.dnd.Property
import com.pineypiney.mtt.dnd.conditions.Condition
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.dnd.race.Subrace
import com.pineypiney.mtt.dnd.spells.Spell
import com.pineypiney.mtt.dnd.traits.*
import com.pineypiney.mtt.dnd.traits.proficiencies.EquipmentType
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import io.netty.buffer.ByteBuf
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.registry.DynamicRegistryManager
import java.util.*

abstract class CharacterDetails {
	abstract var name: String
	abstract val race: Race
	abstract val subrace: Subrace?
	abstract val level: Int
	abstract val type: CreatureType
	abstract val speed: Int
	abstract val size: Size
	abstract var model: CharacterModel
	abstract val maxHealth: Int
	abstract var health: Int
	abstract var armourClass: Int
	abstract var darkVision: Int

	val abilities = Abilities()
	val conditions = SourceMap<Condition.ConditionState<*>>()

	abstract fun getPreparedSpells(): Set<Spell>
	abstract fun getProficiencyBonus(): Int

	abstract fun isProficientIn(equipmentType: EquipmentType): Boolean
	abstract fun isProficientIn(ability: Ability): Boolean

	abstract fun createPayload(regManager: DynamicRegistryManager, uuid: UUID, nbt: NbtCompound): CustomPayload

	fun <T, C> encodePropertyMap(buf: ByteBuf, property: Property<T>, codec: PacketCodec<ByteBuf, C>, get: (T) -> C) {
		MTTPacketCodecs.bytInt.encode(buf, property.sources.size)
		for ((src, value) in property.sources) {
			MTTPacketCodecs.SOURCE.encode(buf, src)
			codec.encode(buf, get(value))
		}
	}

	fun <T, C> decodePropertyMap(buf: ByteBuf, property: Property<T>, codec: PacketCodec<ByteBuf, C>, get: (C) -> T) {
		val size = MTTPacketCodecs.bytInt.decode(buf)
		property.sources.clear()
		repeat(size) {
			val src = MTTPacketCodecs.SOURCE.decode(buf)
			val value = get(codec.decode(buf))
			property.sources[src] = value
		}
	}
}