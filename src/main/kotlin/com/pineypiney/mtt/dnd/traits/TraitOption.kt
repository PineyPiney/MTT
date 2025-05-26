package com.pineypiney.mtt.dnd.traits

class TraitOption<T>(val choices: Int, val options: List<T>, override val apply: ApplyTrait<T>) : Trait<T>() {

	override val values: Set<T> = mutableSetOf()
	override val isReady: Boolean get() = values.size == choices

	constructor(choices: Int, applyTrait: ApplyTrait<T>, vararg options: T): this(choices, options.toList(), applyTrait)
	override fun toString(): String {
		return "$choices Choices[${options.joinToString { it.toString() }}]"
	}
}