package com.pineypiney.mtt.render.entity.state

import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.characters.SheetCharacter
import net.minecraft.client.render.entity.state.BipedEntityRenderState
import java.util.*

open class DNDBipedEntityRenderState : BipedEntityRenderState() {
	var name: String = ""
	var character: Character = SheetCharacter(CharacterSheet(), UUID.randomUUID())
}