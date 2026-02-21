package com.pineypiney.mtt.network.payloads.c2s

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.spells.Spell
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import org.joml.Vector3fc

class CastSpellC2SPayload(val spell: Spell, val level: Int, val locations: List<Vector3fc>, val angles: List<Float>) : CustomPayload {

	constructor(spell: Spell, level: Int, locations: Collection<Vec3d>, angles: List<Float>) : this(
		spell,
		level,
		locations.map(Vec3d::toVector3f),
		angles
	)

	override fun getId(): CustomPayload.Id<out CustomPayload> = ID

	companion object {

		val PAYLOAD_ID = Identifier.of(MTT.MOD_ID, "cast_spell")
		val ID = CustomPayload.Id<CastSpellC2SPayload>(PAYLOAD_ID)
		val CODEC = PacketCodec.tuple(
			MTTPacketCodecs.SPELL, CastSpellC2SPayload::spell,
			MTTPacketCodecs.bytInt, CastSpellC2SPayload::level,
			MTTPacketCodecs.smallCollection(PacketCodecs.VECTOR_3F, ::List), CastSpellC2SPayload::locations,
			MTTPacketCodecs.smallCollection(PacketCodecs.FLOAT, ::List), CastSpellC2SPayload::angles,
			::CastSpellC2SPayload
		)
	}
}