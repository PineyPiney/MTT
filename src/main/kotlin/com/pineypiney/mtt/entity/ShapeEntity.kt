package com.pineypiney.mtt.entity

import com.pineypiney.mtt.dnd.spells.SpellShape
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityPose
import net.minecraft.entity.EntityType
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.data.DataTracker
import net.minecraft.server.world.ServerWorld
import net.minecraft.storage.ReadView
import net.minecraft.storage.WriteView
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class ShapeEntity(type: EntityType<ShapeEntity>, world: World) : Entity(type, world) {

	lateinit var shape: SpellShape
	var colour = -1

	constructor(world: World, shape: SpellShape, pos: Vec3d) : this(MTTEntities.SHAPE, world) {
		this.shape = shape
		this.setPosition(pos)
	}

	override fun getDimensions(pose: EntityPose?): EntityDimensions? {
		val box = shape.getHitBox(entityPos, yaw)
		return EntityDimensions.fixed(box.lengthX.toFloat(), .5f)
	}

	override fun initDataTracker(builder: DataTracker.Builder) {}
	override fun damage(world: ServerWorld, source: DamageSource, amount: Float): Boolean = false
	override fun readCustomData(view: ReadView) {}
	override fun writeCustomData(view: WriteView) {}
}