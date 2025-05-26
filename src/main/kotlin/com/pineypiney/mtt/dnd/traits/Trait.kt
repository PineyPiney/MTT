package com.pineypiney.mtt.dnd.traits

import com.pineypiney.mtt.dnd.CharacterSheet

abstract class Trait<T>() {
	abstract val isReady: Boolean
	abstract val apply: ApplyTrait<T>
	abstract val values: Set<T>

	fun applyWithValues(sheet: CharacterSheet, src: Source){
		apply(sheet, values, src)
	}
}
typealias ApplyTrait<T> = CharacterSheet.(Set<T>, Source) -> Unit