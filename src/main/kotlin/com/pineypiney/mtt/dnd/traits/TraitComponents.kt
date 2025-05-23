package com.pineypiney.mtt.dnd.traits

import com.pineypiney.mtt.dnd.Size
import com.pineypiney.mtt.dnd.stats.Ability
import io.netty.buffer.ByteBuf
import kotlinx.serialization.json.*
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs

object TraitComponents {

	val list = mutableMapOf<String, TraitCodec<*>>()

	fun register(codec: TraitCodec<*>){
		list[codec.ID] = codec
	}

	init {
		register(TraitCodec.TYPE_CODEC)
		register(TraitCodec.SIZE_CODEC)
		register(TraitCodec.SPEED_CODEC)
		register(TraitCodec.MODEL_CODEC)
		register(TraitCodec.LANGUAGE_CODEC)
		register(TraitCodec.DARK_VISION_CODEC)
		register(TraitCodec.HEALTH_BONUS_CODEC)
		register(TraitCodec.ADVANTAGE_CODEC)
		register(TraitCodec.RESISTANCE_CODEC)
		register(TraitCodec.PROFICIENCY_CODEC)
		register(TraitCodec.CUSTOM_TRAIT_CODEC)
		register(TraitCodec.SPELLCASTING_ABILITY_CODEC)
		register(TraitCodec.SPELL_CODEC)
		register(TraitCodec.FEAT_CODEC)
		register(TraitCodec.SUBSPECIES_ID_CODEC)
	}
}

fun <T, C> decodeTrait(buf: ByteBuf, codec: PacketCodec<ByteBuf, C>, getter: (C) -> T): Trait<T>{
	val numChoices = PacketCodecs.BYTE.decode(buf).toInt()
	val listSize = PacketCodecs.BYTE.decode(buf).toInt()
	val list = List(listSize){ getter(codec.decode(buf)) }
	return if(numChoices == 0) SetTraits(list)
	else TraitOption(numChoices, list)
}
fun <T> decodeTrait(buf: ByteBuf, codec: PacketCodec<ByteBuf, T>): Trait<T>{
	val numChoices = PacketCodecs.BYTE.decode(buf).toInt()
	val listSize = PacketCodecs.BYTE.decode(buf).toInt()
	val list = List(listSize){ codec.decode(buf) }
	return if(numChoices == 0) SetTraits(list)
	else TraitOption(numChoices, list)
}
fun <T> getJsonTrait(json: JsonElement, getter: (JsonPrimitive) -> T?): List<Trait<T>>{
	return when(json){
		is JsonPrimitive -> listOf(SetTraits(getter(json) ?: return listOf()))
		is JsonObject -> {
			val numChoices = json["choices"]?.jsonPrimitive?.intOrNull ?: 1
			val options = json["options"]?.jsonArray?.mapNotNull { getter(it.jsonPrimitive) } ?: listOf()
			listOf(TraitOption(numChoices, options))
		}
		is JsonArray -> {
			val setValues = json.filterIsInstance<JsonPrimitive>().mapNotNull { getter(it) }
			val list: MutableList<Trait<T>> = if(setValues.isEmpty()) mutableListOf() else mutableListOf(SetTraits(setValues))
			for(choice in json.filterIsInstance<JsonObject>()){
				val numChoices = choice["choices"]?.jsonPrimitive?.intOrNull ?: 1
				val options = choice["options"]?.jsonArray?.mapNotNull { getter(it.jsonPrimitive) } ?: listOf()
				list.add(TraitOption(numChoices, options))
			}
			list
		}
	}
}
fun <T, C> encodeTrait(buf: ByteBuf, value: Trait<T>, codec: PacketCodec<ByteBuf, C>, getter: (T) -> C){
	when(value){
		is SetTraits<T> -> {
			PacketCodecs.BYTE.encode(buf, 0)
			PacketCodecs.BYTE.encode(buf, value.values.size.toByte())
			for(entry in value.values) codec.encode(buf, getter(entry))
		}
		is TraitOption<T> -> {
			PacketCodecs.BYTE.encode(buf, value.choices.toByte())
			PacketCodecs.BYTE.encode(buf, value.options.size.toByte())
			for(option in value.options) codec.encode(buf, getter(option))
		}
	}
}
fun <T> encodeTrait(buf: ByteBuf, value: Trait<T>, codec: PacketCodec<ByteBuf, T>){
	when(value){
		is SetTraits<T> -> {
			PacketCodecs.BYTE.encode(buf, 0)
			PacketCodecs.BYTE.encode(buf, value.values.size.toByte())
			for(entry in value.values) codec.encode(buf, entry)
		}
		is TraitOption<T> -> {
			PacketCodecs.BYTE.encode(buf, value.choices.toByte())
			PacketCodecs.BYTE.encode(buf, value.options.size.toByte())
			for(option in value.options) codec.encode(buf, option)
		}
	}
}

