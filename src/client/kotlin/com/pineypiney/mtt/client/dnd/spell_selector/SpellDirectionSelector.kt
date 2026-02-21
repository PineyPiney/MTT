package com.pineypiney.mtt.client.dnd.spell_selector

import com.pineypiney.mtt.client.dnd.network.ClientDNDEntity
import com.pineypiney.mtt.dnd.spells.Spell
import com.pineypiney.mtt.entity.ShapeEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d

class SpellDirectionSelector(spell: Spell, val world: ClientWorld) : SpellSelector(spell) {

	var placed = false
	var hoveredShapeEntity: ShapeEntity = ShapeEntity(world, spell.settings.shape, Vec3d.ZERO)
	val affected = mutableListOf<ClientDNDEntity>()

	init {
		world.addEntity(hoveredShapeEntity)
	}

	override fun update() {
		if (placed) return

		val camera = MinecraftClient.getInstance().cameraEntity ?: return
		hoveredShapeEntity.setPosition(camera.entityPos)
		hoveredShapeEntity.yaw = camera.yaw
		hoveredShapeEntity.calculateDimensions()

		val stillAffected = getEntities(hoveredShapeEntity.entityPos, hoveredShapeEntity.yaw)
		var i = 0
		while (i < affected.size) {
			if (stillAffected.contains(affected[i])) i++
			else {
				(affected[i].highlight(false))
				affected.removeAt(i)
			}
		}
		for (entity in stillAffected) {
			if (!affected.contains(entity)) {
				entity.highlight(true, -0x800000)
				affected.add(entity)
			}
		}
	}

	override fun select(): Boolean {
		if (placed) return false
		placed = true
		return true
	}

	override fun unselect(): Boolean {
		if (!placed) return false
		placed = false
		return true
	}

	override fun selectionsMade(): Int = 1

	override fun getLocations(): List<Vec3d> {
		return listOf(hoveredShapeEntity.entityPos)
	}

	override fun getAngles(): List<Float> = listOf(hoveredShapeEntity.yaw)

	override fun cancel() {
		world.removeEntity(hoveredShapeEntity.id, Entity.RemovalReason.DISCARDED)
		for (entity in affected) entity.highlight(false)
		affected.clear()
	}
}