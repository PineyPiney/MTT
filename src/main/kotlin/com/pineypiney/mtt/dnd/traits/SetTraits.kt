package com.pineypiney.mtt.dnd.traits

class SetTraits<T>(val values: List<T>) : Trait<T>(){
	constructor(value: T): this(listOf(value))

	override fun toString(): String {
		return "[${values.joinToString { it.toString() }}]"
	}
}