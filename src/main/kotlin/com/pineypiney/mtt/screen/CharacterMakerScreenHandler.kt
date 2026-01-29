package com.pineypiney.mtt.screen

import com.pineypiney.mtt.dnd.Background
import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.classes.DNDClass
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.dnd.traits.AbilityPointBuyPart
import com.pineypiney.mtt.dnd.traits.Source
import com.pineypiney.mtt.dnd.traits.TraitPart
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler

class CharacterMakerScreenHandler(syncID: Int) : ScreenHandler(MTTScreenHandlers.CHARACTER_MAKER_SCREEN_HANDLER, syncID) {

	constructor(syncID: Int, inventory: PlayerInventory?): this(syncID)

	val sheet = CharacterSheet()

	val raceParts = mutableListOf<MutableList<TraitPart>>()
	val classTraits = mutableListOf<MutableList<TraitPart>>()
	val backgroundTraits = mutableListOf<MutableList<TraitPart>>()
	val abilityPart = AbilityPointBuyPart()

	fun updateTrait(src: String, index: Int, part: Int, values: List<String>){
		if(src == "ability"){
			abilityPart.updateValues(values)
			return
		}
		val trait = when(src){
			"race" -> raceParts.getOrNull(index) ?: return
			"class" -> classTraits.getOrNull(index) ?: return
			"background" -> backgroundTraits.getOrNull(index) ?: return
			else -> return
		}
		val part = trait.getOrNull(part) ?: return
		part.updateValues(values)
	}

	fun setRace(race: Race){
		sheet.race = race
		raceParts.clear()
		raceParts.addAll(race.getAllTraits().map { it.getParts().toMutableList() })
	}

	fun setClass(clazz: DNDClass){
		sheet.classes.clear()
		sheet.classes[clazz] = 1
		classTraits.clear()
		classTraits.addAll(clazz.coreTraits.map { it.getParts().toMutableList() })
	}

	fun setBackground(background: Background){
		sheet.background = background
		backgroundTraits.clear()
		backgroundTraits.addAll(background.compileTraits().map { it.getParts().toMutableList() })
	}

	fun applyTraits(){
		raceParts.forEach { trait ->
			trait.forEach { part -> part.apply(sheet, Source.RaceSource(sheet.race)) }
		}
		classTraits.forEach { trait ->
			trait.forEach { part -> part.apply(sheet, Source.ClassSource(sheet.classes.keys.first())) }
		}
		backgroundTraits.forEach { trait ->
			trait.forEach { part -> part.apply(sheet, Source.BackgroundSource(sheet.background)) }
		}
		abilityPart.apply(sheet)

		sheet.maxHealth = sheet.classes.keys.first().healthDie
		sheet.health = sheet.maxHealth
	}

	override fun quickMove(player: PlayerEntity, slot: Int): ItemStack {
		return ItemStack.EMPTY
	}

	override fun canUse(player: PlayerEntity): Boolean = true
}