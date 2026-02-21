package com.pineypiney.mtt.dnd.characters

import com.pineypiney.mtt.dnd.DNDEngine
import com.pineypiney.mtt.dnd.classes.DNDClass
import com.pineypiney.mtt.dnd.conditions.Condition
import com.pineypiney.mtt.dnd.conditions.ConditionManager
import com.pineypiney.mtt.dnd.network.ServerDNDEntity
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.dnd.race.Subrace
import com.pineypiney.mtt.dnd.spells.Spell
import com.pineypiney.mtt.dnd.traits.*
import com.pineypiney.mtt.dnd.traits.proficiencies.EquipmentType
import com.pineypiney.mtt.dnd.traits.proficiencies.Proficiency
import com.pineypiney.mtt.entity.DNDEntity
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import com.pineypiney.mtt.network.payloads.s2c.CharacterParamsS2CPayload
import io.netty.buffer.ByteBuf
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import java.util.*

class SimpleCharacter(val details: Params, uuid: UUID, engine: DNDEngine) : Character(uuid, engine) {

	override var name: String
		get() = details.name
		set(value) {
			details.name = value
		}
	override val race: Race = details.race
	override val type: CreatureType = race.type
	override val size: Size = race.size.options.first()
	override val speed: Int = race.speed
	override val model: CharacterModel = race.getModel(details.modelId)
	override var health: Int
		get() = details.health
		set(value) {
			details.health = value
		}
	override val maxHealth: Int = details.maxHealth
	override val abilities: Abilities = details.abilities
	override var baseArmourClass: Int
		get() = details.armourClass
		set(value) {
			details.armourClass = value
		}

	val proficiencies = mutableSetOf<Proficiency>()
	override val conditions: ConditionManager = ConditionManager(this, details.conditions)

	init {
		for (trait in details.dndClass.coreTraits) {
			if (trait is ProficiencyTrait) proficiencies.addAll(trait.data.given)
		}
		for (trait in race.traits) {
			if (trait is ProficiencyTrait) proficiencies.addAll(trait.data.given)
		}
		for (namedTrait in race.namedTraits) {
			for (trait in namedTrait.traits) {
				if (trait is ProficiencyTrait) proficiencies.addAll(trait.data.given)
			}
		}
		if (details.subrace != null) {
			for (trait in details.subrace.traits) {
				if (trait is ProficiencyTrait) proficiencies.addAll(trait.data.given)
			}
			for (namedTrait in details.subrace.namedTraits) {
				for (trait in namedTrait.traits) {
					if (trait is ProficiencyTrait) proficiencies.addAll(trait.data.given)
				}
			}
		}
	}

	override fun createEntity(world: World): DNDEntity {
		val entity = ServerDNDEntity(world, this)
		return entity
	}

	override fun createPayload(regManager: DynamicRegistryManager): CustomPayload {
		val nbt = NbtCompound()
		writeNbt(nbt, regManager)
		return CharacterParamsS2CPayload(uuid, details, nbt)
	}

	override fun getLevel(): Int = details.level

	override fun getPreparedSpells(): Set<Spell> = details.spells

	override fun isProficientIn(equipment: EquipmentType): Boolean =
		proficiencies.contains(Proficiency.findById(equipment.id))

	override fun isProficientIn(ability: Ability): Boolean =
		proficiencies.contains(Proficiency.findById(ability.id))

	override fun getProficiencyBonus(): Int = MathHelper.ceilDiv(details.level, 4) + 1

	override fun toString(): String {
		return "$name($race, ${details.dndClass} ${details.level})"
	}

	class Params(
		var name: String,
		val race: Race,
		val subrace: Subrace?,
		val modelId: String,
		val dndClass: DNDClass,
		val level: Int,
		val maxHealth: Int,
		val spells: Set<Spell>
	) {
		val abilities = Abilities()
		var health = maxHealth
		var armourClass = 10
		val conditions = SourceMap<Condition.ConditionState<*>>()

		companion object {
			val PACKET_CODEC = object : PacketCodec<ByteBuf, Params> {
				override fun decode(buf: ByteBuf): Params {
					val name = PacketCodecs.STRING.decode(buf)
					val race = Race.findById(PacketCodecs.STRING.decode(buf))
					val subrace = race.getSubrace(PacketCodecs.STRING.decode(buf))
					val modelId = PacketCodecs.STRING.decode(buf)
					val dndClass = DNDClass.findById(PacketCodecs.STRING.decode(buf))
					val level = MTTPacketCodecs.bytInt.decode(buf)
					val maxHealth = MTTPacketCodecs.shtInt.decode(buf)
					val spells = MTTPacketCodecs.SPELLS.decode(buf)
					val value = Params(name, race, subrace, modelId, dndClass, level, maxHealth, spells)

					value.abilities.decode(buf)
					value.armourClass = MTTPacketCodecs.bytInt.decode(buf)
					value.health = MTTPacketCodecs.shtInt.decode(buf)
					value.conditions.decode(buf, MTTPacketCodecs.smallCollection(MTTPacketCodecs.CONDITION, ::mutableSetOf))

					return value
				}

				override fun encode(buf: ByteBuf, value: Params) {
					PacketCodecs.STRING.encode(buf, value.name)
					PacketCodecs.STRING.encode(buf, value.race.id)
					PacketCodecs.STRING.encode(buf, value.subrace?.name ?: "")
					PacketCodecs.STRING.encode(buf, value.modelId)
					PacketCodecs.STRING.encode(buf, value.dndClass.id)
					MTTPacketCodecs.bytInt.encode(buf, value.level)
					MTTPacketCodecs.shtInt.encode(buf, value.maxHealth)
					MTTPacketCodecs.SPELLS.encode(buf, value.spells)

					value.abilities.encode(buf)
					MTTPacketCodecs.bytInt.encode(buf, value.armourClass)
					MTTPacketCodecs.shtInt.encode(buf, value.health)
					value.conditions.encode(buf, MTTPacketCodecs.smallCollection(MTTPacketCodecs.CONDITION, ::mutableSetOf))
				}

			}
		}
	}
}