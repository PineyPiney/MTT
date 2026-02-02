package com.pineypiney.mtt.dnd.race

import com.pineypiney.mtt.dnd.traits.Trait

class NamedTrait<T : Trait<*>>(val name: String, val traits: Set<T>) {

	override fun toString(): String {
		return "Trait($name)"
	}
}