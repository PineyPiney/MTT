package com.pineypiney.mtt.item.dnd

import com.pineypiney.mtt.dnd.server.DNDServerEngine
import com.pineypiney.mtt.util.getEngine
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.world.World

class CharacterKillStickItem(settings: Settings) : Item(settings) {

	override fun use(world: World, user: PlayerEntity, hand: Hand): ActionResult {
		return if (user.isSneaking && !world.isClient) {
			val engine = world.getEngine() as DNDServerEngine
			engine.addCharacter(engine.characterBin.restoreCharacter() ?: return ActionResult.PASS)
			ActionResult.SUCCESS_SERVER
		} else ActionResult.PASS
	}
}