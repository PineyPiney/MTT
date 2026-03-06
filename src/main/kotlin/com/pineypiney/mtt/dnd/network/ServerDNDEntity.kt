package com.pineypiney.mtt.dnd.network

import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.server.ServerDNDEngine
import com.pineypiney.mtt.entity.DNDEntity
import com.pineypiney.mtt.util.getEngine
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityPosition
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.TeleportTarget
import net.minecraft.world.World
import java.util.*

class ServerDNDEntity : DNDEntity {
	constructor(world: World) : super(world)
	constructor(world: World, character: Character) : super(world, character)

	override var character: ServerCharacter? = null

	override fun teleportTo(teleportTarget: TeleportTarget): Entity? {
		val handler = entityWorld.server!!.getEngine().characterManager[this]
		handler?.requestTeleport(this, EntityPosition.fromTeleportTarget(teleportTarget), teleportTarget.relatives)
		return super.teleportTo(teleportTarget)
	}

	override fun getEntityWorld(): ServerWorld {
		return super.getEntityWorld() as ServerWorld
	}

	override fun updateCharacter(uuid: UUID) {
		character = (entityWorld.getEngine() as ServerDNDEngine).getCharacter(uuid) ?: character
		calculateDimensions()
	}
}