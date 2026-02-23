package com.pineypiney.mtt.client.gui.screens

import com.pineypiney.mtt.client.gui.widget.DynamicWidgets
import com.pineypiney.mtt.client.gui.widget.sheet_widgets.*
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.characters.CharacterSheet
import com.pineypiney.mtt.dnd.traits.Ability
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class CharacterSheetScreen(val sheet: CharacterSheet, val character: Character) : Screen(Text.translatable("mtt.screen.character_sheet")) {

	var x = 0
	var y = 0

	override fun init() {
		x = (width - 256) / 2
		y = (height - 252) / 2

		val classText = Text.literal("")
		for ((i, clazz) in sheet.classes.entries.withIndex()) {
			classText.append(Text.translatable("mtt.class.${clazz.key.id}"))
			classText.append(" ${clazz.value}")
			if (i < sheet.classes.size - 1) classText.append(" / ")
		}

		val raceInfo = InfoTextWidget(
			this,
			x + 8,
			y + 30,
			character.race.getText(),
			character.race.getText(sheet.subrace)
		)
		val classInfo = InfoTextWidget(this, x + raceInfo.width + 16, y + 30, classText, classText)
		addDrawableChild(raceInfo)
		addDrawableChild(classInfo)

		addDrawableChild(
			AbilityWidget(
				this,
				Ability.STRENGTH,
				character,
				x + 8,
				y + 48,
				28,
				38,
				Text.translatable("mtt.ability.strength")
			)
		)
		addDrawableChild(
			AbilityWidget(
				this,
				Ability.DEXTERITY,
				character,
				x + 40,
				y + 48,
				28,
				38,
				Text.translatable("mtt.ability.dexterity")
			)
		)
		addDrawableChild(
			AbilityWidget(
				this,
				Ability.CONSTITUTION,
				character,
				x + 8,
				y + 88,
				28,
				38,
				Text.translatable("mtt.ability.constitution")
			)
		)
		addDrawableChild(
			AbilityWidget(
				this,
				Ability.INTELLIGENCE,
				character,
				x + 40,
				y + 88,
				28,
				38,
				Text.translatable("mtt.ability.intelligence")
			)
		)
		addDrawableChild(
			AbilityWidget(
				this,
				Ability.WISDOM,
				character,
				x + 8,
				y + 128,
				28,
				38,
				Text.translatable("mtt.ability.wisdom")
			)
		)
		addDrawableChild(
			AbilityWidget(
				this,
				Ability.CHARISMA,
				character,
				x + 40,
				y + 128,
				28,
				38,
				Text.translatable("mtt.ability.charisma")
			)
		)
		addDrawableChild(
			ArmourClassWidget(
				this,
				character,
				x + 76,
				y + 45,
				40,
				40,
				Text.translatable("mtt.armour_class")
			)
		)
		addDrawableChild(
			TraitWidget(
				this,
				character,
				"mtt.initiative",
				24,
				{ char -> char.getInitiativeModifier().let { if (it > 0) "+$it" else it.toString() } },
				5,
				x + 122,
				y + 48,
				28,
				35
			)
		)
		addDrawableChild(
			TraitWidget(
				this,
				character,
				"mtt.speed",
				24,
				{ char -> char.speed.toString() },
				5,
				x + 154,
				y + 48,
				28,
				35
			)
		)
		addDrawableChild(HealthWidget(this, character, x + 72, y + 88, 110, 35))
	}

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
		super.render(context, mouseX, mouseY, deltaTicks)
		renderForeground(context, mouseX, mouseY, deltaTicks)
	}

	override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
		super.renderBackground(context, mouseX, mouseY, deltaTicks)
		DynamicWidgets.drawThickBoxWithBorder(context, x, y, 256, 252)
	}

	fun renderForeground(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float){
		val nameText = Text.literal(character.name)
		context.matrices.pushMatrix()
		context.matrices.scale(2f, 2f)
		context.drawText(client?.textRenderer, nameText, x / 2 + 4, y / 2 + 4, -12566464, false)
		context.matrices.popMatrix()
	}
}