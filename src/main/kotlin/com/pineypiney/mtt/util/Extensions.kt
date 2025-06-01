package com.pineypiney.mtt.util

import com.pineypiney.mtt.dnd.DNDEngine
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder
import net.minecraft.world.World
import java.util.*
import kotlin.jvm.optionals.getOrNull

fun <T: Any> T?.optional(): Optional<T> {
	return if(this == null) Optional.empty()
	else Optional.of(this)
}

fun <T: Any> Optional<T>.nullable(): T? {
	return this.getOrNull()
}

fun World.getEngine(): DNDEngine = (this as DNDEngineHolder<*>).`mtt$getDNDEngine`()