package com.pineypiney.mtt.util

import net.minecraft.text.Text

object Localisation {

	fun <T> translateList(list: List<T>, or: Boolean, translationKey: (T) -> String): Text{
		return when(list.size){
			0 -> Text.empty()
			1 -> Text.translatable(translationKey(list.first()))
			else -> {
				val typeStr = if(or) "or" else "and"
				val genericSeparator = Text.translatable("mtt.formatting.separator")
				val lastSeparator = Text.translatableWithFallback("mtt.formatting.separator.last_$typeStr", genericSeparator.string)
				val text = Text.translatable(translationKey(list.first()))
				for(i in 1..list.size - 2) text.append(genericSeparator).append(Text.translatable(translationKey(list[i])))
				text.append(lastSeparator).append(Text.translatable(translationKey(list.last())))
			}
		}
	}
	fun translateList(list: List<String>, or: Boolean): Text{
		return when(list.size){
			0 -> Text.empty()
			1 -> Text.translatable(list.first())
			else -> {
				val typeStr = if(or) "or" else "and"
				val genericSeparator = Text.translatable("mtt.formatting.separator")
				val lastSeparator = Text.translatableWithFallback("mtt.formatting.separator.last_$typeStr", genericSeparator.string)
				val text = Text.translatable(list.first())
				for(i in 1..list.size - 2) text.append(genericSeparator).append(Text.translatable(list[i]))
				text.append(lastSeparator).append(Text.translatable(list.last()))
			}
		}
	}
}