package com.pineypiney.mtt.dice

class DiceFormula {

	enum class Operator(val symbol: Char, val operation: (Int, Int) -> Int, val order: Int) {
		PLUS('+', Int::plus, 0),
		MINUS('-', Int::minus, 0),
		TIMES('*', Int::times, 1),
		DIVIDE('/', Int::div, 1)
	}

	companion object {

		fun parse(string: String, debugRolls: Boolean = false, debugLayers: Boolean = false): Int{
			var highestLevel = 0
			var level = 0
			var lastBracket = 0
			val parts = mutableListOf<Pair<Int, String>>()
			for((i, c) in string.withIndex()){
				if(c == '(') {
					parts.add(level++ to string.substring(lastBracket, i))
					lastBracket = i + 1
					if(level > highestLevel) highestLevel = level
				}
				else if(c == ')') {
					parts.add(level-- to string.substring(lastBracket, i))
					lastBracket = i + 1
				}
			}
			parts.add(0 to string.substring(lastBracket))

			var editingLevel = highestLevel
			while(editingLevel > 0) {
				for (i in editingLevel - 1..parts.size - editingLevel) {
					if (parts[i].first == editingLevel) {
						val value = parseWithoutBrackets(parts[i].second, debugRolls)
						val valStr = value.toString()
						parts[i] = editingLevel - 1 to valStr
					}
				}

				// Collapse the parts in editingLevel with the neighbouring parts
				editingLevel--
				if(editingLevel > 0) {
					var startOfLevel = -1
					var i = editingLevel
					while (i < parts.size) {
						if (startOfLevel == -1) {
							if (parts[i].first == editingLevel) startOfLevel = i
							i++
						} else {
							if (parts[i].first < editingLevel) {
								var collapsed = parts[startOfLevel].second
								for (j in startOfLevel + 1..<i) {
									collapsed += parts.removeAt(startOfLevel + 1).second
								}
								parts[startOfLevel] = editingLevel to collapsed

								i = startOfLevel + 2
								startOfLevel = -1
							} else i++
						}
					}
				}

				if(debugLayers) {
					val str = StringBuilder(parts.first().second)
					var layer = 0
					for(l in 1..<parts.size){
						if(parts[l].first > layer) str.append('(')
						else if(parts[l].first < layer) str.append(')')
						layer = parts[l].first
						str.append(parts[l].second)
					}
					println("= $str}")
				}
			}

			return parseWithoutBrackets(parts.joinToString(""){it.second}, debugRolls)
		}

		private fun parseWithoutBrackets(string: String, debugRolls: Boolean): Int{
			//println("Calculating Bracketless Formula $string")
			var currentOperator = Operator.PLUS
			val numberParts = mutableListOf<Pair<Operator, Int>>()
			var index = 0
			for(i in 0..<string.length){
				val operator = getOperator(string[i])
				if(operator != null){
					val numStr = string.substring(index, i)
					// If a number was resolved to be negative then there will be a minus sign immediately after another operator,
					// in which case ignore this minus sign as an operator and include it in the next number parsing
					if(numStr.isBlank() && operator == Operator.MINUS) continue
					numberParts.add(currentOperator to parseValue(numStr, debugRolls))
					index = i+1
					currentOperator = operator
				}
			}
			numberParts.add(currentOperator to parseValue(string.substring(index), debugRolls))

			var part = 1
			while(part < numberParts.size){
				if(numberParts[part].first.order == 1){
					numberParts[part - 1] = numberParts[part - 1].first to numberParts[part].first.operation(numberParts[part - 1].second, numberParts[part].second)
					numberParts.removeAt(part)
				}
				else part++
			}

			//println("After first operations: " + numberParts.joinToString(" "){ "${it.first.symbol} ${it.second}" })

			var value = numberParts[0].second
			for(i in 1..<numberParts.size){
				value = numberParts[i].first.operation(value, numberParts[i].second)
			}
			//println("Value of $string is $value")
			return value
		}

		private fun parseValue(string: String, debugRolls: Boolean): Int{
			val die = DieFormula.parse(string) ?: return string.trim().toInt()
			val rolls = die.roll(debugRolls)
			val value = rolls.sum()
			if(debugRolls) println("Rolled $string as {${rolls.joinToString()}} for a total of $value")
			return value
		}

		private fun getOperator(char: Char): Operator?{
			return Operator.entries.firstOrNull { it.symbol == char }
		}
	}
}