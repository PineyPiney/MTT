package com.pineypiney.mtt.item

import com.pineypiney.mtt.dnd.DNDEngine
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.item.dnd.DNDItem
import net.minecraft.util.ActionResult

class SpellManagerItem(settings: Settings) : DNDItem(settings) {

	override fun use(engine: DNDEngine<*>, character: Character): ActionResult {
		engine.getEntityOfCharacter(character.uuid)?.openSpellBook()
		return ActionResult.SUCCESS
	}
}