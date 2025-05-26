package com.pineypiney.mtt.dnd.traits

class SetTraits<T>(override val values: Set<T>, override val apply: ApplyTrait<T>) : Trait<T>(){
	constructor(apply: ApplyTrait<T>, vararg values: T): this(values.toSet(), apply)
	constructor(value: T, apply: ApplyTrait<T>): this(setOf(value), apply)

	override val isReady: Boolean get() = true
	override fun toString(): String {
		return "[${values.joinToString { it.toString() }}]"
	}
}