package com.pineypiney.mtt.dnd.traits

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.traits.feats.Feat
import com.pineypiney.mtt.dnd.traits.proficiencies.Proficiency
import com.pineypiney.mtt.network.codec.MTTPacketCodecs.decodeList
import com.pineypiney.mtt.network.codec.MTTPacketCodecs.encodeCollection
import io.netty.buffer.ByteBuf
import kotlinx.serialization.json.*
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs

interface TraitCodec<T: Trait<T>> : PacketCodec<ByteBuf, T> {

	val ID: String

	// Abstractly overriding these functions prevents them from being nullable
	override fun decode(buf: ByteBuf): T
	abstract override fun encode(buf: ByteBuf, value: T)
	fun readFromJson(json: JsonElement, traits: MutableCollection<Trait<*>>)

	companion object {

		fun <T> decodeGivenAndOptions(buf: ByteBuf, codec: PacketCodec<ByteBuf, T>): GivenAndOptions<T> {
			val given = decodeList(buf, codec).toSet()
			val numChoices = PacketCodecs.INTEGER.decode(buf)
			val options = decodeList(buf, codec).toSet()
			return GivenAndOptions(given, numChoices, options)
		}
		fun <T, C> decodeGivenAndOptions(buf: ByteBuf, codec: PacketCodec<ByteBuf, C>, getter: (C) -> T): GivenAndOptions<T> {
			val given = decodeList(buf, codec, getter).toSet()
			val numChoices = PacketCodecs.INTEGER.decode(buf)
			val options = decodeList(buf, codec, getter).toSet()
			return GivenAndOptions(given, numChoices, options)
		}
		fun <T> encodeGivenAndOptions(buf: ByteBuf, data: GivenAndOptions<T>, codec: PacketCodec<ByteBuf, T>){
			encodeCollection(buf, data.given, codec)
			PacketCodecs.INTEGER.encode(buf, data.numChoices)
			encodeCollection(buf, data.options, codec)
		}
		fun <T, C> encodeGivenAndOptions(buf: ByteBuf, data: GivenAndOptions<T>, codec: PacketCodec<ByteBuf, C>, getter: (T) -> C){
			encodeCollection(buf, data.given, codec, getter)
			PacketCodecs.INTEGER.encode(buf, data.numChoices)
			encodeCollection(buf, data.options, codec, getter)
		}

		fun <T> readJsonList(json: JsonElement, getter: (JsonPrimitive) -> T): List<T>{
			return when(json){
				is JsonPrimitive -> listOf(getter(json))
				is JsonArray -> json.filterIsInstance<JsonPrimitive>().map(getter)
				is JsonObject -> {
					val options = json["options"] ?: return emptyList()
					readJsonList(options, getter)
				}
			}
		}

		fun <T> readJsonChoice(json: JsonObject, shortCuts: Map<String, List<T>> = emptyMap(), getter: (JsonPrimitive) -> T?): Pair<Int, Set<T>>{
			val numChoices = json["choices"]?.jsonPrimitive?.intOrNull ?: 1
			val entryStrings = json["options"]
			val options = mutableSetOf<T>()
			if (entryStrings is JsonArray) {
				for (entry in entryStrings) {
					val shortcutValues = shortCuts[entry.jsonPrimitive.content]
					if (shortcutValues == null) options.add(getter(entry.jsonPrimitive) ?: continue)
					else options.addAll(shortcutValues)
				}
			}
			else if (entryStrings is JsonPrimitive) {
				val shortcutValues = shortCuts[entryStrings.content]
				val value = getter(entryStrings)
				if (shortcutValues != null) options.addAll(shortcutValues)
				else if (value != null) options.add(value)
			}
			return numChoices to options
		}

		fun <T> readJsonGivenAndOptions(json: JsonElement, shortCuts: Map<String, List<T>> = emptyMap(), getter: (JsonPrimitive) -> T?): GivenAndOptions<T>{
			return when(json) {
				is JsonPrimitive -> {
					val option = getter(json)
					if(option != null) GivenAndOptions(setOf(option), 0, emptySet())
					else GivenAndOptions.empty()
				}
				is JsonObject -> {
					val (numChoices, options) = readJsonChoice(json, shortCuts, getter)
					GivenAndOptions(emptySet(), numChoices, options)
				}

				is JsonArray -> {
					val given = json.filterIsInstance<JsonPrimitive>().mapNotNull { getter(it) }.toSet()
					val choice = json.filterIsInstance<JsonObject>().firstOrNull()

					if(choice == null) GivenAndOptions(given, 0, emptySet())
					else {
						val (numChoices, options) = readJsonChoice(choice, shortCuts, getter)
						GivenAndOptions(given, numChoices, options)
					}
				}
			}
		}

		val CREATURE_TYPE_CODEC = object : TraitCodec<CreatureTypeTrait>{
			override val ID: String = "creature_type"

			override fun decode(buf: ByteBuf): CreatureTypeTrait {
				return CreatureTypeTrait(CreatureType.valueOf(PacketCodecs.STRING.decode(buf)))
			}
			override fun encode(buf: ByteBuf, value: CreatureTypeTrait) {
				PacketCodecs.STRING.encode(buf, value.type.name)
			}

			override fun readFromJson(json: JsonElement, traits: MutableCollection<Trait<*>>) {
				traits.add(CreatureTypeTrait(CreatureType.valueOf(json.jsonPrimitive.content.uppercase())))
			}
		}

		val SPEED_TRAIT = object : TraitCodec<SpeedTrait> {
			override val ID: String = "speed"

			override fun decode(buf: ByteBuf): SpeedTrait {
				return SpeedTrait(PacketCodecs.INTEGER.decode(buf))
			}

			override fun encode(buf: ByteBuf, value: SpeedTrait) {
				PacketCodecs.INTEGER.encode(buf, value.speed)
			}

			override fun readFromJson(json: JsonElement, traits: MutableCollection<Trait<*>>) {
				traits.add(SpeedTrait(json.jsonPrimitive.int))
			}
		}

		val SIZE_CODEC = object : TraitCodec<SizeTrait>{
			override val ID: String = "size"

			override fun decode(buf: ByteBuf): SizeTrait {
				return SizeTrait(decodeList(buf, PacketCodecs.STRING, Size::fromString).toSet())
			}
			override fun encode(buf: ByteBuf, value: SizeTrait) {
				encodeCollection(buf, value.options, PacketCodecs.STRING, Size::name)
			}

			override fun readFromJson(json: JsonElement, traits: MutableCollection<Trait<*>>) {
				traits.add(SizeTrait(readJsonList(json){ Size.fromString(it.content) }.toSet()))
			}
		}

		val MODEL_CODEC = object : TraitCodec<ModelTrait>{
			override val ID: String = "model"

			override fun decode(buf: ByteBuf): ModelTrait {
				return ModelTrait(decodeList(buf, PacketCodecs.STRING).toSet())
			}
			override fun encode(buf: ByteBuf, value: ModelTrait) {
				encodeCollection(buf, value.options, PacketCodecs.STRING)
			}

			override fun readFromJson(json: JsonElement, traits: MutableCollection<Trait<*>>) {
				traits.add(ModelTrait(readJsonList(json, JsonPrimitive::content).toSet()))
			}
		}

		val LANGUAGE_CODEC = object : TraitCodec<LanguageTrait> {
			override val ID: String = "language"

			override fun decode(buf: ByteBuf): LanguageTrait {
				return LanguageTrait(decodeGivenAndOptions(buf, PacketCodecs.STRING))
			}

			override fun encode(buf: ByteBuf, value: LanguageTrait) {
				encodeGivenAndOptions(buf, value.data, PacketCodecs.STRING)
			}

			override fun readFromJson(json: JsonElement, traits: MutableCollection<Trait<*>>) {
				traits.add(LanguageTrait(readJsonGivenAndOptions(json, emptyMap(), JsonPrimitive::content)))
			}
		}

		val DARK_VISION_TRAIT = object : TraitCodec<DarkVisionTrait> {
			override val ID: String = "dark_vision"

			override fun decode(buf: ByteBuf): DarkVisionTrait {
				return DarkVisionTrait(PacketCodecs.INTEGER.decode(buf))
			}

			override fun encode(buf: ByteBuf, value: DarkVisionTrait) {
				PacketCodecs.INTEGER.encode(buf, value.distance)
			}

			override fun readFromJson(json: JsonElement, traits: MutableCollection<Trait<*>>) {
				traits.add(DarkVisionTrait(json.jsonPrimitive.int))
			}
		}

		val ADVANTAGE_CODEC = object : TraitCodec<AdvantageTrait> {
			override val ID: String = "advantage"

			override fun decode(buf: ByteBuf): AdvantageTrait {
				return AdvantageTrait(PacketCodecs.STRING.decode(buf), decodeGivenAndOptions(buf, PacketCodecs.STRING))
			}

			override fun encode(buf: ByteBuf, value: AdvantageTrait) {
				PacketCodecs.STRING.encode(buf, value.type)
				encodeGivenAndOptions(buf, value.data, PacketCodecs.STRING)
			}

			override fun readFromJson(json: JsonElement, traits: MutableCollection<Trait<*>>) {
				when(json){
					is JsonPrimitive -> {}
					is JsonObject -> {
						val type = json.getValue("type").jsonPrimitive.content
						val map = when(type){
							"condition" -> emptyMap<String, List<String>>()
							else -> emptyMap()
						}
						traits.add(AdvantageTrait(type, readJsonGivenAndOptions(json.getValue("advantage"), map, JsonPrimitive::content)))
					}
					is JsonArray -> json.forEach { readFromJson(it, traits) }
				}
			}
		}

		val RESISTANCE_CODEC = object : TraitCodec<ResistanceTrait> {
			override val ID: String = "resistance"

			override fun decode(buf: ByteBuf): ResistanceTrait {
				return ResistanceTrait(decodeGivenAndOptions(buf, PacketCodecs.STRING))
			}

			override fun encode(buf: ByteBuf, value: ResistanceTrait) {
				encodeGivenAndOptions(buf, value.data, PacketCodecs.STRING)
			}

			override fun readFromJson(json: JsonElement, traits: MutableCollection<Trait<*>>) {
				traits.add(ResistanceTrait(readJsonGivenAndOptions(json, emptyMap(), JsonPrimitive::content)))
			}
		}

		val PROFICIENCY_CODEC = object : TraitCodec<ProficiencyTrait> {
			override val ID: String = "proficiency"

			override fun decode(buf: ByteBuf): ProficiencyTrait {
				val data = decodeGivenAndOptions(buf, PacketCodecs.STRING, Proficiency::findById)
				val first = data.given.firstOrNull() ?: data.options.firstOrNull() ?: Proficiency.NONE
				return ProficiencyTrait(first.type, data)
			}

			override fun encode(buf: ByteBuf, value: ProficiencyTrait) {
				encodeGivenAndOptions(buf, value.data, PacketCodecs.STRING, Proficiency::id)
			}

			override fun readFromJson(json: JsonElement, traits: MutableCollection<Trait<*>>) {
				when(json){
					is JsonPrimitive -> {}
					is JsonObject -> {

						val type = json.getValue("type").jsonPrimitive.content
						val map = when(type){
							"ability" -> abilityMap
							"skill" -> skillMap
							"weapon" -> weaponMap
							"armour" -> armourMap
							"tool" -> toolMap
							else -> emptyMap()
						}
						traits.add(ProficiencyTrait(type, readJsonGivenAndOptions(json.getValue("proficiency"), map){ Proficiency.findById(it.content) }))
					}
					is JsonArray -> json.forEach { readFromJson(it, traits) }
				}
			}

			val abilityMap = mapOf("all" to Proficiency.findByType("ability"))
			val skillMap = mapOf("all" to Proficiency.findByType("skill"))
			val weaponMap = mapOf(
				"all" to Proficiency.findByType("weapon"),
				"melee" to Proficiency.findByTag("melee"),
				"ranged" to Proficiency.findByTag("ranged"),
			)
			val armourMap = mapOf(
				"all" to Proficiency.findByType("armour")
			)
			val toolMap = mapOf(
				"all" to Proficiency.findByType("tool"),
				"artisan" to Proficiency.findByTag("artisan"),
				"game" to Proficiency.findByTag("game"),
				"instrument" to Proficiency.findByTag("instrument")
			)
		}

		val HEALTH_BONUS_CODEC = object : TraitCodec<HealthBonusTrait>{
			override val ID: String = "health_bonus"

			override fun decode(buf: ByteBuf): HealthBonusTrait {
				return HealthBonusTrait(PacketCodecs.INTEGER.decode(buf), PacketCodecs.INTEGER.decode(buf))
			}

			override fun encode(buf: ByteBuf, value: HealthBonusTrait) {
				PacketCodecs.INTEGER.encode(buf, value.base)
				PacketCodecs.INTEGER.encode(buf, value.perLevel)
			}

			override fun readFromJson(json: JsonElement, traits: MutableCollection<Trait<*>>) {
				when (json) {
					is JsonPrimitive -> traits.add(HealthBonusTrait(json.int, 0))
					is JsonObject -> {
						val base = json["base"]?.jsonPrimitive?.intOrNull ?: 0
						val level = json["level"]?.jsonPrimitive?.intOrNull ?: 0
						if(base != 0 || level != 0)traits.add(HealthBonusTrait(base, level))
					}
					else -> MTT.logger.warn("Cannot parse Health Bonus traits: json should be an object or int")
				}
			}
		}

		val SPELLCASTING_ABILITY_CODEC = object : TraitCodec<SpellcastingAbilityTrait>{
			override val ID: String = "spell_ability"

			override fun decode(buf: ByteBuf): SpellcastingAbilityTrait {
				return SpellcastingAbilityTrait(decodeList(buf, PacketCodecs.STRING, Ability::valueOf).toSet())
			}

			override fun encode(buf: ByteBuf, value: SpellcastingAbilityTrait) {
				encodeCollection(buf, value.options, PacketCodecs.STRING, Ability::name)
			}

			override fun readFromJson(json: JsonElement, traits: MutableCollection<Trait<*>>) {
				when(json){
					is JsonPrimitive -> traits.add(SpellcastingAbilityTrait(setOf(Ability.valueOf(json.content.uppercase()))))
					is JsonObject -> traits.add(SpellcastingAbilityTrait(readJsonChoice(json){ Ability.valueOf(it.content.uppercase()) }.second.toSet()))
					is JsonArray -> {}
				}

			}
		}

		val SPELL_CODEC = object : TraitCodec<SpellTrait>{
			override val ID: String = "spell"

			override fun decode(buf: ByteBuf): SpellTrait {
				return SpellTrait(PacketCodecs.INTEGER.decode(buf), decodeList(buf, PacketCodecs.STRING).toSet())
			}

			override fun encode(buf: ByteBuf, value: SpellTrait) {
				PacketCodecs.INTEGER.encode(buf, value.unlockLevel)
				encodeCollection(buf, value.spells, PacketCodecs.STRING)
			}

			override fun readFromJson(json: JsonElement, traits: MutableCollection<Trait<*>>) {
				when(json){
					is JsonPrimitive -> traits.add(SpellTrait(1, setOf(json.content)))
					is JsonObject -> {
						val level = json["level"]?.jsonPrimitive?.intOrNull ?: 1
						val spells = readJsonList(json["spell"]!!, JsonPrimitive::content)
						if(spells.isNotEmpty()) traits.add(SpellTrait(level, spells.toSet()))
					}
					is JsonArray -> json.forEach { readFromJson(it, traits) }
				}
			}

		}

		val ABILITY_IMPROVEMENT_CODEC = object : TraitCodec<AbilityImprovementTrait>{
			override val ID: String = "ability_score"

			override fun decode(buf: ByteBuf): AbilityImprovementTrait {
				return AbilityImprovementTrait(decodeList(buf, PacketCodecs.STRING, Ability::valueOf).toSet(), PacketCodecs.INTEGER.decode(buf))
			}

			override fun encode(buf: ByteBuf, value: AbilityImprovementTrait) {
				encodeCollection(buf, value.options, PacketCodecs.STRING, Ability::name)
				PacketCodecs.INTEGER.encode(buf, value.points)
			}

			override fun readFromJson(json: JsonElement, traits: MutableCollection<Trait<*>>) {
				when(json){
					is JsonPrimitive -> traits.add(AbilityImprovementTrait(setOf(Ability.valueOf(json.content.uppercase())), 1))
					is JsonObject -> {

					}
					is JsonArray -> json.forEach { readFromJson(it, traits) }
				}
			}

		}

		val FEAT_CODEC = object : TraitCodec<FeatTrait>{
			override val ID: String = "feat"

			override fun decode(buf: ByteBuf): FeatTrait {
				return FeatTrait(decodeList(buf, PacketCodecs.STRING){ Feat.findById(it) } .toSet())
			}

			override fun encode(buf: ByteBuf, value: FeatTrait) {
				encodeCollection(buf, value.feats, PacketCodecs.STRING, Feat::id)
			}

			override fun readFromJson(json: JsonElement, traits: MutableCollection<Trait<*>>) {
				when(json){
					is JsonPrimitive -> traits.add(FeatTrait(setOf(Feat.findById(json.content))))
					is JsonObject -> traits.add(FeatTrait(readJsonChoice(json){ Feat.findById(it.content) }.second.toSet()))
					is JsonArray -> {}
				}
			}
		}

		val CUSTOM_CODEC = object : TraitCodec<CustomTrait>{
			override val ID: String = "custom"

			override fun decode(buf: ByteBuf): CustomTrait {
				return CustomTrait(PacketCodecs.STRING.decode(buf))
			}
			override fun encode(buf: ByteBuf, value: CustomTrait) {
				PacketCodecs.STRING.encode(buf, value.id)
			}

			override fun readFromJson(json: JsonElement, traits: MutableCollection<Trait<*>>) {
				traits.add(CustomTrait(json.jsonPrimitive.content))
			}
		}
	}
}