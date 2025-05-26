package com.pineypiney.mtt.dnd.traits

import io.netty.buffer.ByteBuf
import net.minecraft.text.Text

abstract class TraitComponent<T, C: TraitComponent<T, C>>(val root: String = "feature") {
	open val declarationKey get() = "mtt.$root.${getID()}.declaration"
	abstract fun getCodec(): TraitCodec<T, C>
	open fun getID(): String = getCodec().ID

	open fun getLabel(): Text = Text.translatable("mtt.$root.${getID()}")
	open fun getDescription(): Text = Text.translatable("mtt.$root.${getID()}.description")
	open fun getTranslationKey(value: T): String = "mtt.${getID()}.$value"
	abstract fun getLines(): List<Any>

	@Suppress("UNCHECKED_CAST")
	fun encode(buf: ByteBuf){
		getCodec().encode(buf, this as C)
	}
}