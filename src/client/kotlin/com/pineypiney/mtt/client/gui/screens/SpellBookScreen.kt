package com.pineypiney.mtt.client.gui.screens

import com.pineypiney.mtt.client.dnd.spell_selector.SpellSelector
import com.pineypiney.mtt.dnd.spells.Spell
import com.pineypiney.mtt.mixin.client.BookScreenAccessor
import com.pineypiney.mtt.mixin_interfaces.DNDClient
import net.minecraft.client.gui.Click
import net.minecraft.client.gui.screen.ingame.BookScreen
import net.minecraft.text.Text

class SpellBookScreen(val spells: List<Spell>) : BookScreen(createContent(spells)) {

	override fun mouseClicked(click: Click, doubled: Boolean): Boolean {
		if (super.mouseClicked(click, doubled)) return true
		if (click.x > (width - MAX_TEXT_WIDTH) / 2
			&& click.x < (width + MAX_TEXT_WIDTH) / 2
			&& click.y > (height - MAX_TEXT_HEIGHT) / 2
			&& click.y < (height + MAX_TEXT_HEIGHT) / 2
		) {
			@Suppress("CAST_NEVER_SUCCEEDS")
			val spell = spells[(this as BookScreenAccessor).pageIndex]
			(client as DNDClient).`mTT$setSpellSelector`(SpellSelector.fromSpell(spell, client.world!!))
			client.setScreen(null)
			return true
		}
		return false
	}

	companion object {
		fun createContent(spells: Collection<Spell>): Contents {
			val text = spells.map { spell ->
				Text.translatable(spell.getTranslationKey()).append("\n")
					.append(Text.translatable(spell.getDescriptionKey()))
			}
			return Contents(text)
		}
	}
}