package com.pineypiney.mtt.serialisation

import com.pineypiney.mtt.dnd.server.ServerDNDEngine
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.minecraft.network.RegistryByteBuf
import java.io.File

class EnginePropertySerialiser<B : ByteBuf>(val fileName: String, val createBuffer: ServerDNDEngine.() -> B, val save: ServerDNDEngine.(buf: B) -> Unit, val load: ServerDNDEngine.(buf: B) -> Unit) {

	fun save(engine: ServerDNDEngine, mttDir: File) {
		val file = File(mttDir, fileName)
		if (!file.exists()) file.createNewFile()

		val buf = createBuffer(engine)

		save(engine, buf)
		file.writeBytes(buf.nioBuffer().array())
	}

	fun load(engine: ServerDNDEngine, mttDir: File) {
		val file = File(mttDir, fileName)
		if (!file.exists()) return

		val buf = createBuffer(engine)
		buf.writeBytes(file.readBytes())
		if (buf.readableBytes() == 0) return

		load(engine, buf)
	}

	companion object {
		val CHARACTER_SERIALISER =
			EnginePropertySerialiser("characters.bin", { RegistryByteBuf(Unpooled.buffer(), server.registryManager) }, ServerDNDEngine::encodeCharacters, ServerDNDEngine::decodeCharacters)
		val COMBATS_SERIALISER = EnginePropertySerialiser("combats.bin", { Unpooled.buffer() }, ServerDNDEngine::encodeCombats, ServerDNDEngine::decodeCombats)
	}
}