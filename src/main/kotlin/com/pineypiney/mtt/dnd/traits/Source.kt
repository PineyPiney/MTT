package com.pineypiney.mtt.dnd.traits

import com.pineypiney.mtt.dnd.Background
import com.pineypiney.mtt.dnd.DNDServerEngine
import com.pineypiney.mtt.dnd.classes.DNDClass
import com.pineypiney.mtt.dnd.race.Race
import net.minecraft.nbt.NbtCompound
import java.util.*
import kotlin.jvm.optionals.getOrNull

sealed interface Source {

	val overridePower: Int

	fun writeNbt(nbt: NbtCompound)

	class RaceSource(val race: Race): Source {
		override val overridePower: Int = 0
		override fun writeNbt(nbt: NbtCompound) {
			nbt.putString("race", race.id)
		}
		override fun equals(other: Any?): Boolean {
			return other is RaceSource && race == other.race
		}
		override fun hashCode(): Int {
			return race.hashCode()
		}
	}
	class ClassSource(val clazz: DNDClass) : Source {
		override val overridePower: Int = 1
		override fun writeNbt(nbt: NbtCompound) {
			nbt.putString("class", clazz.id)
		}
		override fun equals(other: Any?): Boolean {
			return other is ClassSource && clazz == other.clazz
		}
		override fun hashCode(): Int {
			return clazz.hashCode()
		}
	}
	class BackgroundSource(val background: Background) : Source {
		override val overridePower: Int = 2
		override fun writeNbt(nbt: NbtCompound) {
			nbt.putString("background", background.id)
		}

		override fun equals(other: Any?): Boolean {
			return other is BackgroundSource && background == other.background
		}

		override fun hashCode(): Int {
			return background.hashCode()
		}
	}
	class FeatureSource(val id: String): Source {
		override val overridePower: Int = 16
		override fun writeNbt(nbt: NbtCompound) {
			nbt.putString("feature", id)
		}
		override fun equals(other: Any?): Boolean {
			return other is FeatureSource && id == other.id
		}
		override fun hashCode(): Int {
			return id.hashCode()
		}
	}

	class DMOverrideSource(val dm: UUID): Source {
		override val overridePower: Int = Int.MAX_VALUE
		override fun writeNbt(nbt: NbtCompound) {
			val str = ByteArray(16)
			for(i in 0..7){
				str[i] = ((dm.mostSignificantBits shr (i * 8)) and 255).toByte()
				str[i + 8] = ((dm.leastSignificantBits shr (i * 8)) and 255).toByte()
			}
			nbt.putString("dm", str.toString(Charsets.ISO_8859_1))
		}

		override fun equals(other: Any?): Boolean {
			return other is DMOverrideSource && dm == other.dm
		}

		override fun hashCode(): Int {
			return dm.hashCode()
		}
	}

	companion object {

		fun readNbt(nbt: NbtCompound, engine: DNDServerEngine): Source?{
			nbt.getString("race").getOrNull()?.let {
				val race = Race.findById(it)
				return RaceSource(race)
			}
			nbt.getString("class").getOrNull()?.let { id ->
				val clazz = DNDClass.classes.firstOrNull { it.id == id }
				if(clazz != null) return ClassSource(clazz)
			}
			return null
		}
	}
}