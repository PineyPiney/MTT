package com.pineypiney.mtt.dice

import kotlin.math.max
import kotlin.math.min

class DieFormula(val numDice: Int, val numSides: Int, private val operators: List<Triple<Operator, Selector, Int>>){

	fun roll(debug: Boolean): List<Int>{
		val diceList = MutableList(numDice){ Dice.roll(numSides) }
		if(debug) println("Initial Rolls: {${diceList.joinToString()}}")
		for((operator, selector, ref) in operators){
			when(operator){
				is IndividualOperator -> operator.op(diceList, selector, numSides, ref)
				is GroupOperator -> operator.op(diceList, selector, ref)
			}
			if(diceList.size > DICE_LIMIT) throw DiceFormulaError("Too many dice")
		}
		return diceList
	}

	sealed interface Operator{val name: String}

	class IndividualOperator(override val name: String, val op: (diceList: MutableList<Int>, selector: Selector, die: Int, ref: Int) -> Unit): Operator
	class GroupOperator(override val name: String, val op: (diceList: MutableList<Int>, selector: Selector, ref: Int) -> Unit): Operator

	sealed interface Selector{val name: String}

	class IndividualSelector(override val name: String, val pred: (value: Int, ref: Int) -> Boolean): Selector
	class GroupSelector(override val name: String, val op: (values: List<Int>, ref: Int) -> List<Int>): Selector

