package com.pineypiney.mtt.client.dnd.network

import com.pineypiney.mtt.client.dnd.ClientDNDEngine
import com.pineypiney.mtt.network.payloads.c2s.CharacterMoveC2SPayload
import com.pineypiney.mtt.network.payloads.c2s.TeleportConfirmC2SPayload
import com.pineypiney.mtt.network.payloads.s2c.CharacterDamageS2CPayload
import com.pineypiney.mtt.network.payloads.s2c.CharacterPositionLookS2CPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityPosition
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.PositionFlag
import net.minecraft.util.math.Vec3d

class ClientCharacterNetworkHandler(val engine: ClientDNDEngine) {

	fun onPlayerPositionLook(payload: CharacterPositionLookS2CPayload) {
		val playerEntity: PlayerEntity = engine.client.player ?: return
		val entity = engine.getEntityFromPlayer(playerEntity.uuid) ?: return
		if (!entity.hasVehicle()) {
			setPosition(payload.change, payload.relatives, entity, false)
		}

		ClientPlayNetworking.send(TeleportConfirmC2SPayload(payload.teleportID))
		ClientPlayNetworking.send(
			CharacterMoveC2SPayload(
				entity.entityPos,
				entity.yaw,
				entity.pitch,
				onGround = false,
				horizontalCollision = false
			)
		)
	}

	private fun setPosition(
		pos: EntityPosition,
		flags: Set<PositionFlag?>,
		entity: Entity,
		bl: Boolean
	): Boolean {
		val entityPosition = EntityPosition.fromEntity(entity)
		val entityPosition2 = EntityPosition.apply(entityPosition, pos, flags)
		val bl2 = entityPosition.position().squaredDistanceTo(entityPosition2.position()) > 4096.0
		if (bl && !bl2) {
			entity.updateTrackedPositionAndAngles(
				entityPosition2.position(),
				entityPosition2.yaw(),
				entityPosition2.pitch()
			)
			entity.velocity = entityPosition2.deltaMovement()
			return true
		} else {
			entity.setPosition(entityPosition2.position())
			entity.velocity = entityPosition2.deltaMovement()
			entity.yaw = entityPosition2.yaw()
			entity.pitch = entityPosition2.pitch()
			val entityPosition3 = EntityPosition(entity.lastRenderPos, Vec3d.ZERO, entity.lastYaw, entity.lastPitch)
			val entityPosition4 = EntityPosition.apply(entityPosition3, pos, flags)
			entity.setLastPositionAndAngles(entityPosition4.position(), entityPosition4.yaw(), entityPosition4.pitch())
			return false
		}
	}

	fun onCharacterDamage(payload: CharacterDamageS2CPayload) {
		val character = engine.getCharacter(payload.character) ?: return
		character.health -= payload.amount

		val entity = engine.getEntityOfCharacter(character.uuid) as? ClientDNDEntity ?: return
		entity.addSplashText(payload.type.getAmountText(payload.amount))
	}
}