package com.pineypiney.mtt.util

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.DNDEngine
import com.pineypiney.mtt.dnd.server.ServerDNDEngine
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.server.MinecraftServer
import net.minecraft.world.World
import java.util.*
import kotlin.jvm.optionals.getOrNull

fun <T: Any> T?.optional(): Optional<T> {
	return if(this == null) Optional.empty()
	else Optional.of(this)
}

fun <T: Any> Optional<T>.nullable(): T? {
	return this.getOrNull()
}

fun <B : ByteBuf, T : Any> PacketCodec<B, T>.nullify(): PacketCodec<B, T?> {
	return object : PacketCodec<B, T?> {
		override fun decode(buf: B): T? {
			val exists = buf.readBoolean()
			return if (exists) this@nullify.decode(buf) else null
		}

		override fun encode(buf: B, value: T?) {
			buf.writeBoolean(value != null)
			if (value != null) this@nullify.encode(buf, value)
		}
	}
}

fun UUID.toInts(): List<Int> {
	return listOf(
		(mostSignificantBits shr 32).toInt(), mostSignificantBits.toInt(),
		(leastSignificantBits shr 32).toInt(), leastSignificantBits.toInt()
	)
}

fun World.getEngine(): DNDEngine = (this as DNDEngineHolder<*>).`mtt$getDNDEngine`()
fun MinecraftServer.getEngine(): ServerDNDEngine = (this as DNDEngineHolder<*>).`mtt$getDNDEngine`() as ServerDNDEngine

fun <E> Collection<E>.findOrError(message: String, pred: (E) -> Boolean): E {
	return try {
		first(pred)
	} catch (e: NoSuchElementException) {
		MTT.logger.error(message)
		throw e
	}
}