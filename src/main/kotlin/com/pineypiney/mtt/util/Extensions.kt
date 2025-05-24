package com.pineypiney.mtt.util

import java.util.*
import kotlin.jvm.optionals.getOrNull

fun <T: Any> T?.optional(): Optional<T> {
	return if(this == null) Optional.empty()
	else Optional.of(this)
}

fun <T: Any> Optional<T>.nullable(): T? {
	return this.getOrNull()
}