package com.pineypiney.mtt.dnd.traits

import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.util.Localisation
import net.minecraft.text.Text

sealed class TraitPart {
	abstract fun isReady(): Boolean
	abstract fun updateValues(values: List<String>)
	abstract fun apply(sheet: CharacterSheet, src: Source)
}

class LiteralPart(val text: Text, val func: (sheet: CharacterSheet, src: Source) -> Unit): TraitPart(){

	constructor(key: String, vararg args: Any, func: (CharacterSheet, Source) -> Unit = { _, _ -> }): this(Text.translatable(key, *args), func)

	override fun isReady() = true
	override fun updateValues(values: List<String>) {}
	override fun apply(sheet: CharacterSheet, src: Source) {
		func(sheet, src)
	}
}

class OneChoicePart<T>(val choices: Set<T>, val label: Text, val parse: (String) -> T, val unparse: (T) -> String, val translationKey: (T) -> String, val declarationKey: String, val func: (sheet: CharacterSheet, value: T, src: Source) -> Unit, val args: Array<Any> = emptyArray()): TraitPart(){

	//constructor(choices: Set<T>, translationKey: (T) -> String, declarationKey: String, vararg args: Any): this(choices, translationKey, declarationKey, args.toList().toTypedArray())
	var decision: T? = null
	override fun isReady(): Boolean = decision != null
	override fun updateValues(values: List<String>) {
		val str = values.firstOrNull()
		if(str == null) decision = null
		else {
			val value = parse(str)
			if(choices.contains(value)) decision = value
		}
	}
	override fun apply(sheet: CharacterSheet, src: Source) {
		decision?.let { func(sheet, it, src) }
	}
}


class GivenAndOptionsPart<T>(val data: GivenAndOptions<T>, val label: Text, val parse: (String) -> T, val unparse: (T) -> String, val translationKey: (T) -> String, val declarationKey: String, val func: (sheet: CharacterSheet, values: List<T>, src: Source) -> Unit, val args: Array<Any> = emptyArray()): TraitPart() {

	//constructor(data: GivenAndOptions<T>, translationKey: (T) -> String, declarationKey: String, vararg args: Any): this(data, translationKey, declarationKey, args.toList().toTypedArray())
	val decisions = mutableListOf<T>()
	override fun isReady(): Boolean = decisions.size == data.numChoices
	override fun updateValues(values: List<String>) {
		decisions.clear()
		val typeValues = values.map(parse)
		if(typeValues.all { data.options.contains(it) } && typeValues.size <= data.numChoices) {
			decisions.addAll(typeValues)
		}
	}
	override fun apply(sheet: CharacterSheet, src: Source) {
		func(sheet, decisions, src)
	}

	fun getCurrentList(): Text{
		val list = data.given.toMutableList().apply { addAll(decisions) }
		return Localisation.translateList(list, false, translationKey)
	}
}

class TallyPart<T>(val options: Set<T>, val points: Int, val parse: (String) -> T, val unparse: (T) -> String, val translationKey: (T) -> String, val func: (sheet: CharacterSheet, option: T, value: Int, src: Source) -> Unit): TraitPart(){
	val tallies = options.map { 0 }.toMutableList()
	val pointsLeft get() = points - tallies.sum()
	override fun isReady(): Boolean {
		return pointsLeft == 0
	}

	override fun updateValues(values: List<String>) {
		if(values.size <= points) {
			var i = 0
			for (option in options) {
				val str = unparse(option)
				tallies[i++] = values.count { it == str }
			}
		}
	}
	override fun apply(sheet: CharacterSheet, src: Source) {
		var i = 0
		for (option in options) {
			val value = tallies[i++]
			if(value > 0) func(sheet, option, value, src)
		}
	}
}

class AbilityPointBuyPart(){

	val totalPoints = 27
	var pointsLeft = 27
	val points = IntArray(6){ 8 }

	fun isReady(): Boolean = pointsLeft == 0

	fun updateValues(values: List<String>) {
		if(values.size < 2) return
		val ability = Integer.parseInt(values.first())
		val score = Integer.parseInt(values[1])
		points[ability] = score
	}

	fun apply(sheet: CharacterSheet) {
		sheet.abilities.strength = points[0]
		sheet.abilities.dexterity = points[1]
		sheet.abilities.constitution = points[2]
		sheet.abilities.intelligence = points[3]
		sheet.abilities.wisdom = points[4]
		sheet.abilities.charisma = points[5]
	}

	fun increment(ability: Int): Boolean{
		if(points[ability] < 15 && pointsLeft >= 1){
			if(points[ability] < 13 || pointsLeft >= 2){
				points[ability]++
				updatePoints()
				return true
			}
		}
		return false
	}

	fun decrement(ability: Int): Boolean{
		if(points[ability] > 8){
			points[ability]--
			updatePoints()
			return true
		}
		return false
	}

	fun updatePoints(){
		var spentPoints = 0
		for(i in points){
			spentPoints += if(i > 13) (2 * i - 21)
			else (i - 8)
		}
		pointsLeft = totalPoints - spentPoints
	}
}
