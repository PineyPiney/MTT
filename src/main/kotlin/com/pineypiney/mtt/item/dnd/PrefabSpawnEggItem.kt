package com.pineypiney.mtt.item.dnd

import com.pineypiney.mtt.dnd.server.DNDServerEngine
import com.pineypiney.mtt.util.getEngine
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult

class PrefabSpawnEggItem(settings: Settings) : Item(settings) {
	override fun useOnBlock(context: ItemUsageContext): ActionResult {
		if (context.world.isClient) return ActionResult.PASS
		val engine = context.world.getEngine() as DNDServerEngine
		val character = engine.prefabs.first().createCharacter(2, engine)
		character.pos = context.hitPos
		engine.addCharacter(character)
		return ActionResult.SUCCESS
	}
}