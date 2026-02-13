package com.pineypiney.mtt.entity

import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.util.Arm
import net.minecraft.world.World

class TestEntity(type: EntityType<out LivingEntity>, world: World) : LivingEntity(type, world) {


	override fun getMainArm(): Arm {
		return Arm.RIGHT
	}
}