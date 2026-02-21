package com.pineypiney.mtt.dnd.characters

import com.pineypiney.mtt.dnd.Background
import com.pineypiney.mtt.dnd.Property
import com.pineypiney.mtt.dnd.classes.DNDClass
import com.pineypiney.mtt.dnd.conditions.Condition
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.dnd.race.Subrace
import com.pineypiney.mtt.dnd.spells.Spell
import com.pineypiney.mtt.dnd.traits.*
import com.pineypiney.mtt.dnd.traits.features.Feature
import com.pineypiney.mtt.dnd.traits.proficiencies.EquipmentType
import com.pineypiney.mtt.dnd.traits.proficiencies.Proficiency
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.util.math.MathHelper

class CharacterSheet {
	var name = "Unnamed Character"
	var race: Race = Race.findById("human")
	var subrace: Subrace? = null
	var background: Background = Background.ACOLYTE
	var level = 1
	private val typeProperty = Property(CreatureType.HUMANOID) { src, _ -> src.overridePower }
	val type get() = typeProperty.getValue()
	private val speedProperty = Property(30) { _, value -> value }
	val speed get() = speedProperty.getValue()
	private val sizeProperty = Property(Size.MEDIUM) { src, _ -> src.overridePower }
	val size get() = sizeProperty.getValue()
	var model: CharacterModel = race.models.first()
	var maxHealth: Int = 6
	var health = 6
	var armourClass = 10
	var darkVision = 0

	val abilities = Abilities()
	val classes = mutableMapOf<DNDClass, Int>()
	val advantages = SourceMap<String>()
	val resistances = SourceMap<String>()
	val proficiencies = SourceMap<Proficiency>()
	val features = SourceMap<Feature>()
	val learnedSpells = SourceMap<Spell>()
	val preparedSpells = SourceMap<Spell>()
	val conditions = SourceMap<Condition.ConditionState<*>>()

	fun addTypeSource(type: CreatureType, src: Source) {
		typeProperty.sources[src] = type
	}

	fun addSizeSource(size: Size, src: Source) {
		sizeProperty.sources[src] = size
	}

	fun addSpeedSource(speed: Int, src: Source) {
		speedProperty.sources[src] = speed
	}

	fun addLanguage(language: String, src: Source) = addLanguages(listOf(language), src)
	fun addLanguages(languages: List<String>, src: Source) {}
	fun addAdvantage(newAdvantage: String, src: Source) = advantages.add(src, newAdvantage)
	fun addAdvantages(newAdvantages: Collection<String>, src: Source) = advantages.addAll(src, newAdvantages)
	fun addResistance(newResistance: String, src: Source) = resistances.add(src, newResistance)
	fun addResistances(newResistances: Collection<String>, src: Source) = resistances.addAll(src, newResistances)
	fun addProficiency(newProficiency: Proficiency, src: Source) = proficiencies.add(src, newProficiency)
	fun addProficiencies(newProficiencies: Collection<Proficiency>, src: Source) = proficiencies.addAll(src, newProficiencies)

	fun isProficientIn(equipmentType: EquipmentType): Boolean {
		val proficiency = Proficiency.findById(equipmentType.id)
		return proficiencies.any { it.value.contains(proficiency) }
	}

	fun isProficientIn(ability: Ability): Boolean {
		val proficiency = Proficiency.findById(ability.id)
		return proficiencies.any { it.value.contains(proficiency) }
	}

	fun calculateProficiencyBonus() = MathHelper.ceilDiv(classes.values.sum(), 4) + 1


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

	fun encodeProperties(buf: ByteBuf) {
		encodePropertyMap(buf, typeProperty, PacketCodecs.STRING, CreatureType::name)
		encodePropertyMap(buf, speedProperty, PacketCodecs.INTEGER, Int::toInt)
		encodePropertyMap(buf, sizeProperty, PacketCodecs.STRING, Size::name)
	}

	fun decodeProperties(buf: ByteBuf) {
		decodePropertyMap(buf, typeProperty, PacketCodecs.STRING, CreatureType::valueOf)
		decodePropertyMap(buf, speedProperty, PacketCodecs.INTEGER, Int::toInt)
		decodePropertyMap(buf, sizeProperty, PacketCodecs.STRING, Size::fromString)
	}
}