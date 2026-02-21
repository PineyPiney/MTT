package com.pineypiney.mtt.dnd.rolls

import com.pineypiney.mtt.dnd.traits.Ability

class AbilityCheck(val ability: Ability, val tags: Set<String>) {

	fun hasTag(tag: String) = tags.contains(tag)
}