package com.pineypiney.mtt.dnd

import com.pineypiney.mtt.dnd.classes.DNDClass
import com.pineypiney.mtt.dnd.species.Species
import com.pineypiney.mtt.dnd.traits.Abilities
import com.pineypiney.mtt.dnd.traits.CreatureType
import com.pineypiney.mtt.dnd.traits.Size
import com.pineypiney.mtt.dnd.traits.Source
import com.pineypiney.mtt.dnd.traits.features.Feature
import com.pineypiney.mtt.dnd.traits.proficiencies.EquipmentType
import com.pineypiney.mtt.dnd.traits.proficiencies.Proficiency
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import io.netty.buffer.ByteBuf
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.util.math.MathHelper
import kotlin.jvm.optionals.getOrNull

class CharacterSheet {
	var name = "Unnamed Character"
	var species: Species = Species.NONE
	var background: Background = Background.NONE
	var level = 1
	private val typeProperty = Property(CreatureType.HUMANOID){ src, value -> src.overridePower }
	val type get() = typeProperty.getValue()
	private val speedProperty = Property(30){ src, value -> value }
	val speed get() = speedProperty.getValue()
	private val sizeProperty = Property(Size.MEDIUM){ src, value -> src.overridePower }
	val size get() = sizeProperty.getValue()
	var model = "default"
	var maxHealth: Int = 6
	var health = 6
	var armourClass = 10
	var darkVision = 0

	val abilities = Abilities()
	val classes = mutableMapOf<DNDClass, Int>()
	val advantages = mutableMapOf<Source, MutableSet<String>>()
	val resistances = mutableMapOf<Source, MutableSet<String>>()
	val proficiencies = mutableMapOf<Source, MutableSet<Proficiency>>()
	val features = mutableMapOf<Source, MutableSet<Feature>>()

	fun addTypeSource(type: CreatureType, src: Source){
		typeProperty.sources[src] = type
	}
	fun addSizeSource(size: Size, src: Source){
		sizeProperty.sources[src] = size
	}
	fun addSpeedSource(speed: Int, src: Source){
		speedProperty.sources[src] = speed
	}

	fun addLanguage(language: String, src: Source) = addLanguages(listOf(language), src)
	fun addLanguages(languages: List<String>, src: Source){}

	fun addAdvantage(newAdvantage: String, src: Source){
		val current = advantages[src]
		if(current != null) current.add(newAdvantage)
		else advantages[src] = mutableSetOf(newAdvantage)
	}
	fun addAdvantages(newAdvantages: Collection<String>, src: Source){
		val current = advantages[src]
		if(current != null) current.addAll(newAdvantages)
		else advantages[src] = newAdvantages.toMutableSet()
	}
	fun addResistance(newResistance: String, src: Source){
		val current = resistances[src]
		if(current != null) current.add(newResistance)
		else resistances[src] = mutableSetOf(newResistance)
	}
	fun addResistances(newResistances: Collection<String>, src: Source){
		val current = resistances[src]
		if(current != null) current.addAll(newResistances)
		else resistances[src] = newResistances.toMutableSet()
	}
	fun addProficiency(newProficiency: Proficiency, src: Source){
		val current = proficiencies[src]
		if(current != null) current.add(newProficiency)
		else proficiencies[src] = mutableSetOf(newProficiency)
	}
	fun addProficiencies(newProficiencies: Collection<Proficiency>, src: Source){
		val current = proficiencies[src]
		if(current != null) current.addAll(newProficiencies)
		else proficiencies[src] = newProficiencies.toMutableSet()
	}

	fun isProficientIn(equipmentType: EquipmentType): Boolean{
		val proficiency = Proficiency.findById(equipmentType.id)
		return proficiencies.any { it.value.contains(proficiency) }
	}

	fun calculateProficiencyBonus() = MathHelper.ceilDiv(classes.values.sum(), 4) + 1

	fun <T, C> encodePropertyMap(buf: ByteBuf, property: Property<T>, codec: PacketCodec<ByteBuf, C>, get: (T) -> C){
		PacketCodecs.INTEGER.encode(buf, property.sources.size)
		for((src, value) in property.sources){
			MTTPacketCodecs.SOURCE_CODEC.encode(buf, src)
			codec.encode(buf, get(value))
		}
	}

	fun <T, C> decodePropertyMap(buf: ByteBuf, property: Property<T>, codec: PacketCodec<ByteBuf, C>, get: (C) -> T){
		val size = PacketCodecs.INTEGER.decode(buf)
		property.sources.clear()
		for(i in 0..<size){
			val src = MTTPacketCodecs.SOURCE_CODEC.decode(buf)
			val value = get(codec.decode(buf))
			property.sources[src] = value
		}

	}

	fun encodeProperties(buf: ByteBuf){
		encodePropertyMap(buf, typeProperty, PacketCodecs.STRING, CreatureType::name)
		encodePropertyMap(buf, speedProperty, PacketCodecs.INTEGER, Int::toInt)
		encodePropertyMap(buf, sizeProperty, PacketCodecs.STRING, Size::name)
	}

	fun decodeProperties(buf: ByteBuf){
		decodePropertyMap(buf, typeProperty, PacketCodecs.STRING, CreatureType::valueOf)
		decodePropertyMap(buf, speedProperty, PacketCodecs.INTEGER, Int::toInt)
		decodePropertyMap(buf, sizeProperty, PacketCodecs.STRING, Size::fromString)
	}

	fun writeNbt(): NbtCompound {
		val sheetNbt = NbtCompound()
		sheetNbt.putString("name", name)
		sheetNbt.putString("species", species.id)
		sheetNbt.putInt("level", level)
		sheetNbt.putInt("speed", speed)
		sheetNbt.putString("size", size.name)
		sheetNbt.putString("model", model)
		sheetNbt.putInt("darkVision", darkVision)
		return sheetNbt
	}

	fun readNbt(nbt: NbtCompound, engine: DNDServerEngine){
		nbt.getString("name").getOrNull()?.let { name = it }
		nbt.getString("species").getOrNull()?.let{ species = Species.findById(it) }
		nbt.getInt("level").getOrNull()?.let { level = it }
		//nbt.getInt("speed").getOrNull()?.let { speed = it }
		//nbt.getString("size").getOrNull()?.let{ size = Size.Companion.fromString(it) }
		nbt.getString("model").getOrNull()?.let { model = it }
		nbt.getInt("darkVision").getOrNull()?.let { darkVision = it }
	}
}