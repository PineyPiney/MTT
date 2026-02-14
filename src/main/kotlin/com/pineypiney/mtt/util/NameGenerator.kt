package com.pineypiney.mtt.util

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.random.Random

class NameGenerator(val names: Map<String, Set<String>>, val formats: Set<Format>) {

	val weightTotal = formats.sumOf(Format::weight)

	fun generate(): String {
		var i = Random.nextInt(weightTotal)
		val format = formats.first {
			i -= it.weight
			i < 0
		}
		val s = StringBuilder()
		for (part in format.parts) s.append(part.getPart(this))
		return s.toString()
	}

	class Format(val weight: Int, val parts: Set<Part>)

	abstract class Part {
		abstract fun getPart(generator: NameGenerator): String
	}

	class LiteralPart(val literal: String) : Part() {
		override fun getPart(generator: NameGenerator) = literal
	}

	class RandomPart(val part: String) : Part() {
		override fun getPart(generator: NameGenerator) = generator.names[part]!!.random()
	}

	companion object {
		fun fromJson(json: JsonObject): NameGenerator {
			val listJson = json.objOrNull("lists")
			val names =
				listJson?.mapValues { (_, names) -> (names as JsonArray).map { (it as JsonPrimitive).content }.toSet() }
					?: emptyMap()
			val formatsJson = json.array("formats")
			val formats = formatsJson.map { format ->
				val obj = format as JsonObject
				val weight = obj.int("weight", 1)
				val partsJson = obj.arrayOrNull("parts") ?: return@map Format(0, emptySet())

				Format(weight, partsJson.map { partJson ->
					val partObj = partJson as JsonObject
					if (partObj.containsKey("literal")) LiteralPart(partObj.string("literal"))
					else RandomPart(partObj.string("random"))
				}.toSet())
			}
			return NameGenerator(names, formats.toSet())
		}
	}
}