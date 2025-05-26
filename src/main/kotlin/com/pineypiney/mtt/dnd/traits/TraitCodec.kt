package com.pineypiney.mtt.dnd.traits

import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.DamageType
import com.pineypiney.mtt.dnd.proficiencies.Proficiency
import com.pineypiney.mtt.dnd.traits.feats.Feat
import io.netty.buffer.ByteBuf
import kotlinx.serialization.json.*
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs

interface TraitCodec<T, C: TraitComponent<T, C>> : PacketCodec<ByteBuf, C>  {

	val ID: String
	fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>)
	fun apply(sheet: CharacterSheet, list: Set<T>, source: Source)

	companion object {
		val TYPE_CODEC = object : TraitCodec<CreatureType, CreatureTypeComponent>{
			override val ID: String = "type"
			override fun decode(buf: ByteBuf): CreatureTypeComponent {
				return CreatureTypeComponent(decodeTrait(buf, PacketCodecs.STRING, ::apply, CreatureType::valueOf))
			}
			override fun encode(buf: ByteBuf, value: CreatureTypeComponent) {
				encodeTrait(buf, value.type, PacketCodecs.STRING, CreatureType::name)
			}
			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				getJsonTrait(json, ::apply){ CreatureType.valueOf(it.content.uppercase()) }.forEach { list.add(CreatureTypeComponent(it)) }
			}
			override fun apply(sheet: CharacterSheet, list: Set<CreatureType>, source: Source) {

			}
		}
		val SIZE_CODEC = object : TraitCodec<Size, SizeComponent>{
			override val ID = "size"
			override fun decode(buf: ByteBuf): SizeComponent {
				return SizeComponent(decodeTrait(buf, PacketCodecs.STRING, ::apply, Size::fromString))
			}

			override fun encode(buf: ByteBuf, value: SizeComponent) {
				encodeTrait(buf, value.size, PacketCodecs.STRING, Size::name)
			}
			
			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				getJsonTrait(json, ::apply) { Size.fromString(it.content) }.forEach { list.add(SizeComponent(it)) }
			}

			override fun apply(sheet: CharacterSheet, list: Set<Size>, source: Source) {

			}
		}
		val SPEED_CODEC = object : TraitCodec<Int, SpeedComponent>{
			override val ID = "speed"
			override fun decode(buf: ByteBuf): SpeedComponent {
				return SpeedComponent(decodeTrait(buf, PacketCodecs.INTEGER, ::apply))
			}

			override fun encode(buf: ByteBuf, value: SpeedComponent) {
				encodeTrait(buf, value.speed, PacketCodecs.INTEGER)
			}

			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				getJsonTrait(json, ::apply, emptyMap(), JsonPrimitive::int).forEach { list.add(SpeedComponent(it)) }
			}

			override fun apply(sheet: CharacterSheet, list: Set<Int>, source: Source) {

			}
		}
		val MODEL_CODEC = object : TraitCodec<String, ModelComponent> {
			override val ID = "model"
			override fun decode(buf: ByteBuf): ModelComponent {
				return ModelComponent(decodeTrait(buf, PacketCodecs.STRING, ::apply))
			}

			override fun encode(buf: ByteBuf, value: ModelComponent) {
				encodeTrait(buf, value.model, PacketCodecs.STRING)
			}

			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				getJsonTrait(json, ::apply, emptyMap(), JsonPrimitive::content).forEach { list.add(ModelComponent(it)) }
			}

			override fun apply(sheet: CharacterSheet, list: Set<String>, source: Source) {

			}
		}
		val LANGUAGE_CODEC = object : TraitCodec<String, LanguageComponent> {
			override val ID = "language"
			override fun decode(buf: ByteBuf): LanguageComponent {
				return LanguageComponent(decodeTrait(buf, PacketCodecs.STRING, ::apply))
			}

			override fun encode(buf: ByteBuf, value: LanguageComponent) {
				encodeTrait(buf, value.languages, PacketCodecs.STRING)
			}

			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				getJsonTrait(json, ::apply, emptyMap(), JsonPrimitive::content).forEach { list.add(LanguageComponent(it)) }
			}

			override fun apply(sheet: CharacterSheet, list: Set<String>, source: Source) {

			}
		}

		val DARK_VISION_CODEC = object : TraitCodec<Int, DarkVisionComponent> {
			override val ID = "dark_vision"
			override fun decode(buf: ByteBuf): DarkVisionComponent {
				return DarkVisionComponent(PacketCodecs.INTEGER.decode(buf))
			}

			override fun encode(buf: ByteBuf, value: DarkVisionComponent) {
				PacketCodecs.INTEGER.encode(buf, value.darkVision)
			}

			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				list.add(DarkVisionComponent(json.jsonPrimitive.int))
			}

			override fun apply(sheet: CharacterSheet, list: Set<Int>, source: Source) {

			}
		}
		val HEALTH_BONUS_CODEC = object : TraitCodec<Int, HealthBonusComponent>{

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

			override fun apply(sheet: CharacterSheet, list: Set<Int>, source: Source) {

			}
		}
		val ADVANTAGE_CODEC = object : TraitCodec<String, AdvantageComponent> {
			override val ID = "advantage"
			override fun decode(buf: ByteBuf): AdvantageComponent {
				return AdvantageComponent(PacketCodecs.STRING.decode(buf), decodeTrait(buf, PacketCodecs.STRING, ::apply))
			}

			override fun encode(buf: ByteBuf, value: AdvantageComponent) {
				PacketCodecs.STRING.encode(buf, value.type)
				encodeTrait(buf, value.advantages, PacketCodecs.STRING)
			}

			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				when (json) {
					is JsonObject -> {
						val type = json["type"]?.jsonPrimitive?.content ?: return
						if (json.contains("advantage")) {
							getJsonTrait(json["advantage"]!!, ::apply, emptyMap(), JsonPrimitive::content).forEach {
								list.add(AdvantageComponent(type, it))
							}
						}
					}

					is JsonArray -> json.forEach { readFromJson(it, list) }
					else -> {}
				}
			}

			override fun apply(sheet: CharacterSheet, list: Set<String>, source: Source) {

			}
		}
		val RESISTANCE_CODEC = object : TraitCodec<String, ResistanceComponent> {
			override val ID = "resistance"
			override fun decode(buf: ByteBuf): ResistanceComponent {
				return ResistanceComponent(PacketCodecs.STRING.decode(buf), decodeTrait(buf, PacketCodecs.STRING, ::apply))
			}

			override fun encode(buf: ByteBuf, value: ResistanceComponent) {
				PacketCodecs.STRING.encode(buf, value.type)
				encodeTrait(buf, value.name, PacketCodecs.STRING)
			}

			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				when (json) {
					is JsonObject -> {
						val type = json["type"]?.jsonPrimitive?.content ?: "damage"
						if (json.contains("resistance")) {
							getJsonTrait(json["resistance"]!!, ::apply, damageMap, JsonPrimitive::content).forEach {
								list.add(ResistanceComponent(type, it))
							}
						}
					}

					is JsonArray -> json.forEach { readFromJson(it, list) }
					is JsonPrimitive -> list.add(ResistanceComponent("damage", SetTraits(json.content, ::apply)))
				}
			}

			override fun apply(sheet: CharacterSheet, list: Set<String>, source: Source) {

			}

			val damageMap = mapOf(
				"all" to DamageType.list.map { it.id }
			)
		}
		val PROFICIENCY_CODEC = object : TraitCodec<Proficiency, ProficiencyComponent> {
			override fun decode(buf: ByteBuf): ProficiencyComponent {
				return ProficiencyComponent(PacketCodecs.STRING.decode(buf), decodeTrait(buf, PacketCodecs.STRING, ::apply){ id ->
					Proficiency.set.firstOrNull { it.id == id } ?: Proficiency.NONE
				})
			}

			override fun encode(buf: ByteBuf, value: ProficiencyComponent) {
				PacketCodecs.STRING.encode(buf, value.type)
				encodeTrait(buf, value.name, PacketCodecs.STRING, Proficiency::id)
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
							getJsonTrait(json["proficiency"]!!, ::apply, shortCuts){ Proficiency.findById(it.content) }.forEach {
								list.add(ProficiencyComponent(type, it))
							}
						}
					}

					is JsonArray -> json.forEach { readFromJson(it, list) }
					else -> {}
				}
			}

			override fun apply(sheet: CharacterSheet, list: Set<Proficiency>, source: Source) {

			}

			val skillMap = mapOf("all" to Proficiency.findByType("skill"))
			val weaponMap = mapOf(
				"all" to Proficiency.findByType("weapon"),
				"melee" to Proficiency.findByTag("melee"),
				"ranged" to Proficiency.findByTag("ranged"),
			)
		}
		val CUSTOM_TRAIT_CODEC = object : TraitCodec<String, CustomTraitComponent> {
			override val ID = "custom"
			override fun decode(buf: ByteBuf): CustomTraitComponent {
				return CustomTraitComponent(PacketCodecs.STRING.decode(buf))
			}

			override fun encode(buf: ByteBuf, value: CustomTraitComponent) {
				PacketCodecs.STRING.encode(buf, value.id)
			}


			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				if (json !is JsonObject) return
				val id = json["id"]?.jsonPrimitive?.content ?: return
				list.add(CustomTraitComponent(id))
			}

			override fun apply(sheet: CharacterSheet, list: Set<String>, source: Source) {

			}
		}
		val SPELLCASTING_ABILITY_CODEC = object : TraitCodec<Ability, SpellcastingAbilityComponent> {
			override val ID = "spell_ability"
			override fun decode(buf: ByteBuf): SpellcastingAbilityComponent {
				return SpellcastingAbilityComponent(decodeTrait(buf, PacketCodecs.STRING, ::apply, Ability::valueOf))
			}

			override fun encode(buf: ByteBuf, value: SpellcastingAbilityComponent) {
				encodeTrait(buf, value.ability, PacketCodecs.STRING, Ability::name)
			}


			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				getJsonTrait(json, ::apply) { Ability.get(it.content.uppercase()) }.forEach {
					list.add(
						SpellcastingAbilityComponent(it)
					)
				}
			}

			override fun apply(sheet: CharacterSheet, list: Set<Ability>, source: Source) {

			}
		}
		val SPELL_CODEC = object : TraitCodec<String, SpellComponent> {
			override val ID = "spell"
			override fun decode(buf: ByteBuf): SpellComponent {
				return SpellComponent(PacketCodecs.INTEGER.decode(buf), decodeTrait(buf, PacketCodecs.STRING, ::apply))
			}

			override fun encode(buf: ByteBuf, value: SpellComponent) {
				PacketCodecs.INTEGER.encode(buf, value.unlockLevel)
				encodeTrait(buf, value.spell, PacketCodecs.STRING)
			}


			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				when (json) {
					is JsonPrimitive -> list.add(SpellComponent(1, SetTraits(json.content, ::apply)))
					is JsonObject -> {
						val spellJson = json["spell"] ?: return
						val unlockLevel = json["level"]?.jsonPrimitive?.intOrNull ?: 1
						when (spellJson) {
							is JsonPrimitive -> list.add(SpellComponent(unlockLevel, SetTraits(spellJson.content, ::apply)))
							is JsonArray -> {
								val spells = spellJson.filterIsInstance<JsonPrimitive>().map { it.content }
								if (spells.isNotEmpty()) list.add(SpellComponent(unlockLevel, SetTraits(spells.toSet(), ::apply)))
							}

							else -> {}
						}
					}

					is JsonArray -> {
						json.forEach { readFromJson(it, list) }
					}
				}
			}

			override fun apply(sheet: CharacterSheet, list: Set<String>, source: Source) {

			}
		}
		val FEAT_CODEC = object : TraitCodec<Feat, FeatComponent> {
			override val ID = "feat"
			override fun decode(buf: ByteBuf): FeatComponent {
				return FeatComponent(decodeTrait(buf, PacketCodecs.STRING, ::apply, Feat::getById))
			}

			override fun encode(buf: ByteBuf, value: FeatComponent) {
				encodeTrait(buf, value.feat, PacketCodecs.STRING, Feat::id)
			}

			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				getJsonTrait(json, ::apply, emptyMap()){ Feat.getById(it.content) }.forEach { list.add(FeatComponent(it)) }
			}

			override fun apply(sheet: CharacterSheet, list: Set<Feat>, source: Source) {

			}
		}
		val SUBSPECIES_ID_CODEC = object : TraitCodec<String, SubspeciesIDComponent> {
			override val ID = "sub_species_id"
			override fun decode(buf: ByteBuf): SubspeciesIDComponent {
				return SubspeciesIDComponent(decodeTrait(buf, PacketCodecs.STRING, ::apply))
			}
			override fun encode(buf: ByteBuf, value: SubspeciesIDComponent) {
				encodeTrait(buf, value.type, PacketCodecs.STRING)
			}

			override fun readFromJson(json: JsonElement, list: MutableList<TraitComponent<*, *>>) {
				getJsonTrait(json, ::apply, emptyMap(), JsonPrimitive::content).forEach { list.add(SubspeciesIDComponent(it)) }
			}

			override fun apply(sheet: CharacterSheet, list: Set<String>, source: Source) {

			}
		}
	}
}