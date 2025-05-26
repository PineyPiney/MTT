package com.pineypiney.mtt.dnd.traits.feats

abstract class Feat(val id: String) {

	init {
		allFeats.add(this)
	}

	companion object {
		val allFeats: MutableSet<Feat> = mutableSetOf()
		fun getById(id: String) = allFeats.firstOrNull { it.id == id } ?: Feats.None
	}
}