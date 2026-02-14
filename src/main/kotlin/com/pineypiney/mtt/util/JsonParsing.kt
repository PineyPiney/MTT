package com.pineypiney.mtt.util

import kotlinx.serialization.json.*

fun JsonArray.int(index: Int, default: Int = 0): Int{
	return try { getOrNull(index)?.jsonPrimitive?.intOrNull ?: default }
	catch (e: IllegalArgumentException){ default }
}

fun JsonArray.float(index: Int, default: Float = 0f): Float{
	return try { getOrNull(index)?.jsonPrimitive?.floatOrNull ?: default }
	catch (e: IllegalArgumentException){ default }
}

fun JsonObject.int(key: String, default: Int = 0): Int {
	return try {
		get(key)?.jsonPrimitive?.intOrNull ?: default
	} catch (e: IllegalArgumentException) {
		default
	}
}

fun JsonObject.float(key: String, default: Float = 0f): Float {
	return try {
		get(key)?.jsonPrimitive?.floatOrNull ?: default
	} catch (e: IllegalArgumentException) {
		default
	}
}

fun JsonObject.string(key: String, default: String = ""): String {
	return try {
		get(key)?.jsonPrimitive?.contentOrNull ?: return default
	} catch (e: IllegalArgumentException) {
		default
	}
}

fun JsonObject.stringOrNull(key: String): String? {
	return try {
		get(key)?.jsonPrimitive?.contentOrNull ?: return null
	} catch (e: IllegalArgumentException) {
		null
	}
}

fun JsonObject.obj(key: String, default: JsonObject = JsonObject(emptyMap())): JsonObject {
	return try {
		get(key)?.jsonObject ?: return default
	} catch (e: IllegalArgumentException) {
		default
	}
}

fun JsonObject.objOrNull(key: String): JsonObject? {
	return try {
		get(key)?.jsonObject ?: return null
	} catch (e: IllegalArgumentException) {
		null
	}
}

fun JsonObject.array(key: String, default: JsonArray = JsonArray(emptyList())): JsonArray {
	return try {
		get(key)?.jsonArray ?: return default
	} catch (e: IllegalArgumentException) {
		default
	}
}

fun JsonObject.arrayOrNull(key: String): JsonArray? {
	return try {
		get(key)?.jsonArray ?: return null
	} catch (e: IllegalArgumentException) {
		null
	}
}