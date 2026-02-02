package com.pineypiney.mtt.dnd.traits

import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.traits.feats.Feat
import com.pineypiney.mtt.dnd.traits.proficiencies.Proficiency
import com.pineypiney.mtt.util.Localisation
import net.minecraft.text.Text


object Traits {

	val set = mutableSetOf<TraitCodec<*>>()

	fun getCodec(id: String) = set.firstOrNull { it.ID == id }

	init {
		set.add(TraitCodec.CREATURE_TYPE_CODEC)
		set.add(TraitCodec.SPEED_TRAIT)
		set.add(TraitCodec.SIZE_CODEC)
		set.add(TraitCodec.MODEL_CODEC)
		set.add(TraitCodec.LANGUAGE_CODEC)
		set.add(TraitCodec.DARK_VISION_TRAIT)
		set.add(TraitCodec.ADVANTAGE_CODEC)
		set.add(TraitCodec.RESISTANCE_CODEC)
		set.add(TraitCodec.PROFICIENCY_CODEC)
		set.add(TraitCodec.HEALTH_BONUS_CODEC)
		set.add(TraitCodec.SPELLCASTING_ABILITY_CODEC)
		set.add(TraitCodec.SPELL_CODEC)
		set.add(TraitCodec.ABILITY_IMPROVEMENT_CODEC)
		set.add(TraitCodec.FEAT_CODEC)
		set.add(TraitCodec.CUSTOM_CODEC)
	}
}

/**
 * Used by Race Traits that have initial given values as well as choice values,
 * For example, in 2024 5e default races start of with common as a given language,
 * and 2 choices from the standard languages
 */
data class GivenAndOptions<T>(val given: Set<T>, val numChoices: Int, val options: Set<T>){
	companion object{
		fun <T> empty() = GivenAndOptions<T>(emptySet(), 0, emptySet())
	}

	fun createPart(label: String, translationKey: (T) -> String, decKey: String, parse: (String) -> T, unparse: (T) -> String, single: ApplySingle<T>, collection: ApplyCollection<T>): TraitPart{
		return if(options.isEmpty()){
			val list = given.toList()
			val arg = Localisation.translateList(list, false, translationKey)
			LiteralPart(decKey, arg){ sheet, src -> collection(sheet, given, src)}
		}
		else if(given.isEmpty() && numChoices == 1){
			OneChoicePart(options, Text.translatable(label), parse, unparse, translationKey, decKey, single)
		}
		else {
			GivenAndOptionsPart(this, Text.translatable(label), parse, unparse, translationKey, decKey, collection)
		}
	}
}


class CreatureTypeTrait(val type: CreatureType): Trait<CreatureTypeTrait>("trait") {
	override fun getCodec(): TraitCodec<CreatureTypeTrait> = TraitCodec.CREATURE_TYPE_CODEC
	override fun getParts(): Set<TraitPart> {
		return setOf(LiteralPart(getDeclarationKey(), getTranslation(type.name.lowercase())){ sheet, src -> sheet.addTypeSource(type, src) }, LiteralPart(getDescriptionKey()))
	}
}

class SpeedTrait(val speed: Int): Trait<SpeedTrait>("trait") {
	override fun getCodec(): TraitCodec<SpeedTrait> = TraitCodec.SPEED_TRAIT
	override fun getParts(): Set<TraitPart> {
		return setOf(LiteralPart(getDeclarationKey(), speed){ sheet, src -> sheet.addSpeedSource(speed, src)}, LiteralPart(getDescriptionKey()))
	}
}

class SizeTrait(val options: Set<Size>): Trait<SizeTrait>("trait") {
	override fun getCodec(): TraitCodec<SizeTrait> = TraitCodec.SIZE_CODEC
	override fun getParts(): Set<TraitPart> {
		val declaration = if(options.size == 1) LiteralPart(getDeclarationKey(), getTranslation(options.first().name)){ sheet, src -> sheet.addSizeSource(options.first(), src)}
		else OneChoicePart(options, getLabel(), Size::fromString, Size::name, { getTranslationKey(it.name.lowercase()) }, "mtt.trait.size.declaration", CharacterSheet::addSizeSource)
		return setOf(declaration, LiteralPart(getDescriptionKey()))
	}
}

