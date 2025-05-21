package com.pineypiney.mtt

import com.pineypiney.mtt.dnd.species.Species
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

fun main() {
	parseSpecies()
}

fun parseSpecies(){
	val json = Json.parseToJsonElement(human).jsonObject
	val species = Species.parse(json)
}

const val human = """"""