class CreatureTypeComponent(val type: Trait<String>): TraitComponent<CreatureTypeComponent>(){
	override fun getCodec(): TraitCodec<CreatureTypeComponent> = TraitCodec.TYPE_CODEC
}
class SizeComponent(val size: Trait<Size>): TraitComponent<SizeComponent>(){
	override fun getCodec(): TraitCodec<SizeComponent> = TraitCodec.SIZE_CODEC
}
class SpeedComponent(val speed: Trait<Int>): TraitComponent<SpeedComponent>(){
	override fun getCodec(): TraitCodec<SpeedComponent> = TraitCodec.SPEED_CODEC
}
class ModelComponent(val model: Trait<String>): TraitComponent<ModelComponent>(){
	override fun getCodec(): TraitCodec<ModelComponent> = TraitCodec.MODEL_CODEC
}
class LanguageComponent(val languages: Trait<String>): TraitComponent<LanguageComponent>(){
	override fun getCodec(): TraitCodec<LanguageComponent> = TraitCodec.LANGUAGE_CODEC
}
class DarkVisionComponent(val darkVision: Trait<Int>): TraitComponent<DarkVisionComponent>(){
	override fun getCodec(): TraitCodec<DarkVisionComponent> = TraitCodec.DARK_VISION_CODEC
}
class HealthBonusComponent(val base: Int, val perLevel: Int): TraitComponent<HealthBonusComponent>(){
	override fun getCodec(): TraitCodec<HealthBonusComponent> = TraitCodec.HEALTH_BONUS_CODEC
}
class AdvantageComponent(val type: String, val advantages: Trait<String>): TraitComponent<AdvantageComponent>(){
	override fun getCodec(): TraitCodec<AdvantageComponent> = TraitCodec.ADVANTAGE_CODEC
}
class ResistanceComponent(val type: String, val name: Trait<String>): TraitComponent<ResistanceComponent>(){
	override fun getCodec(): TraitCodec<ResistanceComponent> = TraitCodec.RESISTANCE_CODEC
}
class ProficiencyComponent(val type: String, val name: Trait<String>): TraitComponent<ProficiencyComponent>(){
	override fun getCodec(): TraitCodec<ProficiencyComponent> = TraitCodec.PROFICIENCY_CODEC
}
class CustomTraitComponent(val name: String, val description: String): TraitComponent<CustomTraitComponent>(){
	override fun getCodec(): TraitCodec<CustomTraitComponent> = TraitCodec.CUSTOM_TRAIT_CODEC
}
class SpellcastingAbilityComponent(val ability: Trait<Ability>): TraitComponent<SpellcastingAbilityComponent>(){
	override fun getCodec(): TraitCodec<SpellcastingAbilityComponent> = TraitCodec.SPELLCASTING_ABILITY_CODEC
}
class SpellComponent(val unlockLevel: Int, val spell: Trait<String>): TraitComponent<SpellComponent>(){
	override fun getCodec(): TraitCodec<SpellComponent> = TraitCodec.SPELL_CODEC
}
class FeatComponent(val feat: Trait<String>): TraitComponent<FeatComponent>(){
	override fun getCodec(): TraitCodec<FeatComponent> = TraitCodec.FEAT_CODEC
}
class SubspeciesIDComponent(val type: Trait<String>): TraitComponent<SubspeciesIDComponent>(){
	override fun getCodec(): TraitCodec<SubspeciesIDComponent> = TraitCodec.SUBSPECIES_ID_CODEC
}