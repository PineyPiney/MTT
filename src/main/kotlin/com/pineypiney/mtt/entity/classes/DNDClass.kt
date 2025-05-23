package com.pineypiney.mtt.entity.classes

class DNDClass(val healthDie: Int) {

	companion object {
		val BARBARIAN = DNDClass(12)
		val FIGHTER = DNDClass(10)
		val RANGER = DNDClass(8)
		val WIZARD = DNDClass(6)
	}
}