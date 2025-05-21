package com.pineypiney.mtt.screen

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler

class CharacterMakerScreenHandler(syncID: Int) : ScreenHandler(MTTScreenHandlers.CHARACTER_MAKER_SCREEN_HANDLER, syncID) {

	constructor(syncID: Int, inventory: PlayerInventory?): this(syncID)

	override fun quickMove(
		player: PlayerEntity?,
		slot: Int
	): ItemStack? {
		return ItemStack.EMPTY
	}

	override fun canUse(player: PlayerEntity?): Boolean {
		return true
	}
}