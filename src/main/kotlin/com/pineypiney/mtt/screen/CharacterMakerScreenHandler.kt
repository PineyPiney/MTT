package com.pineypiney.mtt.screen

import com.pineypiney.mtt.dnd.Background
import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.classes.DNDClass
import com.pineypiney.mtt.dnd.species.Species
import com.pineypiney.mtt.dnd.traits.Trait
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler

class CharacterMakerScreenHandler(syncID: Int) : ScreenHandler(MTTScreenHandlers.CHARACTER_MAKER_SCREEN_HANDLER, syncID) {

	constructor(syncID: Int, inventory: PlayerInventory?): this(syncID)

	val sheet = CharacterSheet()

	val speciesTraits = mutableListOf<Trait<*>>()
	val classTraits = mutableListOf<Trait<*>>()
	val backgroundTraits = mutableListOf<Trait<*>>()

	fun setSpecies(species: Species){
		sheet.species = species
	}

	fun setClass(clazz: DNDClass){
		sheet.classes.clear()
		sheet.classes[clazz] = 1
	}

	fun setBackground(background: Background){

	}

	override fun quickMove(player: PlayerEntity, slot: Int): ItemStack {
		return ItemStack.EMPTY
	}

	override fun canUse(player: PlayerEntity): Boolean = true
}