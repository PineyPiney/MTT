package com.pineypiney.mtt.dnd.species

import com.pineypiney.mtt.dnd.traits.TraitComponent

class SubSpecies(val name: String, val components: List<TraitComponent<*, *>>, val namedTraits: List<NamedTrait>) {
	override fun toString(): String {
		return "$name Species"
	}
}