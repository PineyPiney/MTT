package com.pineypiney.mtt

import com.pineypiney.mtt.dnd.race.Race
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

fun main() {
	parseRace()
}

fun parseRace(){
	val json = Json.parseToJsonElement(human).jsonObject
	val race = Race.parse(json)
}

const val human = """"""
