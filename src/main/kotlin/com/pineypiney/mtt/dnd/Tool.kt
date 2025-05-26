package com.pineypiney.mtt.dnd

class Tool(val id: String, val type: String, val tags: Set<String>) {

	constructor(id: String, type: String, vararg tags: String): this(id, type, tags.toSet())

	init {
		set.add(this)
	}

	companion object {
		val set = mutableSetOf<Tool>()

		val ALCHEMISTS_SUPPLIES = Tool("alchemists_supplies", "artisan")
		val BREWERS_SUPPLIES = Tool("brewers_supplies", "artisan")
		val CALLIGRAPHERS_SUPPLIES = Tool("calligraphers_supplies", "artisan")
		val CARPENTERS_TOOLS = Tool("carpenters_tools", "artisan")
		val CARTOGRAPHERS_TOOLS = Tool("cartographers_tools", "artisan")
		val COBBLERS_TOOLS = Tool("cobblers_tools", "artisan")
		val COOKS_UTENSILS = Tool("cooks_utensils", "artisan")
		val GLASSBLOWERS_TOOLS = Tool("glassblowers_tools", "artisan")
		val JEWELERS_TOOLS = Tool("jewelers_tools", "artisan")
		val LEATHERWORKERS_TOOLS = Tool("leatherworkers_tools", "artisan")
		val MASONS_TOOLS = Tool("masons_tools", "artisan")
		val PAINTERS_SUPPLIES = Tool("painters_supplies", "artisan")
		val POTTERS_TOOLS = Tool("potters_tools", "artisan")
		val SMITHS_TOOLS = Tool("smiths_tools", "artisan")
		val TINKERS_TOOLS = Tool("tinkers_tools", "artisan")
		val WEAVERS_TOOLS = Tool("weavers_tools", "artisan")
		val WOODCARVERS_TOOLS = Tool("woodcarvers_tools", "artisan")

		val DISGUISE_KIT = Tool("disguise_kit", "other")
		val FORGERY_KIT = Tool("forgery_kit", "other")
		val HERBALISM_KIT = Tool("herbalism_kit", "other")
		val NAVIGATORS_TOOLS = Tool("navigators_tools", "other")
		val POISONERS_KIT = Tool("poisoners_kit", "other")
		val THIEVES_TOOLS = Tool("thieves_tools", "other")

		val DICE_SET = Tool("dice_set", "game")
		val DRAGONCHESS_SET = Tool("dragonchess_set", "game")
		val PLAYING_CARD_SET = Tool("playing_card_set", "game")
		val THREE_DRAGON_ANTE_SET = Tool("three_dragon_ante_set", "game")

		val BAGPIPES = Tool("bagpipes", "instrument")
		val DRUM = Tool("drum", "instrument")
		val DULCIMER = Tool("dulcimer", "instrument")
		val FLUTE = Tool("flute", "instrument")
		val HORN = Tool("horn", "instrument")
		val LUTE = Tool("lute", "instrument")
		val LYRE = Tool("lyre", "instrument")
		val PAN_FLUTE = Tool("pan_flute", "instrument")
		val SHAWM = Tool("shawm", "instrument")
		val VIOL = Tool("viol", "instrument")
	}
}