package com.pineypiney.mtt.item.dnd

import com.pineypiney.mtt.dnd.DNDEngine
import com.pineypiney.mtt.dnd.characters.Character
import net.minecraft.item.Item
import net.minecraft.util.ActionResult

open class DNDItem(settings: Settings) : Item(settings) {
	open fun use(engine: DNDEngine, character: Character): ActionResult = ActionResult.PASS
}