package com.pineypiney.mtt

import com.pineypiney.mtt.dnd.CreatureType
import com.pineypiney.mtt.dnd.DNDServerEngine
import com.pineypiney.mtt.dnd.Size
import com.pineypiney.mtt.dnd.species.Species
import com.pineypiney.mtt.entity.classes.DNDClass
import net.minecraft.nbt.NbtCompound
import kotlin.jvm.optionals.getOrNull

class CharacterSheet {
	var name = "Unnamed Character"
	var species: Species = Species.NONE
	var level = 1
	var type = CreatureType.HUMANOID
	var speed = 30
	var size = Size.MEDIUM
	var model = "default"
	var darkVision = 0

	val proficiencies = mutableListOf<String>()
	val classes = mutableMapOf<DNDClass, Int>()

	fun writeNbt(): NbtCompound{
		val sheetNbt = NbtCompound()
		sheetNbt.putString("name", name)
		sheetNbt.putString("species", species.id)
		sheetNbt.putInt("level", level)
		sheetNbt.putInt("movement", speed)
		sheetNbt.putString("size", size.name)
		sheetNbt.putString("model", model)
		sheetNbt.putInt("darkVision", darkVision)
		return sheetNbt
	}

	fun readNbt(nbt: NbtCompound, engine: DNDServerEngine){
		nbt.getString("name").getOrNull()?.let { name = it }
		nbt.getString("species").getOrNull()?.let{ species = engine.loadedSpecies[it] ?: return@let }
		nbt.getInt("level").getOrNull()?.let { level = it }
		nbt.getInt("movement").getOrNull()?.let { speed = it }
		nbt.getString("size").getOrNull()?.let{ size = Size.fromString(it) }
		nbt.getString("model").getOrNull()?.let { model = it }
		nbt.getInt("darkVision").getOrNull()?.let { darkVision = it }
	}
}