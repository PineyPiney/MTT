package com.pineypiney.mtt.entity

import com.pineypiney.mtt.dnd.classes.DNDClass
import net.minecraft.entity.EntityType
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World

open class DNDClassEntity(type: EntityType<*>, world: World): DNDEntity(type, world) {

	val classes = mutableMapOf<DNDClass, Int>()

	fun calculateHealthFromClass(): Int{
		var health = 0
		for((clas, level) in classes){
			if(health == 0){
				health += clas.healthDie
				health += ((clas.healthDie / 2) + 1) * (level - 1)
			}
			else health += ((clas.healthDie / 2) + 1) * level
		}
		return health
	}

	override fun calculateProficiencyBonus() = MathHelper.ceilDiv(classes.values.sum(), 4) + 1
}