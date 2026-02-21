package com.pineypiney.mtt.client.dnd.spell_selector

import com.pineypiney.mtt.client.dnd.network.ClientDNDEntity
import com.pineypiney.mtt.dnd.spells.Spell
import com.pineypiney.mtt.entity.ShapeEntity
import com.pineypiney.mtt.mixin_interfaces.DNDClient
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d

class SpellLocationSelector(spell: Spell, val numLocations: Int) : SpellSelector(spell) {

	var hovering: Vec3d? = null
	var hoveredEntity: ClientDNDEntity? = null
	var hoveredShapeEntity: ShapeEntity? = null
	val selected = mutableListOf<ShapeEntity>()
	val affected = mutableListOf<ClientDNDEntity>()

	override fun update() {
		val hitResult = (MinecraftClient.getInstance() as DNDClient).`mTT$getDndCrosshairTarget`()
		when (hitResult?.type) {
			HitResult.Type.ENTITY -> {
				val entity = (hitResult as EntityHitResult).entity
				if (entity is ClientDNDEntity && entity != hovering) {
					unhover()
					hovering = entity.entityPos
					hoveredShapeEntity = getShapeEntity(entity.entityPos)
					hoveredShapeEntity?.colour = -1

					hoveredEntity = entity
					entity.highlight(true, -1)
				}
			}

			HitResult.Type.BLOCK -> {
				val pos = (hitResult as BlockHitResult).pos
				if (pos != hovering) {
					unhover()
					hovering = pos
					hoveredShapeEntity = getShapeEntity(pos)
					hoveredShapeEntity?.colour = -1
				}
			}

			else -> unhover()
		}
	}

	override fun select(): Boolean {
		return hoveredEntity?.let(::selectEntity)
			?: hovering?.let(::selectPosition)
			?: false
	}

	override fun unselect(): Boolean {
		return hoveredEntity?.let(::unselectEntity)
			?: hovering?.let(::unselectPosition)
			?: false
	}

	override fun selectionsMade(): Int = selected.size

	override fun getLocations(): List<Vec3d> {
		return selected.map { it.entityPos }
	}

	override fun getAngles(): List<Float> = emptyList()

	override fun cancel() {
		hoveredEntity?.highlight(false)
		hoveredEntity = null
		hoveredShapeEntity = null
		MinecraftClient.getInstance().world?.let { world ->
			for (entity in selected) world.removeEntity(entity.id, Entity.RemovalReason.DISCARDED)
		}
		selected.clear()
		for (entity in affected) entity.highlight(false)
		affected.clear()
	}

	fun selectPosition(pos: Vec3d): Boolean {
		if (selected.size < numLocations) {
			if (addPosition(pos)) {
				for (affectedEntity in getEntities(pos)) {
					if (!affected.contains(affectedEntity)) {
						affected.add(affectedEntity)
						affectedEntity.highlight(true, -0x800000)
					}
				}
				return true
			}
		}
		return false
	}

	fun unselectPosition(pos: Vec3d): Boolean {
		val shape = getShapeEntity(pos) ?: return false
		val index = selected.indexOf(shape)
		if (index >= 0) {
			selected.removeAt(index)
			if (!selected.contains(shape)) {
				MinecraftClient.getInstance().world?.removeEntity(shape.id, Entity.RemovalReason.DISCARDED)
				val stillAffected = selected.flatMap { getEntities(it.entityPos) }.toSet()
				for (affectedEntity in getEntities(shape.entityPos)) {
					if (!stillAffected.contains(affectedEntity)) {
						affected.remove(affectedEntity)
						affectedEntity.highlight(false)
					}
				}
			}
			return true
		}
		return false
	}

	fun selectEntity(entity: ClientDNDEntity): Boolean {
		val pos = entity.entityPos
		if (selected.size < numLocations) {
			if (addPosition(pos)) {
				for (affectedEntity in getEntities(pos)) {
					if (!affected.contains(affectedEntity)) {
						affected.add(affectedEntity)
						if (!selected.any { it.entityPos == affectedEntity.entityPos }) affectedEntity.highlight(
							true,
							-0x800000
						)
					}
				}
				return true
			}
		}
		return false
	}

	fun unselectEntity(entity: ClientDNDEntity): Boolean {
		val shape = getShapeEntity(entity.entityPos) ?: return false
		val index = selected.indexOf(shape)
		if (index >= 0) {
			selected.removeAt(index)
			if (!selected.contains(shape)) {
				MinecraftClient.getInstance().world?.removeEntity(shape.id, Entity.RemovalReason.DISCARDED)
				val stillAffected = selected.flatMap { getEntities(it.entityPos) }.toSet()
				for (affectedEntity in getEntities(shape.entityPos)) {
					if (!stillAffected.contains(affectedEntity)) {
						affected.remove(affectedEntity)
						if (affectedEntity != entity) affectedEntity.highlight(false)
					}
				}
			}
			return true
		}
		return false
	}

	fun addPosition(pos: Vec3d): Boolean {
		val world = MinecraftClient.getInstance().world ?: return false
		selected.firstOrNull { it.entityPos == pos }?.let {
			selected.add(it)
			return true
		}
		val entity = ShapeEntity(world, spell.settings.shape, spell.settings.shape.placeAtCentre(pos, 0f))
		return if (selected.add(entity)) {
			world.addEntity(entity)
			true
		} else false
	}

	fun unhover() {
		if (selected.any { it.entityPos == hoveredEntity?.entityPos }) hoveredEntity?.highlight(true, -0x010000)
		else if (affected.contains(hoveredEntity)) hoveredEntity?.highlight(true, -0x800000)
		else hoveredEntity?.highlight(false)
		hoveredEntity = null

		hoveredShapeEntity?.colour = -0x800000
		hoveredShapeEntity = null

		hovering = null
	}

	fun getShapeEntities(pos: Vec3d): List<ShapeEntity> {
		return selected.filter { it.shape.getHitBox(it.entityPos, it.yaw).contains(pos) }
	}

	fun getShapeEntity(pos: Vec3d): ShapeEntity? {
		return getShapeEntities(pos).minByOrNull { it.entityPos.distanceTo(pos) }
	}
}