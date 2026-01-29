package com.pineypiney.mtt.dnd

import com.pineypiney.mtt.dnd.traits.Source

class Property<T>(val defaultValue: T, val assignValue: (Source, T) -> Int) {

	val sources = mutableMapOf<Source, T>()

	fun getValue(): T{
		return sources.maxByOrNull { assignValue(it.key, it.value) }?.value ?: defaultValue
	}
}