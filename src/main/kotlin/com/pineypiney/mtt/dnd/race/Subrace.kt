package com.pineypiney.mtt.dnd.race

import com.pineypiney.mtt.dnd.traits.Trait

class Subrace(val name: String, val traits: List<Trait<*>>, val namedTraits: List<NamedTrait<*>>) {


	fun getAllTraits(): Set<Trait<*>> {
		val set = traits.toMutableSet()
		for (namedTrait in namedTraits) set.addAll(namedTrait.traits)
		return set
	}


	override fun toString(): String {
		return "$name Subrace"
	}
}