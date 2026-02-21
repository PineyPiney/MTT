package com.pineypiney.mtt.dnd.traits

import com.pineypiney.mtt.dnd.Background
import com.pineypiney.mtt.dnd.classes.DNDClass
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.dnd.race.Subrace
import com.pineypiney.mtt.dnd.server.ServerDNDEngine
import com.pineypiney.mtt.dnd.traits.features.Features
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
	class SubraceSource(val race: Race, val subrace: Subrace) : Source {
		override val overridePower: Int = 0
		override fun writeNbt(nbt: NbtCompound) {
			nbt.putString("race", race.id)
			nbt.putString("subrace", subrace.name)
		}

		override fun equals(other: Any?): Boolean {
			return other is SubraceSource && subrace == other.subrace
		}

		override fun hashCode(): Int {
			return subrace.hashCode()
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

	object SpellSource : Source {
		override val overridePower: Int = 32

		override fun writeNbt(nbt: NbtCompound) {
			nbt.putString("spell", "spell")
		}
	}

	class DMOverrideSource(val dm: UUID): Source {
		override val overridePower: Int = Int.MAX_VALUE
		override fun writeNbt(nbt: NbtCompound) {
			nbt.putLongArray("dm", longArrayOf(dm.mostSignificantBits, dm.leastSignificantBits))
		}

		override fun equals(other: Any?): Boolean {
			return other is DMOverrideSource && dm == other.dm
		}

		override fun hashCode(): Int {
			return dm.hashCode()
		}
	}

	companion object {

		fun readNbt(nbt: NbtCompound, engine: ServerDNDEngine): Source? {
			nbt.getString("subrace").getOrNull()?.let { id ->
				val race = Race.findById(nbt.getString("race").get())
				return SubraceSource(race, race.getSubrace(id) ?: return null)
			}
			nbt.getString("race").getOrNull()?.let { id ->
				val race = Race.findById(id)
				return RaceSource(race)
			}
			nbt.getString("class").getOrNull()?.let { id ->
				val clazz = DNDClass.findById(id)
				return ClassSource(clazz)
			}
			nbt.getString("background").getOrNull()?.let { id ->
				val background = Background.findById(id)
				return BackgroundSource(background)
			}
			nbt.getString("feature").getOrNull()?.let { id ->
				val feature = Features.set.firstOrNull { it.id == id } ?: return null
				return FeatureSource(feature.id)
			}
			nbt.getString("spell").getOrNull()?.let { id ->
				return SpellSource
			}
			nbt.getLongArray("dm").getOrNull()?.let {
				return DMOverrideSource(UUID(it[0], it[1]))
			}
			return null
		}
	}
}