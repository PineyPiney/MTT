package com.pineypiney.mtt.dnd.traits

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.text.Text

/**
 * Traits that different DND Races can have.
 * They are given classes to make them as dynamic as possible,
 * so that they can be defined in JSON files
 */

abstract class Trait<T: Trait<T>>(val root: String = "feature") {
	abstract fun getCodec(): TraitCodec<T>
	abstract fun getParts(): Set<TraitPart>

	open fun getID() = getCodec().ID
	open fun getLabelKey(): String = "mtt.$root.${getID()}"
	open fun getDeclarationKey(): String = "mtt.$root.${getID()}.declaration"
	open fun getDescriptionKey(): String = "mtt.$root.${getID()}.description"
	open fun getTranslationKey(value: Any): String = "mtt.${getID()}.$value"

	open fun getLabel(): Text = Text.translatable(getLabelKey())
	open fun getTranslation(value: Any): Text = Text.translatable(getTranslationKey(value))

	@Suppress("UNCHECKED_CAST")
	fun encode(buf: ByteBuf){
		val codec = getCodec()
		PacketCodecs.STRING.encode(buf, codec.ID)
		codec.encode(buf, this as T)
	}
}