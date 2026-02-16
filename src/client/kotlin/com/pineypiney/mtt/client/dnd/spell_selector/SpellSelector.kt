package com.pineypiney.mtt.client.dnd.spell_selector

import com.pineypiney.mtt.client.dnd.network.ClientDNDEntity
import com.pineypiney.mtt.dnd.spells.Spell
import com.pineypiney.mtt.entity.MTTEntities
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.Vec3d

abstract class SpellSelector(val spell: Spell) {

	abstract fun update()
	abstract fun select(): Boolean
	abstract fun unselect(): Boolean
	abstract fun selectionsMade(): Int
	abstract fun getLocations(): List<Vec3d>
	abstract fun cancel()

	fun getEntities(pos: Vec3d): List<ClientDNDEntity> {
		val world = MinecraftClient.getInstance().world ?: return emptyList()
		val cameraEntity = MinecraftClient.getInstance().cameraEntity ?: return emptyList()
		val direction = (pos.subtract(cameraEntity.entityPos)).yawAndPitch.x
		val box = spell.settings.shape.getContainingBox(pos, direction)
		val entities = world.getEntitiesByType(MTTEntities.DND_ENTITY, box) {
			spell.settings.shape.isIn(
				pos,
				direction,
				it.entityPos
			)
		}
		return entities.filterIsInstance<ClientDNDEntity>()
	}
}