class ModelTrait(val options: Set<String>): Trait<ModelTrait>("trait") {
	override fun getCodec(): TraitCodec<ModelTrait> = TraitCodec.MODEL_CODEC
	override fun getParts(): Set<TraitPart> {
		val declaration = if(options.size == 1) LiteralPart(getDeclarationKey(), getTranslation(options.first())){ sheet, _ -> sheet.model = options.first()}
		else OneChoicePart(
			options,
			getLabel(),
			{ it },
			{ it },
			{ "mtt.model.$it" },
			"mtt.trait.model.declaration",
			{ sheet, m, _ -> sheet.model = m })
		return setOf(declaration, LiteralPart(getDescriptionKey()))
	}
}

class LanguageTrait(val data: GivenAndOptions<String>): Trait<LanguageTrait>("trait") {
	override fun getCodec(): TraitCodec<LanguageTrait> = TraitCodec.LANGUAGE_CODEC
	override fun getParts(): Set<TraitPart> {
		val translationKey = { it: String -> "mtt.language.$it" }
		val decKey = getDeclarationKey()
		val declaration = if(data.options.isEmpty()){
			val list = data.given.toList()
			val arg = Localisation.translateList(list, false, translationKey)
			LiteralPart(decKey, arg){ sheet, src -> sheet.addLanguages(list, src) }
		}
		else if(data.given.isEmpty() && data.numChoices == 1){
			OneChoicePart(data.options, getLabel(), {it}, {it}, translationKey, decKey, CharacterSheet::addLanguage)
		}
		else {
			GivenAndOptionsPart(data, getLabel(), {it}, {it}, translationKey, decKey, CharacterSheet::addLanguages)
		}

		return setOf(declaration)
	}
}

class DarkVisionTrait(val distance: Int): Trait<DarkVisionTrait>(){
	override fun getCodec(): TraitCodec<DarkVisionTrait> = TraitCodec.DARK_VISION_TRAIT

	override fun getParts(): Set<TraitPart> {
		return setOf(LiteralPart(getDeclarationKey(), distance), LiteralPart(getDescriptionKey()))
	}
}

class AdvantageTrait(val type: String, val data: GivenAndOptions<String>): Trait<AdvantageTrait>() {
	override fun getCodec(): TraitCodec<AdvantageTrait> = TraitCodec.ADVANTAGE_CODEC
	override fun getParts(): Set<TraitPart> {
		val translationKey = { it: String -> "mtt.$type.$it" }
		val decKey = "mtt.feature.advantage.$type.declaration"
		val declaration = data.createPart(getLabelKey(), translationKey, decKey, {it}, {it}, CharacterSheet::addAdvantage, CharacterSheet::addAdvantages)

		return setOf(declaration, LiteralPart(getDescriptionKey()))
	}

	override fun getTranslationKey(value: Any): String = "mtt.$type.$value"
}

class ResistanceTrait(val data: GivenAndOptions<String>): Trait<ResistanceTrait>() {
	override fun getCodec(): TraitCodec<ResistanceTrait> = TraitCodec.RESISTANCE_CODEC
	override fun getParts(): Set<TraitPart> {
		val translationKey = { it: String -> "mtt.damage_type.$it" }
		val decKey = getDeclarationKey()
		val declaration = data.createPart(getLabelKey(), translationKey, decKey, {it}, {it}, CharacterSheet::addResistance, CharacterSheet::addResistances)

		return setOf(declaration, LiteralPart(getDescriptionKey()))
	}
}

class ProficiencyTrait(val type: String, val data: GivenAndOptions<Proficiency>): Trait<ProficiencyTrait>() {

	constructor(type: String, vararg given: Proficiency): this(type, GivenAndOptions(given.toSet(), 0, emptySet()))
	constructor(type: String, choices: Int, vararg options: Proficiency): this(type, GivenAndOptions(emptySet(), choices, options.toSet()))
	constructor(type: String, choices: Int, options: Set<Proficiency>): this(type, GivenAndOptions(emptySet(), choices, options))
	override fun getCodec(): TraitCodec<ProficiencyTrait> = TraitCodec.PROFICIENCY_CODEC
	override fun getParts(): Set<TraitPart> {
		val translationKey = { it: Proficiency -> "mtt.$type.${it.id}" }
		val decKey = getDeclarationKey()
		val declaration = data.createPart(getLabelKey(), translationKey, decKey, Proficiency::findById, Proficiency::id, CharacterSheet::addProficiency, CharacterSheet::addProficiencies)

		return setOf(declaration, LiteralPart(getDescriptionKey()))
	}

