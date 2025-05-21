package com.pineypiney.mtt.dnd.traits

import io.netty.buffer.ByteBuf

abstract class TraitComponent<T: TraitComponent<T>>() {
	abstract fun getCodec(): TraitCodec<T>

	@Suppress("UNCHECKED_CAST")
	fun encode(buf: ByteBuf){
		getCodec().encode(buf, this as T)
	}
}