package com.pineypiney.mtt.dnd.traits.proficiencies

import net.minecraft.network.codec.PacketCodecs

class Proficiency(val id: String, val type: String, val tags: Set<String>) {

	constructor(id: String, type: String, vararg tags: String): this(id, type, tags.toSet())

	init {
		set.add(this)
	}

	companion object {

		val set: MutableSet<Proficiency> = mutableSetOf()

		val STRENGTH = Proficiency("strength", "ability")
		val DEXTERITY = Proficiency("dexterity", "ability")
		val CONSTITUTION = Proficiency("constitution", "ability")
		val INTELLIGENCE = Proficiency("intelligence", "ability")
		val WISDOM = Proficiency("wisdom", "ability")
		val CHARISMA = Proficiency("charisma", "ability")

		val ACROBATICS = Proficiency("acrobatics", "skill")
		val ANIMAL_HANDLING = Proficiency("animal_handling", "skill")
		val ARCANA = Proficiency("arcana", "skill")
		val ATHLETICS = Proficiency("athletics", "skill")
		val DECEPTION = Proficiency("deception", "skill")
		val HISTORY = Proficiency("history", "skill")
		val INSIGHT = Proficiency("insight", "skill")
		val INTIMIDATION = Proficiency("intimidation", "skill")
		val INVESTIGATION = Proficiency("investigation", "skill")
		val MEDICINE = Proficiency("medicine", "skill")
		val NATURE = Proficiency("nature", "skill")
		val PERCEPTION = Proficiency("perception", "skill")
		val PERFORMANCE = Proficiency("performance", "skill")
		val PERSUASION = Proficiency("persuasion", "skill")
		val RELIGION = Proficiency("religion", "skill")
		val SLEIGHT_OF_HAND = Proficiency("sleight_of_hand", "skill")
		val STEALTH = Proficiency("stealth", "skill")
		val SURVIVAL = Proficiency("survival", "skill")

		val NONE = Proficiency("none", "none")
		val SIMPLE = Proficiency("simple", "weapon")
		val MARTIAL = Proficiency("martial", "weapon")
		val CLUB = Proficiency("club", "weapon", "simple", "melee")
		val GREAT_CLUB = Proficiency("great_club", "weapon", "simple", "melee")
		val DAGGER = Proficiency("dagger", "weapon", "simple", "melee")
		val HAND_AXE = Proficiency("hand_axe", "weapon", "simple", "melee")
		val JAVELIN = Proficiency("javelin", "weapon", "simple", "melee")
		val LIGHT_HAMMER = Proficiency("light_hammer", "weapon", "simple", "melee")
		val MACE = Proficiency("mace", "weapon", "simple", "melee")
		val QUARTER_STAFF = Proficiency("quarter_staff", "weapon", "simple", "melee")
		val SICKLE = Proficiency("sickle", "weapon", "simple", "melee")
		val SPEAR = Proficiency("spear", "weapon", "simple", "melee")
		val SHORT_SWORD = Proficiency("short_sword", "weapon", "martial", "melee")
		val LONG_SWORD = Proficiency("long_sword", "weapon", "martial", "melee")
		val GREAT_SWORD = Proficiency("great_sword", "weapon", "martial", "melee")
		val LIGHT_CROSSBOW = Proficiency("light_crossbow", "weapon", "simple", "ranged")
		val DART = Proficiency("dart", "weapon", "simple", "ranged")
		val SHORTBOW = Proficiency("shortbow", "weapon", "simple", "ranged")
		val SLING = Proficiency("sling", "weapon", "simple", "ranged")
		val BLOWGUN = Proficiency("blowgun", "weapon", "martial", "ranged")
		val HAND_CROSSBOW = Proficiency("hand_crossbow", "weapon", "martial", "ranged")
		val HEAVY_CROSSBOW = Proficiency("heavy_crossbow", "weapon", "martial", "ranged")
		val LONGBOW = Proficiency("longbow", "weapon", "martial", "ranged")
		val NET = Proficiency("net", "weapon", "martial", "ranged")

		val SHIELDS = Proficiency("shields", "armour")
		val LIGHT_ARMOUR = Proficiency("light_armour", "armour")
		val MEDIUM_ARMOUR = Proficiency("medium_armour", "armour")
		val HEAVY_ARMOUR = Proficiency("heavy_armour", "armour")

		val ALCHEMISTS_SUPPLIES = Proficiency("alchemists_supplies", "tool", "artisan")
		val BREWERS_SUPPLIES = Proficiency("brewers_supplies", "tool", "artisan")
		val CALLIGRAPHERS_SUPPLIES = Proficiency("calligraphers_supplies", "tool", "artisan")
		val CARPENTERS_TOOLS = Proficiency("carpenters_tools", "tool", "artisan")
		val CARTOGRAPHERS_TOOLS = Proficiency("cartographers_tools", "tool", "artisan")
		val COBBLERS_TOOLS = Proficiency("cobblers_tools", "tool", "artisan")
		val COOKS_UTENSILS = Proficiency("cooks_utensils", "tool", "artisan")
		val GLASSBLOWERS_TOOLS = Proficiency("glassblowers_tools", "tool", "artisan")
		val JEWELERS_TOOLS = Proficiency("jewelers_tools", "tool", "artisan")
		val LEATHERWORKERS_TOOLS = Proficiency("leatherworkers_tools", "tool", "artisan")
		val MASONS_TOOLS = Proficiency("masons_tools", "tool", "artisan")
		val PAINTERS_SUPPLIES = Proficiency("painters_supplies", "tool", "artisan")
		val POTTERS_TOOLS = Proficiency("potters_tools", "tool", "artisan")
		val SMITHS_TOOLS = Proficiency("smiths_tools", "tool", "artisan")
		val TINKERS_TOOLS = Proficiency("tinkers_tools", "tool", "artisan")
		val WEAVERS_TOOLS = Proficiency("weavers_tools", "tool", "artisan")
		val WOODCARVERS_TOOLS = Proficiency("woodcarvers_tools", "tool", "artisan")

		val DISGUISE_KIT = Proficiency("disguise_kit", "tool", "other")
		val FORGERY_KIT = Proficiency("forgery_kit", "tool", "other")
		val HERBALISM_KIT = Proficiency("herbalism_kit", "tool", "other")
		val NAVIGATORS_TOOLS = Proficiency("navigators_tools", "tool", "other")
		val POISONERS_KIT = Proficiency("poisoners_kit", "tool", "other")
		val THIEVES_TOOLS = Proficiency("thieves_tools", "tool", "other")

		val DICE_SET = Proficiency("dice_set", "tool", "game")
		val DRAGONCHESS_SET = Proficiency("dragonchess_set", "tool", "game")
		val PLAYING_CARD_SET = Proficiency("playing_card_set", "tool", "game")
		val THREE_DRAGON_ANTE_SET = Proficiency("three_dragon_ante_set", "tool", "game")

		val BAGPIPES = Proficiency("bagpipes", "tool", "instrument")
		val DRUM = Proficiency("drum", "tool", "instrument")
		val DULCIMER = Proficiency("dulcimer", "tool", "instrument")
		val FLUTE = Proficiency("flute", "tool", "instrument")
		val HORN = Proficiency("horn", "tool", "instrument")
		val LUTE = Proficiency("lute", "tool", "instrument")
		val LYRE = Proficiency("lyre", "tool", "instrument")
		val PAN_FLUTE = Proficiency("pan_flute", "tool", "instrument")
		val SHAWM = Proficiency("shawm", "tool", "instrument")
		val VIOL = Proficiency("viol", "tool", "instrument")

		fun findById(id: String) = set.firstOrNull{ it.id == id } ?: NONE
		fun findByType(type: String) = set.filter { it.type == type }
		fun findByTag(tag: String) = set.filter { it.tags.contains(tag) }

		val CODEC = PacketCodecs.STRING.xmap(::findById, Proficiency::id)
	}
}