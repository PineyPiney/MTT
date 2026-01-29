package com.pineypiney.mtt.dnd.race

import com.pineypiney.mtt.dnd.traits.Trait

class NamedTrait<T: Trait<T>>(val name: String, val traits: Set<Trait<T>>) {

	override fun toString(): String {
		return "Trait($name)"
	}
}