package com.pineypiney.mtt.dnd.species

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.CreatureType
import com.pineypiney.mtt.dnd.Size
import com.pineypiney.mtt.dnd.traits.*
import kotlinx.serialization.json.*

open class Species(val id: String, val type: CreatureType, val speed: Int, val size: Trait<Size>, val model: Trait<String>, val components: List<TraitComponent<*, *>>, val namedTraits: List<NamedTrait>, val subspecies: List<SubSpecies>) {

	class Builder(val id: String){
		var type = CreatureType.HUMANOID
		var speed = 30
		var size: Trait<Size> = SetTraits(Size.MEDIUM)
		var model: Trait<String> = SetTraits("default")

		val components = mutableListOf<TraitComponent<*, *>>()
		val namedTraits = mutableListOf<NamedTrait>()
		val subspecies = mutableListOf<SubSpecies>()

		fun type(value: CreatureType) = this.apply { type = value }
		fun speed(value: Int) = this.apply{ speed = value }
		fun size(value: Trait<Size>) = this.apply{ size = value }
		fun model(value: Trait<String>) = this.apply{ model = value }

		fun addComponents(json: JsonElement, factory: (json: JsonElement, list: MutableList<TraitComponent<*, *>>) -> Unit){
			factory(json, components)
		}

		fun addNamedAbility(json: JsonElement, factory: (json: JsonElement, list: MutableList<TraitComponent<*, *>>) -> Unit){
			factory(json, components)
		}

		fun build() = Species(id, type, speed, size, model, components, namedTraits, subspecies)
	}

	override fun toString(): String {
		return "${this@Species.id} Species"
	}

	companion object {

		val NONE = Builder("None").build()

		@Throws(Exception::class)
		fun parse(json: JsonObject): Species{
			val speciesID = (json["id"] as? JsonPrimitive)?.content ?: throw Exception()
			val builder = Builder(speciesID)

			for ((name, element) in json) {
				when(name){
					"id" -> continue
					"type" -> builder.type(CreatureType.valueOf(element.jsonPrimitive.content.uppercase()))
					"speed"-> builder.speed(element.jsonPrimitive.int)
					"size" -> {
						when(element){
							is JsonObject -> {
								val options = element["options"]!!.jsonArray
								builder.size(TraitOption(1, options.map { Size.fromString(it.jsonPrimitive.content) }))
							}
							is JsonPrimitive -> builder.size(SetTraits(Size.fromString(element.content)))
							else -> {}
						}
					}

					"model" -> {
						when(element){
							is JsonObject -> {
								val options = element["options"]!!.jsonArray
								builder.model(TraitOption(1, options.map { it.jsonPrimitive.content }))
							}
							is JsonPrimitive -> builder.model(SetTraits(element.content))
							else -> {}
						}
					}
					"sub_species" -> {
						val speciesArray = (element as? JsonArray)
						if(speciesArray == null){
							MTT.logger.warn("SubSpecies json should be an array")
							continue
						}
						for(entry in speciesArray){
							when(entry){
								is JsonPrimitive -> builder.components.add(SubspeciesIDComponent(SetTraits(entry.content)))
								is JsonObject -> {
									val subSpeciesID = entry["id"]?.jsonPrimitive?.content
									if(subSpeciesID == null){
										MTT.logger.warn("SubSpecies does not contain 'name' field")
										continue
									}

									val subComps = mutableListOf<TraitComponent<*, *>>()
									val subTraits = mutableListOf<NamedTrait>()
									for((name, trait) in entry){
										if(name == "id" || name == "description") continue
										val factory = TraitComponents.list[name]
										if(factory == null) {
											MTT.logger.warn("There is no trait component with id $name")
											continue
										}
										parseTrait(name, trait, subComps, subTraits)
									}

									builder.subspecies.add(SubSpecies(subSpeciesID, subComps, subTraits))
								}
								else -> {}
							}
						}
					}
					else -> {
						val error = parseTrait(name, element, builder.components, builder.namedTraits)
						if(error.isNotEmpty()) MTT.logger.warn("Error parsing species json $name: $error")
					}
				}
			}

			return builder.build()
		}

		fun parseTrait(name: String, element: JsonElement, components: MutableList<TraitComponent<*, *>>, namedTraits: MutableList<NamedTrait>): String{
			when(name){
				"trait" -> {
					when(element){
						is JsonObject -> parseNamedTrait(element, components, namedTraits)
						is JsonArray -> element.forEach { if(it is JsonObject) parseNamedTrait(it, components, namedTraits) }
						else -> return "Trait json does not contain any named trait information"
					}
				}
				else -> {
					val codec = TraitComponents.list[name] ?: return "There is no trait component with id $name"
					codec.readFromJson(element, components)
				}
			}
			return ""
		}

		fun parseNamedTrait(element: JsonObject, components: MutableList<TraitComponent<*, *>>, namedTraits: MutableList<NamedTrait>){
			val traitID = element["id"]?.jsonPrimitive?.content ?: return
			val effect = (element["effect"] as? JsonObject) ?: run {
				components.add(CustomTraitComponent(traitID))
				return
			}
			val list = mutableListOf<TraitComponent<*, *>>()
			for((type, json) in effect){
				val codec = TraitComponents.list[type] ?: continue
				codec.readFromJson(json, list)
			}
			namedTraits.add(NamedTrait(traitID, list))
		}
	}
}