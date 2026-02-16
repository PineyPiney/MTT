package com.pineypiney.mtt.client.render.entity.state

import com.pineypiney.mtt.dnd.spells.SpellShape
import net.minecraft.client.render.entity.state.EntityRenderState

open class ShapeEntityRenderState : EntityRenderState() {
	var shape: SpellShape = SpellShape.Single
	var colour: Int = -1
}