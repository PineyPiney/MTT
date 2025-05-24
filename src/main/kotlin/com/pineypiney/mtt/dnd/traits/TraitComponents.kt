package com.pineypiney.mtt.dnd.traits

import com.pineypiney.mtt.dnd.CreatureType
import com.pineypiney.mtt.dnd.Size
import com.pineypiney.mtt.dnd.stats.Ability
import io.netty.buffer.ByteBuf
import kotlinx.serialization.json.*
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.text.Text

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
fun <T> getJsonTrait(json: JsonElement, shortCuts: Map<String, List<T>> = emptyMap(), getter: (JsonPrimitive) -> T?): List<Trait<T>>{
	return when(json){
		is JsonPrimitive -> listOf(SetTraits(getter(json) ?: return listOf()))
		is JsonObject -> {
			val numChoices = json["choices"]?.jsonPrimitive?.intOrNull ?: 1
			val entryStrings = json["options"]
			val options = mutableListOf<T>()
			if(entryStrings is JsonArray){
				for(entry in entryStrings) {
					val shortcutValues = shortCuts[entry.jsonPrimitive.content]
					if(shortcutValues == null) options.add(getter(entry.jsonPrimitive) ?: continue)
					else options.addAll(shortcutValues)
				}
			}
			else if(entryStrings is JsonPrimitive){
				val shortcutValues = shortCuts[entryStrings.content]
				val value = getter(entryStrings)
				if(shortcutValues != null) options.addAll(shortcutValues)
				else if(value != null) options.add(value)
			}
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

class CreatureTypeComponent(val type: Trait<CreatureType>): TraitComponent<CreatureType, CreatureTypeComponent>("trait"){
	override fun getCodec(): TraitCodec<CreatureTypeComponent> = TraitCodec.TYPE_CODEC
	override fun getTranslationKey(value: CreatureType): String = "mtt.creature_type.${value.name.lowercase()}"
	override fun getLines(): List<Any> = listOf(type, getDescription())
}
class SizeComponent(val size: Trait<Size>): TraitComponent<Size, SizeComponent>("trait"){
	override fun getCodec(): TraitCodec<SizeComponent> = TraitCodec.SIZE_CODEC
	override fun getTranslationKey(value: Size): String = "mtt.size.${value.name}"
	override fun getLines(): List<Any> = listOf(size, getDescription())
}
class SpeedComponent(val speed: Trait<Int>): TraitComponent<Int, SpeedComponent>("trait"){
	override fun getCodec(): TraitCodec<SpeedComponent> = TraitCodec.SPEED_CODEC
	override fun getLines(): List<Any> = listOf(speed, getDescription())
}
class ModelComponent(val model: Trait<String>): TraitComponent<String, ModelComponent>("trait"){
	override fun getCodec(): TraitCodec<ModelComponent> = TraitCodec.MODEL_CODEC
	override fun getLines(): List<Any> = listOf(model, getDescription())
}
class LanguageComponent(val languages: Trait<String>): TraitComponent<String, LanguageComponent>("trait"){
	override fun getCodec(): TraitCodec<LanguageComponent> = TraitCodec.LANGUAGE_CODEC
	override fun getLines(): List<Any> = listOf(languages)
}
class DarkVisionComponent(val darkVision: Int): TraitComponent<Int, DarkVisionComponent>(){
	override fun getCodec(): TraitCodec<DarkVisionComponent> = TraitCodec.DARK_VISION_CODEC
	override fun getLines(): List<Any> = listOf(Text.translatable("mtt.feature.dark_vision.declaration", darkVision), getDescription())
}
class HealthBonusComponent(val base: Int, val perLevel: Int): TraitComponent<Int, HealthBonusComponent>(){
	override fun getCodec(): TraitCodec<HealthBonusComponent> = TraitCodec.HEALTH_BONUS_CODEC
	override fun getLines(): List<Any> {
		val list = mutableListOf<Any>()
		if(base > 0) list.add(Text.translatable("mtt.feature.bonus_health.base.declaration", base))
		if(perLevel > 0) list.add(Text.translatable("mtt.feature.bonus_health.par_level.declaration", perLevel))
		return list
	}
}
class AdvantageComponent(val type: String, val advantages: Trait<String>): TraitComponent<String, AdvantageComponent>(){
	override fun getCodec(): TraitCodec<AdvantageComponent> = TraitCodec.ADVANTAGE_CODEC
	override fun getLines(): List<Any> = listOf(advantages, getDescription())
}
class ResistanceComponent(val type: String, val name: Trait<String>): TraitComponent<String, ResistanceComponent>(){
	override fun getCodec(): TraitCodec<ResistanceComponent> = TraitCodec.RESISTANCE_CODEC
	override fun getLines(): List<Any> = listOf(name, getDescription())
}
class ProficiencyComponent(val type: String, val name: Trait<String>): TraitComponent<String, ProficiencyComponent>(){
	override fun getCodec(): TraitCodec<ProficiencyComponent> = TraitCodec.PROFICIENCY_CODEC
	override fun getTranslationKey(value: String): String = "mtt.$type.$value"
	override fun getLines(): List<Any> = listOf(name, getDescription())
}
class CustomTraitComponent(val id: String): TraitComponent<String, CustomTraitComponent>(){
	override fun getCodec(): TraitCodec<CustomTraitComponent> = TraitCodec.CUSTOM_TRAIT_CODEC
	override fun getID(): String = id
	override fun getLines(): List<Any> = listOf(getDescription())
}
class SpellcastingAbilityComponent(val ability: Trait<Ability>): TraitComponent<Ability, SpellcastingAbilityComponent>(){
	override fun getCodec(): TraitCodec<SpellcastingAbilityComponent> = TraitCodec.SPELLCASTING_ABILITY_CODEC
	override fun getTranslationKey(value: Ability): String = "mtt.ability.${value.name.lowercase()}"
	override fun getLines(): List<Any> = listOf(ability)
}
class SpellComponent(val unlockLevel: Int, val spell: Trait<String>): TraitComponent<String, SpellComponent>(){
	override fun getCodec(): TraitCodec<SpellComponent> = TraitCodec.SPELL_CODEC
	override fun getLines(): List<Any> = listOf(spell)
}
class FeatComponent(val feat: Trait<String>): TraitComponent<String, FeatComponent>(){
	override fun getCodec(): TraitCodec<FeatComponent> = TraitCodec.FEAT_CODEC
	override fun getLines(): List<Any> = listOf(feat, getDescription())
}
class SubspeciesIDComponent(val type: Trait<String>): TraitComponent<String, SubspeciesIDComponent>(){
	override fun getCodec(): TraitCodec<SubspeciesIDComponent> = TraitCodec.SUBSPECIES_ID_CODEC
	override fun getLines(): List<Any> = listOf()
}