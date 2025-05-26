package com.pineypiney.mtt.screen

import com.pineypiney.mtt.dnd.Background
import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.classes.DNDClass
import com.pineypiney.mtt.dnd.species.Species
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler

class CharacterMakerScreenHandler(syncID: Int) : ScreenHandler(MTTScreenHandlers.CHARACTER_MAKER_SCREEN_HANDLER, syncID) {

	constructor(syncID: Int, inventory: PlayerInventory?): this(syncID)

	val sheet = CharacterSheet()

	fun setSpecies(species: Species){

	}

	fun setClass(clazz: DNDClass){

	}

	fun setBackground(background: Background){

	}

	override fun quickMove(player: PlayerEntity, slot: Int): ItemStack {
		return ItemStack.EMPTY
	}

	override fun canUse(player: PlayerEntity): Boolean = true
}