package com.pineypiney.mtt.dnd.species

import com.pineypiney.mtt.dnd.traits.TraitComponent

class NamedTrait(val name: String, val components: List<TraitComponent<*, *>>) {

	override fun toString(): String {
		return "Trait($name)"
	}
}