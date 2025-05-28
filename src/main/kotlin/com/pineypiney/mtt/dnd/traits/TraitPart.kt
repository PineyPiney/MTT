package com.pineypiney.mtt.dnd.traits

import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.util.Localisation
import net.minecraft.text.Text

sealed class TraitPart {
	abstract fun isReady(): Boolean
	abstract fun apply(sheet: CharacterSheet, src: Source)
}

class LiteralPart(val text: Text, val func: (sheet: CharacterSheet, src: Source) -> Unit): TraitPart(){

	constructor(key: String, vararg args: Any, func: (CharacterSheet, Source) -> Unit = { _, _ -> }): this(Text.translatable(key, *args), func)

	override fun isReady() = true
	override fun apply(sheet: CharacterSheet, src: Source) {
		func(sheet, src)
	}
}

class OneChoicePart<T>(val choices: Set<T>, val label: Text, val translationKey: (T) -> String, val declarationKey: String, val func: (sheet: CharacterSheet, value: T, src: Source) -> Unit, val args: Array<Any> = emptyArray()): TraitPart(){

	//constructor(choices: Set<T>, translationKey: (T) -> String, declarationKey: String, vararg args: Any): this(choices, translationKey, declarationKey, args.toList().toTypedArray())
	var decision: T? = null
	override fun isReady(): Boolean = decision != null
	override fun apply(sheet: CharacterSheet, src: Source) {
		decision?.let { func(sheet, it, src) }
	}
}


class GivenAndOptionsPart<T>(val data: GivenAndOptions<T>, val label: Text, val translationKey: (T) -> String, val declarationKey: String, val func: (sheet: CharacterSheet, values: List<T>, src: Source) -> Unit, val args: Array<Any> = emptyArray()): TraitPart() {

	//constructor(data: GivenAndOptions<T>, translationKey: (T) -> String, declarationKey: String, vararg args: Any): this(data, translationKey, declarationKey, args.toList().toTypedArray())
	val decisions = mutableListOf<T>()
	override fun isReady(): Boolean = decisions.size == data.numChoices
	override fun apply(sheet: CharacterSheet, src: Source) {
		func(sheet, decisions, src)
	}

	fun getCurrentList(): Text{
		val list = data.given.toMutableList().apply { addAll(decisions) }
		return Localisation.translateList(list, false, translationKey)
	}
}

class TallyPart<T>(val options: Set<T>, val points: Int, val translationKey: (T) -> String): TraitPart(){
	val map = options.map { 0 }.toMutableList()
	val pointsLeft get() = points - map.sum()
	override fun isReady(): Boolean {
		return pointsLeft == 0
	}
	override fun apply(sheet: CharacterSheet, src: Source) {

	}
}
