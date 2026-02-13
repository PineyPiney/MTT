package com.pineypiney.mtt.client.render.entity.state

import com.pineypiney.mtt.dnd.characters.Character
import net.minecraft.client.render.entity.state.BipedEntityRenderState

open class DNDBipedEntityRenderState : BipedEntityRenderState() {
	lateinit var character: Character

	fun ready() = ::character.isInitialized
}