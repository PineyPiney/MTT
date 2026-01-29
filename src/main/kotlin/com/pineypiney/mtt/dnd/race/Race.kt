package com.pineypiney.mtt.dnd.race

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.traits.*
import kotlinx.serialization.json.*

open class Race(val id: String, val type: CreatureType, val speed: Int, val size: SizeTrait, val model: ModelTrait, val traits: List<Trait<*>>, val namedTraits: List<NamedTrait<*>>, val subRace: List<SubRace>) {

	fun getAllTraits(subRace: SubRace? = null): Set<Trait<*>>{
		val set = mutableSetOf(CreatureTypeTrait(type), SpeedTrait(speed), size, model)
		set.addAll(traits)
		for(namedTrait in namedTraits) set.addAll(namedTrait.traits)
		return set
	}

	class Builder(val id: String){
		var type = CreatureType.HUMANOID
		var speed = 30
		var size: SizeTrait = SizeTrait(setOf(Size.MEDIUM))
		var model: ModelTrait = ModelTrait(setOf("default"))

		val components = mutableListOf<Trait<*>>()
		val namedTraits = mutableListOf<NamedTrait<*>>()
		val subRaces = mutableListOf<SubRace>()

		fun type(value: CreatureType) = this.apply { type = value }
		fun speed(value: Int) = this.apply{ speed = value }
		fun size(value: SizeTrait) = this.apply{ size = value }
		fun model(value: ModelTrait) = this.apply{ model = value }

		fun build() = Race(id, type, speed, size, model, components, namedTraits, subRaces)
	}

	override fun toString(): String {
		return "${this@Race.id} Race"
	}

	companion object {

		val NONE = Builder("None").build()

		@JvmField
		val set = mutableSetOf<Race>()
		fun findById(id: String) = set.firstOrNull { it.id == id } ?: NONE

		@Throws(Exception::class)
		fun parse(json: JsonObject): Race{
			val raceID = (json["id"] as? JsonPrimitive)?.content ?: throw Exception()
			val builder = Builder(raceID)

			for ((id, element) in json) {
				when(id){
					"id" -> continue
					"type" -> builder.type(CreatureType.valueOf(element.jsonPrimitive.content.uppercase()))
					"speed"-> builder.speed(element.jsonPrimitive.int)
					"size" -> builder.size(SizeTrait(TraitCodec.readJsonList(element) { Size.fromString(it.content) }.toSet()))
					"model" -> builder.model(ModelTrait(TraitCodec.readJsonList(element, JsonPrimitive::content).toSet()))
					"tags" -> {}

					"sub_race" -> {
						val subRaceArray = (element as? JsonArray)
						if(subRaceArray == null){
							MTT.logger.warn("SubRace json should be an array")
							continue
						}
						for(entry in subRaceArray){
							when(entry){
								is JsonPrimitive -> {}//builder.components.add(SubRaceIDComponent(SetTraits(entry.content){ set, _ -> }))
								is JsonObject -> {
									val subRaceID = entry["id"]?.jsonPrimitive?.content
									if(subRaceID == null){
										MTT.logger.warn("SubRace does not contain 'name' field")
										continue
									}

									val subComps = mutableListOf<Trait<*>>()
									val subTraits = mutableListOf<NamedTrait<*>>()
									for((name, trait) in entry){
										if(name == "id" || name == "description") continue
										parseTrait(name, trait, subComps, subTraits)
									}

									builder.subRaces.add(SubRace(subRaceID, subComps, subTraits))
								}
								else -> {}
							}
						}
					}
					else -> {
						val error = parseTrait(id, element, builder.components, builder.namedTraits)
						if(error.isNotEmpty()) MTT.logger.warn("Error parsing race json $id: $error")
					}
				}
			}

			return builder.build()
		}

		fun parseTrait(id: String, element: JsonElement, traits: MutableList<Trait<*>>, namedTraits: MutableList<NamedTrait<*>>): String{
			when(id){
				"trait" -> {
					when(element){
						is JsonObject -> parseNamedTrait(element, traits, namedTraits)
						is JsonArray -> element.forEach { if(it is JsonObject) parseNamedTrait(it, traits, namedTraits) }
						else -> return "Trait json does not contain any named trait information"
					}
				}
				else -> {
					val codec = Traits.getCodec(id) ?: return "There is no trait component with id $id"
					codec.readFromJson(element, traits)
				}
			}
			return ""
		}

		fun parseNamedTrait(element: JsonObject, traits: MutableList<Trait<*>>, namedTraits: MutableList<NamedTrait<*>>){
			val traitID = element["id"]?.jsonPrimitive?.content ?: return
			val effect = (element["effect"] as? JsonObject) ?: run {
				traits.add(CustomTrait(traitID))
				return
			}

			val set = mutableSetOf<Trait<*>>()
			for((type, json) in effect){
				val codec = Traits.getCodec(type) ?: continue
				codec.readFromJson(json, set)
			}
			namedTraits.add(NamedTrait(traitID, set))
		}
	}
}