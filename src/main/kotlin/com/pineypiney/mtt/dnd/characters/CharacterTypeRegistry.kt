package com.pineypiney.mtt.dnd.characters

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.network.ServerCharacter
import com.pineypiney.mtt.dnd.server.ServerDNDEngine
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import io.netty.buffer.ByteBuf
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.util.Identifier
import kotlin.reflect.KClass

object CharacterTypeRegistry {

	val entries: MutableMap<String, Entry<*>> = mutableMapOf()

	fun <C : CharacterDetails> register(
		id: Identifier,
		klass: KClass<C>,
		eCodec: PacketCodec<ByteBuf, C>
	) {
		val key = getKey(id)
		val entry = Entry(id, klass, eCodec)
		entries[key] = entry
	}

	private fun getKey(id: Identifier): String = if (id.namespace == MTT.MOD_ID) id.path else id.toString()

	@Suppress("UNCHECKED_CAST")
	inline fun <reified C : CharacterDetails> save(character: Character, details: C, buf: RegistryByteBuf) {
		val entry = entries.values.firstOrNull { details::class == it.klass } as? Entry<C>
		// A
		if (entry == null) MTTPacketCodecs.str.encode(buf, "null")
		else save(entry, character, details, buf)
	}

	fun <C : CharacterDetails> save(entry: Entry<C>, character: Character, details: C, buf: RegistryByteBuf) {
		MTTPacketCodecs.str.encode(buf, getKey(entry.id))
		entry.eCodec.encode(buf, details)
		MTTPacketCodecs.UUID_CODEC.encode(buf, character.uuid)

		val nbt = NbtCompound()
		character.writeNbt(nbt, buf.registryManager)
		buf.writeNbt(nbt)
	}

	fun load(buf: RegistryByteBuf, engine: ServerDNDEngine): ServerCharacter? {
		val type = MTTPacketCodecs.str.decode(buf)
		val entry = entries[type] ?: return null
		return load(entry, buf, engine)
	}

	private fun <C : CharacterDetails> load(entry: Entry<C>, buf: RegistryByteBuf, engine: ServerDNDEngine): ServerCharacter {
		val character = ServerCharacter(entry.eCodec.decode(buf), MTTPacketCodecs.UUID_CODEC.decode(buf), engine)

		val nbt = buf.readNbt() ?: return character
		character.readNbt(nbt, buf.registryManager)
		return character
	}

	class Entry<C : CharacterDetails>(
		val id: Identifier,
		val klass: KClass<C>,
		val eCodec: PacketCodec<ByteBuf, C>
	)

	init {
		register(
			Identifier.of(MTT.MOD_ID, "sheet"),
			CharacterSheet::class,
			MTTPacketCodecs.CHARACTER_SHEET
		)

		register(
			Identifier.of(MTT.MOD_ID, "simple"),
			SimpleCharacterDetails::class,
			SimpleCharacterDetails.PACKET_CODEC
		)
	}
}