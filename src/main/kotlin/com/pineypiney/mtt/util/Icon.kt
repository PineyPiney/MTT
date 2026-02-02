package com.pineypiney.mtt.util

import com.pineypiney.mtt.MTT
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class Icon(val id: Char) {

	fun toText(): Text = Text.literal(id.toString()).fillStyle(style)

	companion object {
		val style = Style.EMPTY.withFont(Identifier.of(MTT.MOD_ID, "icons"))
	}
}