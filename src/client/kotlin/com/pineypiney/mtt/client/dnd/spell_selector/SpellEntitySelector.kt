package com.pineypiney.mtt.client.dnd.spell_selector

import com.pineypiney.mtt.client.dnd.network.ClientDNDEntity
import com.pineypiney.mtt.dnd.spells.Spell
import com.pineypiney.mtt.mixin_interfaces.DNDClient
import net.minecraft.client.MinecraftClient
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d

class SpellEntitySelector(spell: Spell, val numEntities: Int, val duplicate: Boolean) : SpellSelector(spell) {

	var hovering: ClientDNDEntity? = null
	val selected = mutableListOf<ClientDNDEntity>()
	val affected = mutableListOf<ClientDNDEntity>()

	override fun update() {
		val hitResult = (MinecraftClient.getInstance() as DNDClient).`mTT$getDndCrosshairTarget`()
		if (hitResult?.type == HitResult.Type.ENTITY) {
			val entity = (hitResult as EntityHitResult).entity
			if (entity is ClientDNDEntity && entity != hovering) {
				unhover()
				entity.highlight(true, -1)
				hovering = entity
			}
		} else unhover()
	}

	override fun select(): Boolean {
		return hovering?.let { hovering ->
			select(hovering)
		} ?: false
	}

	override fun unselect(): Boolean {
		return hovering?.let { hovering ->
			unselect(hovering)
		} ?: false
	}

	override fun selectionsMade(): Int = selected.size

	override fun getLocations(): List<Vec3d> {
		return selected.map { it.entityPos }
	}

	override fun getAngles(): List<Float> = emptyList()

	override fun cancel() {
		hovering?.highlight(false)
		hovering = null
		for (entity in selected) entity.highlight(false)
		selected.clear()
		for (entity in affected) entity.highlight(false)
		affected.clear()
	}

	fun select(entity: ClientDNDEntity): Boolean {
		if (selected.size < numEntities && (duplicate || !selected.contains(entity))) {
			if (selected.add(entity)) {
				for (affectedEntity in getEntities(entity.entityPos)) {
					if (affectedEntity != entity && !affected.contains(affectedEntity)) {
						affected.add(affectedEntity)
						if (!selected.contains(affectedEntity)) affectedEntity.highlight(true, -0x800000)
					}
				}
				return true
			}
		}
		return false
	}

	fun unselect(entity: ClientDNDEntity): Boolean {
		val index = selected.indexOf(entity)
		if (index >= 0) {
			selected.removeAt(index)
			if (!selected.contains(entity)) {
				val stillAffected = selected.flatMap { getEntities(it.entityPos) }
				for (affectedEntity in getEntities(entity.entityPos)) {
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

	fun unhover() {
		if (selected.contains(hovering)) hovering?.highlight(true, -0x010000)
		else if (affected.contains(hovering)) hovering?.highlight(true, -0x800000)
		else hovering?.highlight(false)
		hovering = null
	}
}