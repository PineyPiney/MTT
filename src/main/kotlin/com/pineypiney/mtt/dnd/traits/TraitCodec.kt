package com.pineypiney.mtt.dnd.traits

import com.pineypiney.mtt.dnd.CreatureType
import com.pineypiney.mtt.dnd.DamageType
import com.pineypiney.mtt.dnd.Size
import com.pineypiney.mtt.dnd.stats.Ability
import com.pineypiney.mtt.dnd.stats.Skill
import com.pineypiney.mtt.item.dnd.equipment.WeaponType
import io.netty.buffer.ByteBuf
import kotlinx.serialization.json.*
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs

interface TraitCodec<C: TraitComponent<*, *>> : PacketCodec<ByteBuf, C>  {

	val ID: String
	fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>)

	companion object {
		val TYPE_CODEC = object : TraitCodec<CreatureTypeComponent>{
			override val ID: String = "type"
			override fun decode(buf: ByteBuf): CreatureTypeComponent {
				return CreatureTypeComponent(decodeTrait(buf, PacketCodecs.STRING, CreatureType::valueOf))
			}
			override fun encode(buf: ByteBuf, value: CreatureTypeComponent) {
				encodeTrait(buf, value.type, PacketCodecs.STRING, CreatureType::name)
			}
			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				getJsonTrait(json){ CreatureType.valueOf(it.content.uppercase()) }.forEach { list.add(CreatureTypeComponent(it)) }
			}
		}
		val SIZE_CODEC = object : TraitCodec<SizeComponent>{
			override fun decode(buf: ByteBuf): SizeComponent {
				return SizeComponent(decodeTrait(buf, PacketCodecs.STRING, Size::fromString))
			}

			override fun encode(buf: ByteBuf, value: SizeComponent) {
				encodeTrait(buf, value.size, PacketCodecs.STRING, Size::name)
			}

			
				override val ID = "size"
				override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
					getJsonTrait(json) { Size.fromString(it.content) }.forEach { list.add(SizeComponent(it)) }
				}
		}
		val SPEED_CODEC = object : TraitCodec<SpeedComponent>{
			override fun decode(buf: ByteBuf): SpeedComponent {
				return SpeedComponent(decodeTrait(buf, PacketCodecs.INTEGER))
			}

			override fun encode(buf: ByteBuf, value: SpeedComponent) {
				encodeTrait(buf, value.speed, PacketCodecs.INTEGER)
			}

			override val ID = "speed"
			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				getJsonTrait(json, emptyMap(), JsonPrimitive::int).forEach { list.add(SpeedComponent(it)) }
			}
		}
		val MODEL_CODEC = object : TraitCodec<ModelComponent> {
			override fun decode(buf: ByteBuf): ModelComponent {
				return ModelComponent(decodeTrait(buf, PacketCodecs.STRING))
			}

			override fun encode(buf: ByteBuf, value: ModelComponent) {
				encodeTrait(buf, value.model, PacketCodecs.STRING)
			}


			override val ID = "model"
			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				getJsonTrait(json, emptyMap(), JsonPrimitive::content).forEach { list.add(ModelComponent(it)) }
			}
		}
		val LANGUAGE_CODEC = object : TraitCodec<LanguageComponent> {
			override fun decode(buf: ByteBuf): LanguageComponent {
				return LanguageComponent(decodeTrait(buf, PacketCodecs.STRING))
			}

			override fun encode(buf: ByteBuf, value: LanguageComponent) {
				encodeTrait(buf, value.languages, PacketCodecs.STRING)
			}


			override val ID = "language"
			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				getJsonTrait(json, emptyMap(), JsonPrimitive::content).forEach { list.add(LanguageComponent(it)) }
			}
		}

		val DARK_VISION_CODEC = object : TraitCodec<DarkVisionComponent> {
			override fun decode(buf: ByteBuf): DarkVisionComponent {
				return DarkVisionComponent(PacketCodecs.INTEGER.decode(buf))
			}

			override fun encode(buf: ByteBuf, value: DarkVisionComponent) {
				PacketCodecs.INTEGER.encode(buf, value.darkVision)
			}


			override val ID = "dark_vision"
			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				list.add(DarkVisionComponent(json.jsonPrimitive.int))
			}
		}
		val HEALTH_BONUS_CODEC = object : TraitCodec<HealthBonusComponent>{

			override val ID = "health"

			override fun decode(buf: ByteBuf): HealthBonusComponent {
				return HealthBonusComponent(PacketCodecs.INTEGER.decode(buf), PacketCodecs.INTEGER.decode(buf))
			}

			override fun encode(buf: ByteBuf, value: HealthBonusComponent) {
				PacketCodecs.INTEGER.encode(buf, value.base)
				PacketCodecs.INTEGER.encode(buf, value.perLevel)
			}

			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				when (json) {
					is JsonPrimitive -> list.add(HealthBonusComponent(json.int, 0))
					is JsonObject -> {
						val base = json["base"]?.jsonPrimitive?.intOrNull ?: 0
						val level = json["level"]?.jsonPrimitive?.intOrNull ?: 0
						if (base != 0 || level != 0) list.add(HealthBonusComponent(base, level))
					}

					else -> {}
				}
			}
		}
		val ADVANTAGE_CODEC = object : TraitCodec<AdvantageComponent> {
			override fun decode(buf: ByteBuf): AdvantageComponent {
				return AdvantageComponent(PacketCodecs.STRING.decode(buf), decodeTrait(buf, PacketCodecs.STRING))
			}

			override fun encode(buf: ByteBuf, value: AdvantageComponent) {
				PacketCodecs.STRING.encode(buf, value.type)
				encodeTrait(buf, value.advantages, PacketCodecs.STRING)
			}


			override val ID = "advantage"
			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				when (json) {
					is JsonObject -> {
						val type = json["type"]?.jsonPrimitive?.content ?: return
						if (json.contains("advantage")) {
							getJsonTrait(json["advantage"]!!, emptyMap(), JsonPrimitive::content).forEach {
								list.add(AdvantageComponent(type, it))
							}
						}
					}

					is JsonArray -> json.forEach { readFromJson(it, list) }
					else -> {}
				}
			}
		}
		val RESISTANCE_CODEC = object : TraitCodec<ResistanceComponent> {
			override fun decode(buf: ByteBuf): ResistanceComponent {
				return ResistanceComponent(PacketCodecs.STRING.decode(buf), decodeTrait(buf, PacketCodecs.STRING))
			}

			override fun encode(buf: ByteBuf, value: ResistanceComponent) {
				PacketCodecs.STRING.encode(buf, value.type)
				encodeTrait(buf, value.name, PacketCodecs.STRING)
			}


			override val ID = "resistance"
			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				when (json) {
					is JsonObject -> {
						val type = json["type"]?.jsonPrimitive?.content ?: "damage"
						if (json.contains("resistance")) {
							getJsonTrait(json["resistance"]!!, damageMap, JsonPrimitive::content).forEach {
								list.add(ResistanceComponent(type, it))
							}
						}
					}

					is JsonArray -> json.forEach { readFromJson(it, list) }
					is JsonPrimitive -> list.add(ResistanceComponent("damage", SetTraits(json.content)))
				}
			}

			val damageMap = mapOf(
				"all" to DamageType.list.map { it.id }
			)
		}
		val PROFICIENCY_CODEC = object : TraitCodec<ProficiencyComponent> {
			override fun decode(buf: ByteBuf): ProficiencyComponent {
				return ProficiencyComponent(PacketCodecs.STRING.decode(buf), decodeTrait(buf, PacketCodecs.STRING))
			}

			override fun encode(buf: ByteBuf, value: ProficiencyComponent) {
				PacketCodecs.STRING.encode(buf, value.type)
				encodeTrait(buf, value.name, PacketCodecs.STRING)
			}


			override val ID = "proficiency"
			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				when (json) {
					is JsonObject -> {
						val type = json["type"]?.jsonPrimitive?.content ?: return
						if (json.contains("proficiency")) {
							val shortCuts = when(type){
								"skill" -> skillMap
								"weapon" -> weaponMap
								"tool" -> emptyMap()
								else -> emptyMap()
							}
							getJsonTrait(json["proficiency"]!!, shortCuts, JsonPrimitive::content).forEach {
								list.add(ProficiencyComponent(type, it))
							}
						}
					}

					is JsonArray -> json.forEach { readFromJson(it, list) }
					else -> {}
				}
			}

			val skillMap = mapOf("all" to Skill.entries.map { it.name.lowercase() })
			val weaponMap = mapOf(
				"all" to WeaponType.set.map { it.id },
				"simple" to WeaponType.set.filter { !it.martial }.map { it.id },
				"martial" to WeaponType.set.filter { it.martial }.map { it.id },
				"melee" to WeaponType.set.filter { !it.ranged }.map { it.id },
				"ranged" to WeaponType.set.filter { it.ranged }.map { it.id }
			)
		}
		val CUSTOM_TRAIT_CODEC = object : TraitCodec<CustomTraitComponent> {
			override fun decode(buf: ByteBuf): CustomTraitComponent {
				return CustomTraitComponent(PacketCodecs.STRING.decode(buf))
			}

			override fun encode(buf: ByteBuf, value: CustomTraitComponent) {
				PacketCodecs.STRING.encode(buf, value.id)
			}


			override val ID = "custom"
			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				if (json !is JsonObject) return
				val id = json["id"]?.jsonPrimitive?.content ?: return
				list.add(CustomTraitComponent(id))
			}
		}
		val SPELLCASTING_ABILITY_CODEC = object : TraitCodec<SpellcastingAbilityComponent> {
			override fun decode(buf: ByteBuf): SpellcastingAbilityComponent {
				return SpellcastingAbilityComponent(decodeTrait(buf, PacketCodecs.STRING, Ability::valueOf))
			}

			override fun encode(buf: ByteBuf, value: SpellcastingAbilityComponent) {
				encodeTrait(buf, value.ability, PacketCodecs.STRING, Ability::name)
			}


			override val ID = "spell_ability"
			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				getJsonTrait(json) { Ability.get(it.content.uppercase()) }.forEach {
					list.add(
						SpellcastingAbilityComponent(it)
					)
				}
			}
		}
		val SPELL_CODEC = object : TraitCodec<SpellComponent> {
			override fun decode(buf: ByteBuf): SpellComponent {
				return SpellComponent(PacketCodecs.INTEGER.decode(buf), decodeTrait(buf, PacketCodecs.STRING))
			}

			override fun encode(buf: ByteBuf, value: SpellComponent) {
				PacketCodecs.INTEGER.encode(buf, value.unlockLevel)
				encodeTrait(buf, value.spell, PacketCodecs.STRING)
			}


			override val ID = "spell"
			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				when (json) {
					is JsonPrimitive -> list.add(SpellComponent(1, SetTraits(json.content)))
					is JsonObject -> {
						val spellJson = json["spell"] ?: return
						val unlockLevel = json["level"]?.jsonPrimitive?.intOrNull ?: 1
						when (spellJson) {
							is JsonPrimitive -> list.add(SpellComponent(unlockLevel, SetTraits(spellJson.content)))
							is JsonArray -> {
								val spells = spellJson.filterIsInstance<JsonPrimitive>().map { it.content }
								if (spells.isNotEmpty()) list.add(SpellComponent(unlockLevel, SetTraits(spells)))
							}

							else -> {}
						}
					}

					is JsonArray -> {
						json.forEach { readFromJson(it, list) }
					}
				}
			}
		}
		val FEAT_CODEC = object : TraitCodec<FeatComponent> {
			override val ID = "feat"
			override fun decode(buf: ByteBuf): FeatComponent {
				return FeatComponent(decodeTrait(buf, PacketCodecs.STRING))
			}

			override fun encode(buf: ByteBuf, value: FeatComponent) {
				encodeTrait(buf, value.feat, PacketCodecs.STRING)
			}

			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				getJsonTrait(json, emptyMap(), JsonPrimitive::content).forEach { list.add(FeatComponent(it)) }
			}
		}
		val SUBSPECIES_ID_CODEC = object : TraitCodec<SubspeciesIDComponent> {
			override val ID = "sub_species_id"
			override fun decode(buf: ByteBuf): SubspeciesIDComponent {
				return SubspeciesIDComponent(decodeTrait(buf, PacketCodecs.STRING))
			}
			override fun encode(buf: ByteBuf, value: SubspeciesIDComponent) {
				encodeTrait(buf, value.type, PacketCodecs.STRING)
			}

			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				getJsonTrait(json, emptyMap(), JsonPrimitive::content).forEach { list.add(SubspeciesIDComponent(it)) }
			}
		}
	}
}