	override fun getLabelKey(): String {
		return "mtt.feature.proficiency.$type"
	}

	override fun getTranslationKey(value: Any): String {
		return "mtt.$type.$value"
	}
}

class HealthBonusTrait(val base: Int, val perLevel: Int): Trait<HealthBonusTrait>(){
	override fun getCodec(): TraitCodec<HealthBonusTrait> = TraitCodec.HEALTH_BONUS_CODEC

	override fun getParts(): Set<TraitPart> {
		val set = mutableSetOf<LiteralPart>()
		if(base > 0) set.add(LiteralPart("mtt.feature.bonus_health.base.declaration", base))
		if(perLevel > 0) set.add(LiteralPart("mtt.feature.bonus_health.per_level.declaration", perLevel))
		return set
	}

}

class SpellcastingAbilityTrait(val options: Set<Ability>): Trait<SpellcastingAbilityTrait>(){
	override fun getCodec(): TraitCodec<SpellcastingAbilityTrait> = TraitCodec.SPELLCASTING_ABILITY_CODEC

	override fun getParts(): Set<TraitPart> {
		return if(options.size == 1) setOf(LiteralPart(getDeclarationKey(), Text.translatable("mtt.ability.${options.first().id}")))
		else setOf(
			OneChoicePart(
				options,
				getLabel(),
				Ability::valueOf,
				Ability::name,
				{ ability -> "mtt.ability.${ability.id}" },
				getDeclarationKey(),
				{ _, _, _ -> })
		)
	}

}

class SpellTrait(val unlockLevel: Int, val spells: Set<String>): Trait<SpellTrait>(){
	override fun getCodec(): TraitCodec<SpellTrait>  = TraitCodec.SPELL_CODEC

	override fun getLabelKey(): String {
		return if (spells.size != 1) super.getLabelKey()
		else "mtt.spell.${spells.first()}"
	}

	override fun getParts(): Set<TraitPart> {
		return setOf(
			LiteralPart(
				getDeclarationKey(),
				Localisation.translateList(spells.toList(), false, ::getTranslationKey),
				unlockLevel
			)
		)
	}

}

class AbilityImprovementTrait(val options: Set<Ability>, val points: Int): Trait<AbilityImprovementTrait>(){
	override fun getCodec(): TraitCodec<AbilityImprovementTrait> = TraitCodec.ABILITY_IMPROVEMENT_CODEC

	override fun getParts(): Set<TraitPart> {
		return setOf(TallyPart(options, points, Ability::valueOf, Ability::name, { "mtt.ability.${it.id}" }){ sheet, ability, score, src -> sheet.abilities.modify(ability, score, src)})
	}
}

class FeatTrait(val feats: Set<Feat>): Trait<FeatTrait>(){
	override fun getCodec(): TraitCodec<FeatTrait>  = TraitCodec.FEAT_CODEC

	override fun getParts(): Set<TraitPart> {
		return if(feats.size == 1) setOf(LiteralPart(getDeclarationKey(), Text.translatable("mtt.feat.${feats.first().id}")), LiteralPart(getDescriptionKey()))
		else setOf(OneChoicePart(feats, getLabel(), Feat::findById, Feat::id, { "mtt.feat.${it.id}" }, getDeclarationKey(), { sheet, value, src -> }), LiteralPart(getDescriptionKey()))
	}
}

class CustomTrait(val id: String): Trait<CustomTrait>() {
	override fun getCodec(): TraitCodec<CustomTrait> = TraitCodec.CUSTOM_CODEC
	override fun getID(): String = id
	override fun getParts(): Set<TraitPart> {
		return setOf(LiteralPart(getDescriptionKey()))
	}
}

typealias ApplySingle<T> = (CharacterSheet, T, Source) -> Unit
typealias ApplyCollection<T> = (CharacterSheet, Collection<T>, Source) -> Unit