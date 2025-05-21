package com.pineypiney.mtt.dnd.traits

class TraitOption<T>(val choices: Int, val options: List<T>) : Trait<T>() {
	override fun toString(): String {
		return "$choices Choices[${options.joinToString { it.toString() }}]"
	}
}