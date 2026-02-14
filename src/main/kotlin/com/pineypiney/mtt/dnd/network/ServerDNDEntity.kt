package com.pineypiney.mtt.dnd.network

import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.entity.DNDEntity
import net.minecraft.world.World

class ServerDNDEntity : DNDEntity {
	constructor(world: World) : super(world)
	constructor(world: World, character: Character) : super(world, character)
}