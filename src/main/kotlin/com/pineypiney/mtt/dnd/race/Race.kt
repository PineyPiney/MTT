package com.pineypiney.mtt.dnd.race

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.characters.CharacterModel
import com.pineypiney.mtt.dnd.traits.*
import kotlinx.serialization.json.*
import net.minecraft.text.MutableText
import net.minecraft.text.Text

open class Race(
	val id: String,
	val type: CreatureType,
	val speed: Int,
	val size: SizeTrait,
	val models: Set<CharacterModel>,
	val traits: List<Trait<*>>,
	val namedTraits: List<NamedTrait<*>>,
	val subraces: Set<Subrace>
) {

	fun getAllTraits(subrace: Subrace? = null): Set<Trait<*>> {
		val set = mutableSetOf(CreatureTypeTrait(type), SpeedTrait(speed), size, ModelTrait(this, models))
		set.addAll(traits)
		for(namedTrait in namedTraits) set.addAll(namedTrait.traits)
		if (subrace != null) {
			set.addAll(subrace.traits)
			for (namedTrait in subrace.namedTraits) set.addAll(namedTrait.traits)
		}
		return set
	}

	fun getText(subrace: Subrace? = null): MutableText {
		return if (subrace == null) Text.translatable("mtt.race.$id")
		else Text.translatable("mtt.race.$id")
			.append("/")
			.append(Text.translatable("mtt.race.${subrace.name}"))
	}

	fun getSubrace(subraceID: String) = subraces.firstOrNull { it.name == subraceID }
	fun getModel(modelID: String) = models.firstOrNull { it.id == modelID } ?: models.first()

	class Builder(val id: String){
		var type = CreatureType.HUMANOID
		var speed = 30
		var size: SizeTrait = SizeTrait(setOf(Size.MEDIUM))
		var models = mutableSetOf<CharacterModel>()

		val components = mutableListOf<Trait<*>>()
		val namedTraits = mutableListOf<NamedTrait<*>>()
		val subraces = mutableSetOf<Subrace>()

		fun type(value: CreatureType) = this.apply { type = value }
		fun speed(value: Int) = this.apply{ speed = value }
		fun size(value: SizeTrait) = this.apply{ size = value }
		fun model(value: CharacterModel) = this.apply { models.add(value) }

		fun build() = Race(id, type, speed, size, models, components, namedTraits, subraces)
	}

	override fun toString(): String {
		return "${this@Race.id} Race"
	}

	companion object {

		@JvmField
		val set = mutableSetOf<Race>()
		fun findById(id: String) =
			set.firstOrNull { it.id == id } ?: throw IllegalArgumentException("No Race with id $id")

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
					"models" -> {
						parseModels(element, builder.models)
					}
					"tags" -> {}

					"sub_races" -> {
						if (element !is JsonArray) {
							MTT.logger.warn("Subrace json should be an array")
							continue
						}
						for (entry in element) {
							when(entry){
								is JsonPrimitive -> {}
								is JsonObject -> {
									val subraceID = entry["id"]?.jsonPrimitive?.content
									if (subraceID == null) {
										MTT.logger.warn("Subrace does not contain 'name' field")
										continue
									}

									val subComps = mutableListOf<Trait<*>>()
									val subTraits = mutableListOf<NamedTrait<*>>()
									for((name, trait) in entry){
										if(name == "id" || name == "description") continue
										parseTrait(name, trait, subComps, subTraits)
									}

									builder.subraces.add(Subrace(subraceID, subComps, subTraits))
								}
								else -> {}
							}
						}
					}
					else -> {
						val error = parseTrait(id, element, builder.components, builder.namedTraits)
						if (error.isNotEmpty()) MTT.logger.warn("Error parsing race $raceID json $id: $error")
					}
				}
			}

			return builder.build()
		}

		fun parseModels(json: JsonElement, set: MutableSet<CharacterModel>): String? {
			if (json !is JsonArray) return "Models json should be an array"
			for (model in json) {
				if (model !is JsonObject) return "Models should be json objects containing and id string, and width, height, and eyeY floats"
				val id = ((model["id"] ?: return "All models should contain an id") as? JsonPrimitive
					?: return "Model ids should be strings").content
				val width = ((model["width"] ?: return "Models $id should contain a width") as? JsonPrimitive
					?: return "Model $id width should be a float").floatOrNull
					?: return "Could not parse width in model $id"
				val height = ((model["height"] ?: return "Models $id should contain a height") as? JsonPrimitive
					?: return "Model $id height should be a float").floatOrNull
					?: return "Could not parse height in model $id"
				val eyeY = ((model["eyeY"] ?: return "Models $id should contain an eyeY") as? JsonPrimitive
					?: return "Model $id eyeY should be a float").floatOrNull
					?: return "Could not parse eyeY in model $id"
				set.add(CharacterModel(id, width, height, eyeY))
			}
			return null
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