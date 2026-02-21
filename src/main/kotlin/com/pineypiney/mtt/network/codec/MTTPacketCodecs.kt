package com.pineypiney.mtt.network.codec

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.Background
import com.pineypiney.mtt.dnd.DamageType
import com.pineypiney.mtt.dnd.Duration
import com.pineypiney.mtt.dnd.characters.CharacterModel
import com.pineypiney.mtt.dnd.characters.CharacterSheet
import com.pineypiney.mtt.dnd.classes.DNDClass
import com.pineypiney.mtt.dnd.conditions.Condition
import com.pineypiney.mtt.dnd.conditions.Conditions
import com.pineypiney.mtt.dnd.race.NamedTrait
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.dnd.race.Subrace
import com.pineypiney.mtt.dnd.spells.Spell
import com.pineypiney.mtt.dnd.traits.*
import com.pineypiney.mtt.dnd.traits.features.Feature
import com.pineypiney.mtt.dnd.traits.features.Features
import com.pineypiney.mtt.dnd.traits.proficiencies.Proficiency
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import java.util.*
import kotlin.math.min
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter

object MTTPacketCodecs {

	val byt: PacketCodec<ByteBuf, Byte> get() = PacketCodecs.BYTE
	val int: PacketCodec<ByteBuf, Int> get() = PacketCodecs.INTEGER
	val flt: PacketCodec<ByteBuf, Float> get() = PacketCodecs.FLOAT
	val str: PacketCodec<ByteBuf, String> get() = PacketCodecs.STRING

	val bytInt: PacketCodec<ByteBuf, Int> =
		PacketCodecs.BYTE.xmap({ byte -> byte.toUByte().toInt() }) { int -> int.toUByte().toByte() }
	val shtInt: PacketCodec<ByteBuf, Int> =
		PacketCodecs.SHORT.xmap({ short -> short.toUShort().toInt() }) { int -> int.toUShort().toShort() }

	fun <B: ByteBuf> INT() = object : PacketCodec<B, Int>{
		override fun decode(buf: B) = buf.readInt()
		override fun encode(buf: B, value: Int) { buf.writeInt(value) }
	}

	fun <E> Set(size: Int, init: (Int) -> E): Set<E> {
		val set = mutableSetOf<E>()
		repeat(size) { set.add(init(it)) }
		return set
	}

	fun <T> decodeList(buf: ByteBuf, codec: PacketCodec<ByteBuf, T>): List<T>{
		val length = int.decode(buf)
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
		repeat(bytInt.decode(buf)) {
			val key = keyCodec.decode(buf)
			val level = valueCodec.decode(buf)
			dst[key] = level
		}
		return dst
	}

	fun <K, V> encodeMap(buf: ByteBuf, map: Map<K, V>, keyCodec: PacketCodec<ByteBuf, K>, valueCodec: PacketCodec<ByteBuf, V>){
		bytInt.encode(buf, map.size)
		for((key, value) in map){
			keyCodec.encode(buf, key)
			valueCodec.encode(buf, value)
		}
	}

	fun <T, C : Collection<T>> smallCollection(codec: PacketCodec<ByteBuf, T>, func: (Int, (Int) -> T) -> C) = object :
		PacketCodec<ByteBuf, C> {
		override fun decode(buf: ByteBuf): C {
			val size = byt.decode(buf).toUByte().toInt()
			return func(size) { codec.decode(buf) }
		}

		override fun encode(buf: ByteBuf, value: C) {
			val size = min(value.size, 255)
			byt.encode(buf, size.toUByte().toByte())

			if (value.size > 255) {
				MTT.logger.warn("Tried to make small collection packet from a large collection, writing the first 255 entries")
				for ((i, it) in value.withIndex()) {
					codec.encode(buf, it)
					if (i >= 254) break
				}
			} else value.forEach { codec.encode(buf, it) }
		}

	}

