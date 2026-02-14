package com.pineypiney.mtt.dnd.characters

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.DNDEngine
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import io.netty.buffer.ByteBuf
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.util.Identifier
import java.util.*
import kotlin.reflect.KClass

object CharacterTypeRegistry {

	val entries: MutableMap<String, Entry<*, *>> = mutableMapOf()

	fun <C : Character, E> register(
		id: Identifier,
		klass: KClass<C>,
		eCodec: PacketCodec<ByteBuf, E>,
		to: (C) -> E,
		from: (E, UUID, DNDEngine) -> C
	) {
		val key = getKey(id)
		val entry = Entry(id, klass, eCodec, to, from)
		entries[key] = entry
	}

	private fun getKey(id: Identifier): String = if (id.namespace == MTT.MOD_ID) id.path else id.toString()

	@Suppress("UNCHECKED_CAST")
	inline fun <reified C : Character> save(character: C, buf: RegistryByteBuf) {
		val entry = entries.values.firstOrNull { character::class == it.klass } as? Entry<C, *>
		// A
		if (entry == null) MTTPacketCodecs.str.encode(buf, "null")
		else save(entry, character, buf)
	}

	fun <C : Character, E> save(entry: Entry<C, E>, character: C, buf: RegistryByteBuf) {
		MTTPacketCodecs.str.encode(buf, getKey(entry.id))
		entry.eCodec.encode(buf, entry.to(character))
		MTTPacketCodecs.UUID_CODEC.encode(buf, character.uuid)

		val nbt = NbtCompound()
		character.writeNbt(nbt, buf.registryManager)
		buf.writeNbt(nbt)
	}

	fun load(buf: RegistryByteBuf, engine: DNDEngine): Character? {
		val type = MTTPacketCodecs.str.decode(buf)
		val entry = entries[type] ?: return null
		return load(entry, buf, engine)
	}

	private fun <C : Character, E> load(entry: Entry<C, E>, buf: RegistryByteBuf, engine: DNDEngine): C {
		val character = entry.from(entry.eCodec.decode(buf), MTTPacketCodecs.UUID_CODEC.decode(buf), engine)

		val nbt = buf.readNbt() ?: return character
		character.readNbt(nbt, buf.registryManager)
		return character
	}

	class Entry<C : Character, E>(
		val id: Identifier,
		val klass: KClass<C>,
		val eCodec: PacketCodec<ByteBuf, E>,
		val to: (C) -> E,
		val from: (E, UUID, DNDEngine) -> C
	)

	init {
		register(
			Identifier.of(MTT.MOD_ID, "sheet"),
			SheetCharacter::class,
			MTTPacketCodecs.CHARACTER_SHEET,
			SheetCharacter::sheet,
			::SheetCharacter
		)

		register(
			Identifier.of(MTT.MOD_ID, "simple"),
			SimpleCharacter::class,
			SimpleCharacter.Params.PACKET_CODEC,
			SimpleCharacter::details,
			::SimpleCharacter
		)
	}
}