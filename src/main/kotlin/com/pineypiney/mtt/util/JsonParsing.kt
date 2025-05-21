package com.pineypiney.mtt.util

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

fun JsonArray.int(index: Int, default: Int = 0): Int{
	return try { getOrNull(index)?.jsonPrimitive?.intOrNull ?: default }
	catch (e: IllegalArgumentException){ default }
}

fun JsonArray.float(index: Int, default: Float = 0f): Float{
	return try { getOrNull(index)?.jsonPrimitive?.floatOrNull ?: default }
	catch (e: IllegalArgumentException){ default }
}