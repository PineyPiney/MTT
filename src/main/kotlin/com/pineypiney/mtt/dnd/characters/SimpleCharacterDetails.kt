package com.pineypiney.mtt.dnd.characters

import com.pineypiney.mtt.dnd.classes.DNDClass
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.dnd.race.Subrace
import com.pineypiney.mtt.dnd.spells.Spell
import com.pineypiney.mtt.dnd.traits.Ability
import com.pineypiney.mtt.dnd.traits.CreatureType
import com.pineypiney.mtt.dnd.traits.ProficiencyTrait
import com.pineypiney.mtt.dnd.traits.Size
import com.pineypiney.mtt.dnd.traits.proficiencies.EquipmentType
import com.pineypiney.mtt.dnd.traits.proficiencies.Proficiency
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import com.pineypiney.mtt.network.payloads.s2c.CharacterParamsS2CPayload
import io.netty.buffer.ByteBuf
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.util.math.MathHelper
import java.util.*

class SimpleCharacterDetails(
	override var name: String,
	override val race: Race,
	override val subrace: Subrace?,
	val modelId: String,
	val dndClass: DNDClass,
	override val level: Int,
	override val maxHealth: Int,
	val spells: Set<Spell>
) : CharacterDetails() {

	override val type: CreatureType get() = race.type
	override val speed: Int get() = race.speed
	override val size: Size get() = race.size.options.first()
	override var model: CharacterModel = race.models.first()

	override var health = maxHealth
	override var armourClass = 10
	override var darkVision: Int = 0

	val proficiencies = mutableSetOf<Proficiency>()

	init {
		for (trait in dndClass.coreTraits) {
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
		if (subrace != null) {
			for (trait in subrace.traits) {
				if (trait is ProficiencyTrait) proficiencies.addAll(trait.data.given)
			}
			for (namedTrait in subrace.namedTraits) {
				for (trait in namedTrait.traits) {
					if (trait is ProficiencyTrait) proficiencies.addAll(trait.data.given)
				}
			}
		}
	}

	override fun getPreparedSpells(): Set<Spell> = spells

	override fun getProficiencyBonus(): Int = MathHelper.ceilDiv(level, 4) + 1

	override fun isProficientIn(equipmentType: EquipmentType): Boolean =
		proficiencies.contains(Proficiency.findById(equipmentType.id))

	override fun isProficientIn(ability: Ability): Boolean =
		proficiencies.contains(Proficiency.findById(ability.id))

	override fun createPayload(regManager: DynamicRegistryManager, uuid: UUID, nbt: NbtCompound): CustomPayload {
		return CharacterParamsS2CPayload(uuid, this, nbt)
	}

	override fun toString(): String {
		return "$name($race, $dndClass $level)"
	}

	companion object {
		val PACKET_CODEC = object : PacketCodec<ByteBuf, SimpleCharacterDetails> {
			override fun decode(buf: ByteBuf): SimpleCharacterDetails {
				val name = PacketCodecs.STRING.decode(buf)
				val race = Race.findById(PacketCodecs.STRING.decode(buf))
				val subrace = race.getSubrace(PacketCodecs.STRING.decode(buf))
				val modelId = PacketCodecs.STRING.decode(buf)
				val dndClass = DNDClass.findById(PacketCodecs.STRING.decode(buf))
				val level = MTTPacketCodecs.bytInt.decode(buf)
				val maxHealth = MTTPacketCodecs.shtInt.decode(buf)
				val spells = MTTPacketCodecs.SPELLS.decode(buf)
				val value = SimpleCharacterDetails(name, race, subrace, modelId, dndClass, level, maxHealth, spells)

				value.abilities.decode(buf)
				value.armourClass = MTTPacketCodecs.bytInt.decode(buf)
				value.health = MTTPacketCodecs.shtInt.decode(buf)
				value.conditions.decode(buf, MTTPacketCodecs.smallCollection(MTTPacketCodecs.CONDITION, ::mutableSetOf))

				return value
			}

			override fun encode(buf: ByteBuf, value: SimpleCharacterDetails) {
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