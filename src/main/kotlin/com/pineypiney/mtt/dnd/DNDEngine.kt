package com.pineypiney.mtt.dnd

import com.pineypiney.mtt.entity.DNDPlayerEntity
import net.minecraft.network.packet.CustomPayload
import java.util.*

abstract class DNDEngine {

	open var running: Boolean = false
	open var DM: UUID? = null

	abstract val players: List<DNDPlayerEntity>
	fun getPlayerCharacters() = players

	fun getPlayer(name: String): DNDPlayerEntity? = players.firstOrNull { it.name == name }
	fun getPlayer(uuid: UUID): DNDPlayerEntity? = players.firstOrNull { uuid == (it.controllingPlayer) }

	fun tick(){

	}

	val updates = mutableListOf<CustomPayload>()
}