package com.pineypiney.mtt.dnd.characters

import com.pineypiney.mtt.dnd.Background
import com.pineypiney.mtt.dnd.Property
import com.pineypiney.mtt.dnd.classes.DNDClass
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.dnd.race.Subrace
import com.pineypiney.mtt.dnd.spells.Spell
import com.pineypiney.mtt.dnd.traits.*
import com.pineypiney.mtt.dnd.traits.features.Feature
import com.pineypiney.mtt.dnd.traits.proficiencies.EquipmentType
import com.pineypiney.mtt.dnd.traits.proficiencies.Proficiency
import com.pineypiney.mtt.network.payloads.s2c.CharacterSheetS2CPayload
import io.netty.buffer.ByteBuf
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.util.math.MathHelper
import java.util.*

class CharacterSheet : CharacterDetails() {

	override var name = "Unnamed Character"
	override var race: Race = Race.findById("human")
	override var subrace: Subrace? = null
	var background: Background = Background.ACOLYTE
	override var level = 1
	private val typeProperty = Property(CreatureType.HUMANOID) { src, _ -> src.overridePower }
	override val type get() = typeProperty.getValue()
	private val speedProperty = Property(30) { _, value -> value }
	override val speed get() = speedProperty.getValue()
	private val sizeProperty = Property(Size.MEDIUM) { src, _ -> src.overridePower }
	override val size get() = sizeProperty.getValue()
	override var model: CharacterModel = race.models.first()
	override var maxHealth: Int = 6
	override var health = 6
	override var armourClass = 10
	override var darkVision = 0

	val classes = mutableMapOf<DNDClass, Int>()
	val advantages = SourceMap<String>()
	val resistances = SourceMap<String>()
	val proficiencies = SourceMap<Proficiency>()
	val features = SourceMap<Feature>()
	val learnedSpells = SourceMap<Spell>()
	val preparedSpells = SourceMap<Spell>()

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

	override fun getPreparedSpells(): Set<Spell> {
		return preparedSpells.getAll().toSet()
	}

	override fun getProficiencyBonus(): Int {
		return calculateProficiencyBonus()
	}

	override fun isProficientIn(equipmentType: EquipmentType): Boolean {
		val proficiency = Proficiency.findById(equipmentType.id)
		return proficiencies.any { it.value.contains(proficiency) }
	}

	override fun isProficientIn(ability: Ability): Boolean {
		val proficiency = Proficiency.findById(ability.id)
		return proficiencies.any { it.value.contains(proficiency) }
	}

	override fun createPayload(regManager: DynamicRegistryManager, uuid: UUID, nbt: NbtCompound): CustomPayload {
		return CharacterSheetS2CPayload(uuid, this, nbt)
	}

	fun calculateProficiencyBonus() = MathHelper.ceilDiv(classes.values.sum(), 4) + 1

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

	override fun toString(): String {
		return "$name($race, (${classes.entries.joinToString { (dndClass, level) -> "$dndClass $level" }}))"
	}
}