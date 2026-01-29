package com.pineypiney.mtt.dnd

class CoinValue(val coppers: Long) {

	override fun toString(): String {
		val str = StringBuilder()
		val change = coppers % 100
		val gold = (coppers - change) / 100
		if(gold > 0L) {
			if (gold > 1000L && gold % 10 == 0L) str.append("${gold / 10} PP")
			else str.append("$gold GP")
		}

		if(change > 0L){
			if(change % 10 == 0L) str.append("${change/10} SP")
			else str.append("$change CP")
		}
		return str.toString()
	}

	companion object {
		val ZERO = CoinValue(0L)
		fun silver(silver: Int) = CoinValue(silver * 10L)
		fun gold(gold: Int) = CoinValue(gold * 100L)
	}
}