package com.pineypiney.mtt.dnd.traits

import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import com.pineypiney.mtt.network.codec.MTTPacketCodecs.bytInt
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec

open class SourceMap<E> : LinkedHashMap<Source, MutableSet<E>>() {

	fun add(key: Source, value: E) {
		val current = get(key)
		if (current != null) current.add(value)
		else put(key, mutableSetOf(value))
	}

	fun addAll(key: Source, values: Collection<E>) {
		val current = get(key)
		if (current != null) current.addAll(values)
		else put(key, values.toMutableSet())
	}

	fun getAll() = flatMap(Map.Entry<Source, MutableSet<E>>::value)

	operator fun set(key: Source, value: E) = add(key, value)
	operator fun set(key: Source, values: Collection<E>) = addAll(key, values)

	fun forEachEntry(action: (E) -> Unit) {
		forEach { (_, values) -> values.forEach(action) }
	}

	fun <R> mapEntries(transform: (E) -> R): List<R> {
		return flatMap { (_, values) -> values.map(transform) }
	}

	fun <B : ByteBuf> encode(buf: B, valueCodec: PacketCodec<B, MutableSet<E>>) {
		bytInt.encode(buf, size)
		for ((key, value) in this) {
			MTTPacketCodecs.SOURCE.encode(buf, key)
			valueCodec.encode(buf, value)
		}
	}

	fun <B : ByteBuf> decode(buf: B, valueCodec: PacketCodec<B, MutableSet<E>>) {
		clear()
		repeat(bytInt.decode(buf)) {
			val key = MTTPacketCodecs.SOURCE.decode(buf)
			val level = valueCodec.decode(buf)
			this[key] = level
		}
	}
}