	fun <T, C : MutableCollection<T>> smallCollection(codec: PacketCodec<ByteBuf, T>, creator: () -> C) = object :
		PacketCodec<ByteBuf, C> {
		override fun decode(buf: ByteBuf): C {
			val size = byt.decode(buf).toUByte().toInt()
			val coll = creator()
			repeat(size) { coll.add(codec.decode(buf)) }
			return coll
		}

		override fun encode(buf: ByteBuf, value: C) {
			val size = min(value.size, 255)
			byt.encode(buf, size.toUByte().toByte())

			if (value.size > 255) {
				MTT.logger.warn("Tried to make small collection packet from a large collection, writing the first 255 entries")
				for ((i, it) in value.withIndex()) {
					codec.encode(buf, it)
					if (i >= 255) break
				}
			} else value.forEach { codec.encode(buf, it) }
		}
	}

	fun <T, C : Collection<T>> collection(codec: PacketCodec<ByteBuf, T>, func: (Int, (Int) -> T) -> C) = object :
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
			repeat(size) { coll.add(codec.decode(buf)) }
			return coll
		}

		override fun encode(buf: ByteBuf, value: C) {
			int.encode(buf, value.size)
			value.forEach { codec.encode(buf, it) }
		}
	}

	fun <B : ByteBuf, K, V> smallMap(keyCodec: PacketCodec<B, K>, valueCodec: PacketCodec<B, V>) =
		object : PacketCodec<B, Map<K, V>> {
			override fun decode(buf: B): Map<K, V> {
				val map = mutableMapOf<K, V>()
				repeat(bytInt.decode(buf)) {
					map[keyCodec.decode(buf)] = valueCodec.decode(buf)
				}
				return map
			}

			override fun encode(buf: B, value: Map<K, V>) {
				bytInt.encode(buf, value.size)
				val size = min(value.size, 255)
				if (size > 255) {
					MTT.logger.warn("Tried to make small map packet from a large collection, writing the first 255 entries")
					var i = 0
					for ((k, v) in value) {
						keyCodec.encode(buf, k)
						valueCodec.encode(buf, v)
						if (i++ >= 254) break
					}
				} else for ((k, v) in value) {
					keyCodec.encode(buf, k)
					valueCodec.encode(buf, v)
				}

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

	val ID = str.xmap(MTT::identifier, MTT::identifierString)

	val UUID_CODEC = PacketCodec.tuple(
		PacketCodecs.LONG, UUID::getMostSignificantBits,
		PacketCodecs.LONG, UUID::getLeastSignificantBits,
		::UUID
	)

	val DURATION = object : PacketCodec<ByteBuf, Duration> {
		override fun decode(buf: ByteBuf): Duration {
			@Suppress("UNCHECKED_CAST")
			val id = bytInt.decode(buf)
			val codec = when (id) {
				0 -> Duration.Time.PACKET_CODEC
				1 -> Duration.Turns.PACKET_CODEC
				2 -> Duration.ShortRest.PACKET_CODEC
				3 -> Duration.LongRest.PACKET_CODEC
				else -> throw Error("Unknown DND Duration ID $id")
			}
			return codec.decode(buf)
		}

		override fun encode(buf: ByteBuf, value: Duration) {
			encodeDuration(buf, value)
		}

		fun <D : Duration> encodeDuration(buf: ByteBuf, value: D) {
			bytInt.encode(buf, value.getID())
			@Suppress("UNCHECKED_CAST")
			val codec = try {
				val companionClass = value::class.companionObject
				val property = companionClass?.memberProperties?.first { it.name == "PACKET_CODEC" }
				val codec = property?.javaGetter?.invoke(value::class.companionObjectInstance!!)
				codec as PacketCodec<ByteBuf, D>
			} catch (e: Exception) {
				MTT.logger.error("Could not find static property PACKET_CODEC in Duration class ${value::class}")
				throw e
			}
			codec.encode(buf, value)
		}
	}

	val DAMAGE_TYPE = str.xmap(DamageType::find, DamageType::id)

	val ABILITY = bytInt.xmap({ Ability.entries[it] }, Ability::ordinal)

	val CLASS = PacketCodecs.STRING.xmap(DNDClass::findById, DNDClass::id)

	val MODEL = PacketCodec.tuple(
		str, CharacterModel::id,
		flt, CharacterModel::width,
		flt, CharacterModel::height,
		flt, CharacterModel::eyeY,
		::CharacterModel
	)

	val SPELL = str.xmap(Spell::findById, Spell::id)
	val SPELLS = smallCollection(SPELL, ::Set)

	val SOURCE = object : PacketCodec<ByteBuf, Source> {
		override fun decode(buf: ByteBuf): Source {
			val type = str.decode(buf)
			when (type) {
				"spell" -> return Source.SpellSource
			}
			val id = str.decode(buf)
			return when(type){
				"race" -> Source.RaceSource(Race.findById(id))
				"subrace" -> {
					val race = Race.findById(id)
					val subraceID = str.decode(buf)
					Source.SubraceSource(race, race.getSubrace(subraceID)!!)
				}
				"class" -> Source.ClassSource(DNDClass.findById(id))
				"background" -> Source.BackgroundSource(Background.findById(id))
				"feature" -> Source.FeatureSource(id)
				"dm" -> Source.DMOverrideSource(UUID.fromString(id))
				else -> throw Exception("Couldn't decode Source of type $type with id $id")
			}
		}

		override fun encode(buf: ByteBuf, value: Source) {
			val parts = when (value) {
				is Source.RaceSource -> setOf("race", value.race.id)
				is Source.SubraceSource -> setOf("subrace", value.race.id, value.subrace.name)
				is Source.ClassSource -> setOf("class", value.clazz.id)
				is Source.BackgroundSource -> setOf("background", value.background.id)
				is Source.FeatureSource -> setOf("feature", value.id)
				is Source.SpellSource -> setOf("spell")
				is Source.DMOverrideSource -> setOf("dm", value.dm.toString())
			}
			for (part in parts) str.encode(buf, part)
		}

	}

	val SOURCE_LIST = smallCollection(
		PacketCodec.tuple(bytInt, Pair<Int, Source>::first, SOURCE, Pair<Int, Source>::second, ::Pair),
		::List
	)

	val FEATURE = object : PacketCodec<ByteBuf, Feature> {
		override fun decode(buf: ByteBuf): Feature {
			val id = str.decode(buf)
			val simple = Features.set.firstOrNull { it.id == id }
			if(simple != null) return simple

			val codec = Features.classCodecs[id] ?: throw Exception("Cannot parse feature: No feature with id $id has been registered")
			return codec.decode(buf) as Feature
		}

		override fun encode(buf: ByteBuf, value: Feature) {
			value.encode(buf)
		}
	}

	val TRAIT = object : PacketCodec<ByteBuf, Trait<*>> {
		override fun decode(buf: ByteBuf): Trait<*>? {
			val componentID = str.decode(buf)
			return Traits.getCodec(componentID)?.decode(buf)
		}

		override fun encode(buf: ByteBuf, value: Trait<*>) {
			value.encode(buf)
		}
	}

	val NAMED_TRAIT = PacketCodec.tuple<ByteBuf, NamedTrait<*>, String, Set<Trait<*>>>(
		str, NamedTrait<*>::name,
		smallCollection(TRAIT, ::Set), NamedTrait<*>::traits,
		::NamedTrait
	)

	val SUBRACE = PacketCodec.tuple(
		str, Subrace::name,
		smallCollection(TRAIT, ::List), Subrace::traits,
		smallCollection(NAMED_TRAIT, ::List), Subrace::namedTraits,
		::Subrace
	)

	val RACE = PacketCodec.tuple(
		str, Race::id,
		str.xmap(CreatureType::valueOf, CreatureType::name), Race::type,
		int, Race::speed,
		TraitCodec.SIZE_CODEC, Race::size,
		smallCollection(MODEL, ::Set), Race::models,
		smallCollection(TRAIT, ::List), Race::traits,
		smallCollection(NAMED_TRAIT, ::List), Race::namedTraits,
		smallCollection(SUBRACE, ::Set), Race::subraces,
		::Race
	)

	val CONDITION = object : PacketCodec<ByteBuf, Condition.ConditionState<*>> {
		override fun decode(buf: ByteBuf): Condition.ConditionState<*> {
			val condition = Conditions.findById(ID.decode(buf))
			return condition.createState(buf)
		}

		override fun encode(buf: ByteBuf, value: Condition.ConditionState<*>) {
			ID.encode(buf, value.condition.id)
			value.encode(buf)
		}
	}

	val CHARACTER_SHEET = object : PacketCodec<ByteBuf, CharacterSheet> {

		override fun decode(buf: ByteBuf): CharacterSheet {
			val sheet = CharacterSheet()
			sheet.name = str.decode(buf)
			sheet.race = Race.findById(str.decode(buf))
			sheet.subrace = sheet.race.getSubrace(str.decode(buf))
			sheet.background = Background.findById(str.decode(buf))

			sheet.level = bytInt.decode(buf)
			sheet.armourClass = bytInt.decode(buf)
			sheet.maxHealth = shtInt.decode(buf)
			sheet.health = shtInt.decode(buf)
			sheet.darkVision = shtInt.decode(buf)

			sheet.abilities.decode(buf)

			sheet.decodeProperties(buf)
			decodeMap(buf, CLASS, bytInt, sheet.classes)
			sheet.advantages.decode(buf, smallCollection(str, ::mutableSetOf))
			sheet.resistances.decode(buf, smallCollection(str, ::mutableSetOf))
			sheet.proficiencies.decode(buf, smallCollection(Proficiency.CODEC, ::mutableSetOf))
			sheet.features.decode(buf, smallCollection(FEATURE, ::mutableSetOf))
			sheet.learnedSpells.decode(buf, smallCollection(SPELL, ::mutableSetOf))
			sheet.preparedSpells.decode(buf, smallCollection(SPELL, ::mutableSetOf))
			sheet.conditions.decode(buf, smallCollection(CONDITION, ::mutableSetOf))

			sheet.model = sheet.race.getModel(str.decode(buf))

			return sheet
		}

		override fun encode(buf: ByteBuf, value: CharacterSheet) {
			str.encode(buf, value.name)
			str.encode(buf, value.race.id)
			str.encode(buf, value.subrace?.name ?: "")
			str.encode(buf, value.background.id)

			bytInt.encode(buf, value.level)
			bytInt.encode(buf, value.armourClass)
			shtInt.encode(buf, value.maxHealth)
			shtInt.encode(buf, value.health)
			shtInt.encode(buf, value.darkVision)

			value.abilities.encode(buf)

			value.encodeProperties(buf)
			encodeMap(buf, value.classes, CLASS, bytInt)
			value.advantages.encode(buf, smallCollection(str, ::mutableSetOf))
			value.resistances.encode(buf, smallCollection(str, ::mutableSetOf))
			value.proficiencies.encode(buf, smallCollection(Proficiency.CODEC, ::mutableSetOf))
			value.features.encode(buf, smallCollection(FEATURE, ::mutableSetOf))
			value.learnedSpells.encode(buf, smallCollection(SPELL, ::mutableSetOf))
			value.preparedSpells.encode(buf, smallCollection(SPELL, ::mutableSetOf))
			value.conditions.encode(buf, smallCollection(CONDITION, ::mutableSetOf))

			str.encode(buf, value.model.id)
		}
	}
}