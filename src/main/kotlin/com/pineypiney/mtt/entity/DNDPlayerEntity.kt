package com.pineypiney.mtt.entity

import com.pineypiney.mtt.dnd.characters.SheetCharacter
import com.pineypiney.mtt.screen.DNDScreenHandler
import com.pineypiney.mtt.util.optional
import net.minecraft.entity.EntityType
import net.minecraft.entity.data.DataTracker
import net.minecraft.world.World
import java.util.*
import kotlin.jvm.optionals.getOrNull

class DNDPlayerEntity(type: EntityType<*>, world: World): DNDClassEntity(type, world) {

	constructor(type: EntityType<*>, world: World, character: SheetCharacter): this(type, world){
		this.character = character
		this.name = character.name
		this.setPosition(character.pos)
		dataTracker.set(CHARACTER_UUID, character.uuid)
	}

	val screenHandler = DNDScreenHandler(1, character.inventory)

	var controllingPlayer: UUID?
		get() = this.dataTracker[CONTROLLING_PLAYER].getOrNull()
		set(value) { dataTracker[CONTROLLING_PLAYER] = value.optional() }

	init {
		isCustomNameVisible = true
	}

	override fun initDataTracker(builder: DataTracker.Builder) {
		super.initDataTracker(builder)
		builder.add(CONTROLLING_PLAYER, Optional.empty())
	}

	companion object {
		val CONTROLLING_PLAYER = DataTracker.registerData(DNDPlayerEntity::class.java, MTTEntities.OPTIONAL_UUID_TRACKER)
	}
}