	companion object {

		const val DICE_LIMIT = 1000

		private fun readNumber(string: String, index: Int): Pair<Int, Int>{
			var i = index
			while(i < string.length && string[i].isDigit()) i++
			return string.substring(index, i).toInt() to i
		}

		private fun readOperator(string: String, index: Int): Pair<Operation, Int>{
			var i = index
			var selector: Selector = LITERAL_SELECTOR
			val operator = when(string[i++]){
				'k' -> KeepOperator
				'd', 'p' -> DropOperator
				'r' -> {
					when(string[i++]){
						'r' -> Reroll
						'o' -> RerollOnce
						'a' -> RerollAdd
						else -> throw DiceFormulaError("Invalid dice reroll operator \"r${string[i]}\", try rr, ro or ra")
					}
				}
				'm' -> {
					when(string[i++]){
						'i' -> Minimum
						'a' -> Maximum
						else -> throw DiceFormulaError("Invalid bounds operator \"m${string[i]}\", try mi or ma")
					}
				}
				'e' -> Explode
				else -> throw DiceFormulaError("Invalid dice operator \"${string[i]}\"")
			}
			when(string[i++]){
				'l' -> selector = LOWEST_SELECTOR
				'h' -> selector = HIGHEST_SELECTOR
				'>' -> selector = GREATER_SELECTOR
				'<' -> selector = LESSER_SELECTOR
				else -> i--
			}
			val (integer, end) = readNumber(string, i)
			return Triple(operator, selector, integer) to end
		}

		fun parse(string: String): DieFormula?{
			val dI = string.indexOf('d')
			if(dI == - 1) return null
			val numDie = string.substring(0, dI).trim().toInt()
			val (dieSides, i) = readNumber(string, dI + 1)
			var index = i
			val operators = mutableListOf<Operation>()
			while(index < string.length && !string[index].isWhitespace()){
				val (op, j) = readOperator(string, index)
				operators.add(op)
				index = j
			}
			return DieFormula(numDie, dieSides, operators)
		}

		private val LITERAL_SELECTOR = IndividualSelector("Number Literal"){ value, ref -> value == ref }
		private val GREATER_SELECTOR = IndividualSelector("Greater Than"){ value, ref -> value > ref }
		private val LESSER_SELECTOR = IndividualSelector("Less Than"){ value, ref -> value < ref }

		private val LOWEST_SELECTOR = GroupSelector("Lowest Values"){ list, ref ->
			if(ref <= 0) emptyList()
			else if(ref >= list.size) List(list.size){it}
			else{
				val lowest = mutableListOf<Int>()
				var value = 1
				while(lowest.size < ref){
					for((i, v) in list.withIndex()){
						if(v == value){
							lowest.add(i)
							if(lowest.size == ref) break
						}
					}
					value++
				}
				lowest
			}
		}

		private val HIGHEST_SELECTOR = GroupSelector("Highest Values"){ list, x ->
			if(x <= 0) emptyList()
			else if(x >= list.size) List(list.size){it}
			else{
				val highest = mutableListOf<Int>()
				var value = list.max()
				while(highest.size < x){
					for((i, v) in list.withIndex()){
						if(v == value){
							highest.add(i)
							if(highest.size == x) break
						}
					}
					value--
				}
				highest
			}
		}

		private val KeepOperator = GroupOperator("Keep"){ list, selector, ref ->
			when(selector){
				is IndividualSelector -> for(i in (0..<list.size).reversed()) if(!selector.pred(list[i], ref)) list.removeAt(i)
				is GroupSelector -> {
					val selected = selector.op(list, ref)
					for(i in (0..<list.size).reversed()) if(!selected.contains(i)) list.removeAt(i)
				}
			}

		}

		private val DropOperator = GroupOperator("Drop") { list, selector, ref ->
			when (selector) {
				is IndividualSelector -> for (i in (0..<list.size).reversed()) if(selector.pred(list[i], ref)) list.removeAt(i)
				is GroupSelector -> {
					val selected = selector.op(list, ref)
					for (i in selected.sortedDescending()) list.removeAt(i)
				}
			}
		}

		private val Reroll = IndividualOperator("Reroll"){ list, selector, die, ref ->
			if(selector !is IndividualSelector) throw DiceFormulaError("Cannot use operator Reroll with group selector ${selector.name}")
			for(i in 0..<list.size) {
				var overflow = 0
				while(selector.pred(list[i], ref)) {
					overflow++
					if(overflow > 500) throw DiceFormulaError("Rerolled dice too many times")
					list[i] = Dice.roll(die)
				}
			}
		}

		private val RerollOnce = IndividualOperator("RerollOnce"){ list, selector, die, ref ->
			when(selector){
				is IndividualSelector -> {
					for(i in 0..<list.size) if(selector.pred(list[i], ref)) list[i] = Dice.roll(die)
				}
				is GroupSelector -> {
					val selected = selector.op(list, ref)
					for(i in 0..<selected.size) list[selected[i]] = Dice.roll(die)
				}
			}
		}

		private val RerollAdd = IndividualOperator("RerollAdd"){ list, selector, die, ref ->
			when(selector){
				is IndividualSelector -> {
					for(i in 0..<list.size) if(selector.pred(list[i], ref)) list[i] += Dice.roll(die)
				}
				is GroupSelector -> {
					val selected = selector.op(list, ref)
					for(i in 0..<selected.size) list[selected[i]] += Dice.roll(die)
				}
			}
		}
		private val Minimum = IndividualOperator("Minimum"){ list, _, _, ref ->
			for(i in 0..<list.size) list[i] = max(list[i], ref)
		}
		private val Maximum = IndividualOperator("Maximum"){ list, _, _, ref ->
			for(i in 0..<list.size) list[i] = min(list[i], ref)
		}
		private val Explode = IndividualOperator("Explode"){ list, selector, die, ref ->
			when(selector){
				is IndividualSelector -> {
					for(i in 0..<list.size) {
						var v = list[i]
						var overflow = 0
						while(selector.pred(v, ref)) {
							overflow++
							if(overflow > 500) throw DiceFormulaError("Exploded dice too many times")
							v = Dice.roll(die)
							list.add(v)
						}
					}
				}
				is GroupSelector -> {
					throw DiceFormulaError("Cannot use operator Explode with group selector ${selector.name}")
				}
			}
		}
	}
}

typealias Operation = Triple<DieFormula.Operator, DieFormula.Selector, Int>