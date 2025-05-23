package com.pineypiney.mtt.dnd.traits

import io.netty.buffer.ByteBuf
import net.minecraft.text.Text

abstract class TraitComponent<T: TraitComponent<T>>() {
	abstract fun getCodec(): TraitCodec<T>
	fun getID(): String = getCodec().ID

	// This is formatting for how the component is displayed
	// telling the player what the value of the component is
	// Defaults to "Component: Value"
	open fun getDef(value: T): Text = Text.translatable("mtt.trait.${getID()}: $value")

	@Suppress("UNCHECKED_CAST")
	fun encode(buf: ByteBuf){
		getCodec().encode(buf, this as T)
	}
}