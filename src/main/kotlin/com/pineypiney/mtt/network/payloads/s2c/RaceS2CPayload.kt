package com.pineypiney.mtt.network.payloads.s2c

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.race.NamedTrait
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.dnd.race.SubRace
import com.pineypiney.mtt.dnd.traits.CreatureType
import com.pineypiney.mtt.dnd.traits.Trait
import com.pineypiney.mtt.dnd.traits.TraitCodec
import com.pineypiney.mtt.dnd.traits.Traits
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

class RaceS2CPayload(val race: Race) : CustomPayload {
	override fun getId(): CustomPayload.Id<out CustomPayload?>? {
		return ID
	}

	companion object {

		val RACE_PACKET_CODEC = object : PacketCodec<ByteBuf, Race>{

//			fun decodeTraitList(buf: ByteBuf): List<TraitComponent<*, *>>{
//				val numComponents = PacketCodecs.BYTE.decode(buf).toInt()
//				return List(numComponents){
//					val componentID = PacketCodecs.STRING.decode(buf)
//					TraitComponents.list[componentID]?.decode(buf)
//				}.filterNotNull()
//			}

			fun decodeTrait(buf: ByteBuf): Trait<*>? {
				val componentID = PacketCodecs.STRING.decode(buf)
				return Traits.getCodec(componentID)?.decode(buf)
			}
			fun decodeNewTraitList(buf: ByteBuf): List<Trait<*>>{
				val numComponents = PacketCodecs.BYTE.decode(buf).toInt()
				return List(numComponents){ decodeTrait(buf) }.filterNotNull()
			}

			override fun decode(buf: ByteBuf): Race {
				val name = PacketCodecs.STRING.decode(buf)
				val type = PacketCodecs.STRING.decode(buf)
				val movement = PacketCodecs.INTEGER.decode(buf)
				val sizeTrait = TraitCodec.SIZE_CODEC.decode(buf)
				val model = TraitCodec.MODEL_CODEC.decode(buf)

				val components = decodeNewTraitList(buf)

				val numNamedTraits = PacketCodecs.BYTE.decode(buf).toInt()
				val namedTraits = List(numNamedTraits){ NamedTrait(PacketCodecs.STRING.decode(buf), decodeNewTraitList(buf).toSet()) }

				val numSubRace = PacketCodecs.BYTE.decode(buf).toInt()
				val subRace = List(numSubRace){
					val subName = PacketCodecs.STRING.decode(buf)
					val subRaceComponents = decodeNewTraitList(buf)

					val numSubRaceNamedTraits = PacketCodecs.BYTE.decode(buf).toInt()
					val subRaceNamedTraits = List(numSubRaceNamedTraits){ NamedTrait(PacketCodecs.STRING.decode(buf), decodeNewTraitList(buf).toSet()) }
					SubRace(subName, subRaceComponents, subRaceNamedTraits)
				}

				return Race(name, CreatureType.valueOf(type.uppercase()), movement, sizeTrait, model, components, namedTraits, subRace)
			}

//			fun <T: TraitComponent<*, *>> encodeTrait(buf: ByteBuf, it: T){
//				val codec = it.getCodec()
//				PacketCodecs.STRING.encode(buf, codec.ID)
//				it.encode(buf)
//			}

//			fun encodeTraitList(buf: ByteBuf, components: List<TraitComponent<*, *>>){
//				PacketCodecs.BYTE.encode(buf, components.size.toByte())
//				components.forEach {
//					encodeTrait(buf, it)
//				}
//			}

			fun encodeNewTraitCollection(buf: ByteBuf, traits: Collection<Trait<*>>){
				PacketCodecs.BYTE.encode(buf, traits.size.toByte())
				traits.forEach { it.encode(buf) }
			}

			override fun encode(buf: ByteBuf, value: Race) {
				PacketCodecs.STRING.encode(buf, value.id)
				PacketCodecs.STRING.encode(buf, value.type.name)
				PacketCodecs.INTEGER.encode(buf, value.speed)
				TraitCodec.SIZE_CODEC.encode(buf, value.size)
				TraitCodec.MODEL_CODEC.encode(buf, value.model)

				encodeNewTraitCollection(buf, value.traits)

				PacketCodecs.BYTE.encode(buf, value.namedTraits.size.toByte())
				value.namedTraits.forEach {
					PacketCodecs.STRING.encode(buf, it.name)
					encodeNewTraitCollection(buf, it.traits)
				}

				PacketCodecs.BYTE.encode(buf, value.subRace.size.toByte())
				value.subRace.forEach {
					PacketCodecs.STRING.encode(buf, it.name)
					encodeNewTraitCollection(buf, it.traits)
					PacketCodecs.BYTE.encode(buf, it.namedTraits.size.toByte())
					it.namedTraits.forEach { namedTrait ->
						PacketCodecs.STRING.encode(buf, namedTrait.name)
						encodeNewTraitCollection(buf, namedTrait.traits)
					}
				}
			}
		}

		val RACE_PAYLOAD_ID = Identifier.of(MTT.MOD_ID, "race")
		val ID = CustomPayload.Id<RaceS2CPayload>(RACE_PAYLOAD_ID)
		val CODEC = PacketCodec.tuple(RACE_PACKET_CODEC, RaceS2CPayload::race, ::RaceS2CPayload)
	}
}