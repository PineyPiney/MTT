package com.pineypiney.mtt.dnd.traits

import com.pineypiney.mtt.dnd.traits.features.Feature

class FeatureTrait(val feature: Feature) : Trait<Feature>() {
	override val isReady: Boolean get() = true
	override val apply: ApplyTrait<Feature> = { set, src -> }
	override val values: Set<Feature> = setOf(feature)

}