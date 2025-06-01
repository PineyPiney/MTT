package com.pineypiney.mtt.network.codec

import com.pineypiney.mtt.dnd.Background
import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.characters.SheetCharacter
import com.pineypiney.mtt.dnd.classes.DNDClass
import com.pineypiney.mtt.dnd.species.Species
import com.pineypiney.mtt.dnd.traits.Source
import com.pineypiney.mtt.dnd.traits.features.Feature
import com.pineypiney.mtt.dnd.traits.features.Features
import com.pineypiney.mtt.dnd.traits.proficiencies.Proficiency
import io.netty.buffer.ByteBuf
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import java.util.*

class MTTPacketCodecs {

	companion object {

		val str get() = PacketCodecs.STRING
		val int get() = PacketCodecs.INTEGER

		fun <B: ByteBuf> INT() = object : PacketCodec<B, Int>{
			override fun decode(buf: B) = buf.readInt()
			override fun encode(buf: B, value: Int) { buf.writeInt(value) }
		}

		fun <T> decodeList(buf: ByteBuf, codec: PacketCodec<ByteBuf, T>): List<T>{
			val length = PacketCodecs.INTEGER.decode(buf)
			return List(length){ codec.decode(buf) }
		}
		fun <T, C> decodeList(buf: ByteBuf, codec: PacketCodec<ByteBuf, C>, getter: (C) -> T): List<T>{
			val length = int.decode(buf)
			return List(length){ getter(codec.decode(buf)) }
		}
		fun <B: ByteBuf, T> decodeCollection(buf: B, codec: PacketCodec<B, T>, dst: MutableCollection<T>): MutableCollection<T>{
			val length = int.decode(buf)
			for(i in 1..length) dst.add(codec.decode(buf))
			return dst
		}
		fun <B: ByteBuf, T> encodeCollection(buf: B, collection: Collection<T>, codec: PacketCodec<B, T>){
			int.encode(buf, collection.size)
			collection.forEach { codec.encode(buf, it) }
		}
		fun <T, C> encodeCollection(buf: ByteBuf, collection: Collection<T>, codec: PacketCodec<ByteBuf, C>, getter: (T) -> C){
			int.encode(buf, collection.size)
			collection.forEach { codec.encode(buf, getter(it)) }
		}

		fun <K, V> decodeMap(buf: ByteBuf, keyCodec: PacketCodec<ByteBuf, K>, valueCodec: PacketCodec<ByteBuf, V>, dst: MutableMap<K, V> = mutableMapOf()): MutableMap<K, V>{
			dst.clear()
			for(i in 1..int.decode(buf)){
				val key = keyCodec.decode(buf)
				val level = valueCodec.decode(buf)
				dst[key] = level
			}
			return dst
		}

		fun <K, V> encodeMap(buf: ByteBuf, map: Map<K, V>, keyCodec: PacketCodec<ByteBuf, K>, valueCodec: PacketCodec<ByteBuf, V>){
			int.encode(buf, map.size)
			for((key, value) in map){
				keyCodec.encode(buf, key)
				valueCodec.encode(buf, value)
			}
		}

		fun <B: ByteBuf, T, TC> from(codec: PacketCodec<B, TC>, to: (T) -> TC, from: (TC) -> T): PacketCodec<B, T> = object :
			PacketCodec<B, T> {
			override fun decode(buf: B): T = from(codec.decode(buf))
			override fun encode(buf: B, value: T) = codec.encode(buf, to(value))
		}

		fun <T, C: List<T>> collection(codec: PacketCodec<ByteBuf, T>, func: (Int, (Int) -> T) -> C) = object :
			PacketCodec<ByteBuf, C> {
			override fun decode(buf: ByteBuf): C {
				val size = int.decode(buf)
				return func(size){ codec.decode(buf) }
			}

			override fun encode(buf: ByteBuf, value: C) {
				int.encode(buf, value.size)
				value.forEach { codec.encode(buf, it) }
			}

		}

		fun <T, C: MutableCollection<T>> collection(codec: PacketCodec<ByteBuf, T>, creator: () -> C) = object :
			PacketCodec<ByteBuf, C> {
			override fun decode(buf: ByteBuf): C {
				val coll = creator()
				val size = int.decode(buf)
				for(i in 1..size) coll.add(codec.decode(buf))
				return coll
			}

			override fun encode(buf: ByteBuf, value: C) {
				int.encode(buf, value.size)
				value.forEach { codec.encode(buf, it) }
			}
		}

		fun <B: ByteBuf, K, V> map(keyCodec: PacketCodec<B, K>, valueCodec: PacketCodec<B, V>) = object : PacketCodec<B, Map<K, V>>{
			override fun decode(buf: B): Map<K, V> {
				val map = mutableMapOf<K, V>()
				for(i in 1..buf.readInt()){
					map[keyCodec.decode(buf)] = valueCodec.decode(buf)
				}
				return map
			}

			override fun encode(buf: B, value: Map<K, V>) {
				buf.writeInt(value.size)
				for((k, v) in value){
					keyCodec.encode(buf, k)
					valueCodec.encode(buf, v)
				}

			}

		}

		val UUID_CODEC = PacketCodec.tuple(
			PacketCodecs.LONG, UUID::getMostSignificantBits,
			PacketCodecs.LONG, UUID::getLeastSignificantBits,
			::UUID
		)

		val SOURCE_CODEC = object : PacketCodec<ByteBuf, Source> {
			override fun decode(buf: ByteBuf): Source {
				val type = str.decode(buf)
				val id = str.decode(buf)
				return when(type){
					"species" -> Source.SpeciesSource(Species.Companion.findById(id))
					"class" -> Source.ClassSource(DNDClass.Companion.classes.first { it.id == id })
					"background" -> Source.BackgroundSource(Background.Companion.findById(id))
					"feature" -> Source.FeatureSource(id)
					"dm" -> Source.DMOverrideSource(UUID.fromString(id))
					else -> throw Exception("Couldn't decode Source of type $type with id $id")
				}
			}

			override fun encode(buf: ByteBuf, value: Source) {
				val (type, id) = when(value){
					is Source.SpeciesSource -> "species" to value.species.id
					is Source.ClassSource -> "class" to value.clazz.id
					is Source.BackgroundSource -> "background" to value.background.id
					is Source.FeatureSource -> "feature" to value.id
					is Source.DMOverrideSource -> "dm" to value.dm.toString()
				}
				str.encode(buf, type)
				str.encode(buf, id)
			}

		}

		val FEATURE_CODEC = object : PacketCodec<ByteBuf, Feature> {
			override fun decode(buf: ByteBuf): Feature {
				val id = str.decode(buf)
				val simple = Features.Companion.set.firstOrNull { it.id == id }
				if(simple != null) return simple

				val codec = Features.Companion.classCodecs[id]
				if(codec == null) throw Exception("Cannot parse feature: No feature with id $id has been registered")
				return codec.decode(buf) as Feature
			}

			override fun encode(buf: ByteBuf, value: Feature) {
				value.encode(buf)
			}
		}

		val CHARACTER_SHEET_CODEC = object : PacketCodec<ByteBuf, CharacterSheet> {

			override fun decode(buf: ByteBuf): CharacterSheet {
				val sheet = CharacterSheet()
				sheet.name = str.decode(buf)
				sheet.species = Species.Companion.findById(str.decode(buf))
				sheet.background = Background.findById(str.decode(buf))
				sheet.level = int.decode(buf)
				sheet.decodeProperties(buf)
				sheet.model = str.decode(buf)
				sheet.armourClass = int.decode(buf)
				sheet.darkVision = int.decode(buf)

				sheet.abilities.decode(buf)

				decodeMap(buf, DNDClass.CODEC, int, sheet.classes)
//				val numClasses = int.decode(buf)
//				sheet.classes.clear()
//				for(i in 1..numClasses){
//					val clazz = DNDClass.classes.first { it.id == str.decode(buf) }
//					val level = int.decode(buf)
//					sheet.classes[clazz] = level
//				}

				decodeMap(buf, SOURCE_CODEC, collection(str, ::mutableSetOf), sheet.advantages)
//				val numAdvantages = int.decode(buf)
//				sheet.advantages.clear()
//				for(i in 1..numAdvantages){
//					val src = SOURCE_CODEC.decode(buf)
//					val set = decodeList(buf, str).toMutableSet()
//					sheet.advantages[src] = set
//				}

				decodeMap(buf, SOURCE_CODEC, collection(str, ::mutableSetOf), sheet.resistances)
				decodeMap(buf, SOURCE_CODEC, collection(Proficiency.Companion.CODEC, ::mutableSetOf), sheet.proficiencies)
				decodeMap(buf, SOURCE_CODEC, collection(FEATURE_CODEC, ::mutableSetOf), sheet.features)

				return sheet
			}

			override fun encode(buf: ByteBuf, value: CharacterSheet) {
				str.encode(buf, value.name)
				str.encode(buf, value.species.id)
				str.encode(buf, value.background.id)
				int.encode(buf, value.level)
				value.encodeProperties(buf)
				str.encode(buf, value.model)
				int.encode(buf, value.armourClass)
				int.encode(buf, value.darkVision)

				value.abilities.encode(buf)
				encodeMap(buf, value.classes, DNDClass.Companion.CODEC, int)
//				int.encode(buf, value.classes.size)
//				for((clazz, level) in value.classes){
//					str.encode(buf, clazz.id)
//					int.encode(buf, level)
//				}

				encodeMap(buf, value.advantages, SOURCE_CODEC, collection(str, ::mutableSetOf))
//				int.encode(buf, value.advantages.size)
//				for((src, list) in value.advantages){
//					SOURCE_CODEC.encode(buf, src)
//					encodeCollection(buf, list, str)
//				}

				encodeMap(buf, value.resistances, SOURCE_CODEC, collection(str, ::mutableSetOf))
				encodeMap(buf, value.proficiencies, SOURCE_CODEC, collection(Proficiency.CODEC, ::mutableSetOf))
				encodeMap(buf, value.features, SOURCE_CODEC, collection(FEATURE_CODEC, ::mutableSetOf))
			}
		}

		val CHARACTER_CODEC = object : PacketCodec<RegistryByteBuf, Character>{
			override fun decode(buf: RegistryByteBuf): Character {
				val type = str.decode(buf)
				return when(type){
					"sheet" -> SheetCharacter.load(buf)
					else -> throw Exception("Cannot parse character: no character type $type")
				}
			}

			override fun encode(buf: RegistryByteBuf, value: Character) {
				value.save(buf)
			}
		}
	}
}