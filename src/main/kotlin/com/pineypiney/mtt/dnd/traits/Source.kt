package com.pineypiney.mtt.dnd.traits

import com.pineypiney.mtt.dnd.Background
import com.pineypiney.mtt.dnd.DNDServerEngine
import com.pineypiney.mtt.dnd.classes.DNDClass
import com.pineypiney.mtt.dnd.species.Species
import com.pineypiney.mtt.dnd.traits.features.Feature
import net.minecraft.nbt.NbtCompound
import java.util.*
import kotlin.jvm.optionals.getOrNull

sealed interface Source {

	val overridePower: Int

	fun writeNbt(nbt: NbtCompound)

	class SpeciesSource(val species: Species): Source {
		override val overridePower: Int = 0
		override fun writeNbt(nbt: NbtCompound) {
			nbt.putString("species", species.id)
		}
		override fun equals(other: Any?): Boolean {
			return other is SpeciesSource && species == other.species
		}
		override fun hashCode(): Int {
			return species.hashCode()
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
	class FeatureSource(val feature: Feature): Source {
		override val overridePower: Int = 16
		override fun writeNbt(nbt: NbtCompound) {
			nbt.putString("feature", feature.id)
		}
		override fun equals(other: Any?): Boolean {
			return other is FeatureSource && feature == other.feature
		}
		override fun hashCode(): Int {
			return feature.hashCode()
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
			nbt.getString("species").getOrNull()?.let {
				val species = engine.loadedSpecies[it]
				if(species != null) return SpeciesSource(species)
			}
			nbt.getString("class").getOrNull()?.let { id ->
				val clazz = DNDClass.classes.firstOrNull { it.id == id }
				if(clazz != null) return ClassSource(clazz)
			}
			return null
		}
	}
}