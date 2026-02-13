package com.pineypiney.mtt.network.payloads.c2s

import com.pineypiney.mtt.MTT
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import kotlin.experimental.and

class CharacterMoveC2SPayload(val pos: Vec3d, val yaw: Float, val pitch: Float, val bools: Byte) : CustomPayload {

	val onGround = bools and 8 > 0
	val horizontalCollision = bools and 4 > 0
	val changePosition = bools and 2 > 0
	val changeLook = bools and 1 > 0

	constructor(pos: Vec3d, yaw: Float, pitch: Float, onGround: Boolean, horizontalCollision: Boolean) : this(
		pos,
		yaw,
		pitch,
		makeByte(onGround, horizontalCollision, true, true)
	)

	constructor(pos: Vec3d, onGround: Boolean, horizontalCollision: Boolean) : this(
		pos,
		0f,
		0f,
		makeByte(onGround, horizontalCollision, true, false)
	)

	constructor(yaw: Float, pitch: Float, onGround: Boolean, horizontalCollision: Boolean) : this(
		Vec3d.ZERO,
		yaw,
		pitch,
		makeByte(onGround, horizontalCollision, false, true)
	)

	constructor(onGround: Boolean, horizontalCollision: Boolean) : this(
		Vec3d.ZERO,
		0f,
		0f,
		makeByte(onGround, horizontalCollision, false, false)
	)

	fun getPos(currentPos: Vec3d) = if (this.changePosition) this.pos else currentPos

	fun getX(currentX: Double): Double {
		return if (this.changePosition) this.pos.x else currentX
	}

	fun getY(currentY: Double): Double {
		return if (this.changePosition) this.pos.y else currentY
	}

	fun getZ(currentZ: Double): Double {
		return if (this.changePosition) this.pos.z else currentZ
	}

	fun getYaw(currentYaw: Float): Float {
		return if (this.changeLook) this.yaw else currentYaw
	}

	fun getPitch(currentPitch: Float): Float {
		return if (this.changeLook) this.pitch else currentPitch
	}

	override fun getId(): CustomPayload.Id<out CustomPayload> = ID

	companion object {

		fun makeByte(
			onGround: Boolean,
			horizontalCollision: Boolean,
			changePosition: Boolean,
			changeLook: Boolean
		): Byte {
			var i = 0
			if (onGround) i = i or 8
			if (horizontalCollision) i = i or 4
			if (changePosition) i = i or 2
			if (changeLook) i = i or 1
			return i.toByte()
		}

		val PAYLOAD_ID = Identifier.of(MTT.MOD_ID, "character_move")
		val ID = CustomPayload.Id<CharacterMoveC2SPayload>(PAYLOAD_ID)
		val CODEC = PacketCodec.tuple(
			Vec3d.PACKET_CODEC, CharacterMoveC2SPayload::pos,
			PacketCodecs.FLOAT, CharacterMoveC2SPayload::yaw,
			PacketCodecs.FLOAT, CharacterMoveC2SPayload::pitch,
			PacketCodecs.BYTE, CharacterMoveC2SPayload::bools,
			::CharacterMoveC2SPayload
		)
	}
}