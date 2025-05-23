package com.pineypiney.mtt.network.payloads.s2c

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.CreatureType
import com.pineypiney.mtt.dnd.Size
import com.pineypiney.mtt.dnd.species.NamedTrait
import com.pineypiney.mtt.dnd.species.Species
import com.pineypiney.mtt.dnd.species.SubSpecies
import com.pineypiney.mtt.dnd.traits.Trait
import com.pineypiney.mtt.dnd.traits.TraitComponent
import com.pineypiney.mtt.dnd.traits.TraitComponents
import com.pineypiney.mtt.dnd.traits.decodeTrait
import com.pineypiney.mtt.dnd.traits.encodeTrait
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

class SpeciesS2CPayload(val species: Species) : CustomPayload {
	override fun getId(): CustomPayload.Id<out CustomPayload?>? {
		return ID
	}

	companion object {

		val SPECIES_PACKET_CODEC = object : PacketCodec<ByteBuf, Species>{

			fun decodeTraitList(buf: ByteBuf): List<TraitComponent<*>>{
				val numComponents = PacketCodecs.BYTE.decode(buf).toInt()
				return List(numComponents){
					val componentID = PacketCodecs.STRING.decode(buf)
					TraitComponents.list[componentID]?.decode(buf)
				}.filterNotNull()
			}

			override fun decode(buf: ByteBuf): Species {
				val name = PacketCodecs.STRING.decode(buf)
				val type = PacketCodecs.STRING.decode(buf)
				val movement = PacketCodecs.INTEGER.decode(buf)
				val sizeTrait: Trait<Size> = decodeTrait(buf, PacketCodecs.STRING, Size::fromString)
				val model = decodeTrait(buf, PacketCodecs.STRING)

				val components = decodeTraitList(buf)

				val numNamedTraits = PacketCodecs.BYTE.decode(buf).toInt()
				val namedTraits = List(numNamedTraits){ NamedTrait(PacketCodecs.STRING.decode(buf), PacketCodecs.STRING.decode(buf), decodeTraitList(buf)) }

				val numSubSpecies = PacketCodecs.BYTE.decode(buf).toInt()
				val subSpecies = List(numSubSpecies){
					val subName = PacketCodecs.STRING.decode(buf)
					val subSpeciesComponents = decodeTraitList(buf)

					val numSubSpeciesNamedTraits = PacketCodecs.BYTE.decode(buf).toInt()
					val subSpeciesNamedTraits = List(numSubSpeciesNamedTraits){ NamedTrait(PacketCodecs.STRING.decode(buf), PacketCodecs.STRING.decode(buf), decodeTraitList(buf)) }
					SubSpecies(subName, subSpeciesComponents, subSpeciesNamedTraits)
				}

				return Species(name, CreatureType.valueOf(type.uppercase()), movement, sizeTrait, model, components, namedTraits, subSpecies)
			}

			fun <T: TraitComponent<*>> encodeTrait(buf: ByteBuf, it: T){
				val codec = it.getCodec()
				PacketCodecs.STRING.encode(buf, codec.ID)
				it.encode(buf)
			}

			fun encodeTraitList(buf: ByteBuf, components: List<TraitComponent<*>>){
				PacketCodecs.BYTE.encode(buf, components.size.toByte())
				components.forEach {
					encodeTrait(buf, it)
				}
			}

			override fun encode(buf: ByteBuf, value: Species) {
				PacketCodecs.STRING.encode(buf, value.id)
				PacketCodecs.STRING.encode(buf, value.type.name)
				PacketCodecs.INTEGER.encode(buf, value.speed)
				encodeTrait(buf, value.size, PacketCodecs.STRING, Size::name)
				encodeTrait(buf, value.model, PacketCodecs.STRING)

				encodeTraitList(buf, value.components)

				PacketCodecs.BYTE.encode(buf, value.namedTraits.size.toByte())
				value.namedTraits.forEach {
					PacketCodecs.STRING.encode(buf, it.name)
					PacketCodecs.STRING.encode(buf, it.description)
					encodeTraitList(buf, it.components)
				}

				PacketCodecs.BYTE.encode(buf, value.subspecies.size.toByte())
				value.subspecies.forEach {
					PacketCodecs.STRING.encode(buf, it.name)
					encodeTraitList(buf, it.components)
					PacketCodecs.BYTE.encode(buf, it.namedTraits.size.toByte())
					it.namedTraits.forEach { t ->
						PacketCodecs.STRING.encode(buf, t.name)
						PacketCodecs.STRING.encode(buf, t.description)
						encodeTraitList(buf, t.components)
					}
				}
			}
		}

		val SPECIES_PAYLOAD_ID = Identifier.of(MTT.Companion.MOD_ID, "species")
		val ID = CustomPayload.Id<SpeciesS2CPayload>(SPECIES_PAYLOAD_ID)
		val CODEC = PacketCodec.tuple(SPECIES_PACKET_CODEC, SpeciesS2CPayload::species, ::SpeciesS2CPayload)
	}
}