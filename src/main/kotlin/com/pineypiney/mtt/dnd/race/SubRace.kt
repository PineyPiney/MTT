package com.pineypiney.mtt.dnd.race

import com.pineypiney.mtt.dnd.traits.Trait

class SubRace(val name: String, val traits: List<Trait<*>>, val namedTraits: List<NamedTrait<*>>) {
	override fun toString(): String {
		return "$name SubRace"
	}
}