package com.pineypiney.mtt.dnd.traits.feats

import com.pineypiney.mtt.dnd.traits.feats.Feats.Companion.allFeats
import com.pineypiney.mtt.dnd.traits.feats.Feats.None

abstract class Feat(val id: String) {

	companion object {
		fun findById(id: String) = allFeats.firstOrNull { it.id == id } ?: None
	}
}