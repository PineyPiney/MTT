package com.pineypiney.mtt.dnd.characters

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.DNDEngine
import com.pineypiney.mtt.dnd.classes.DNDClass
import com.pineypiney.mtt.dnd.race.Race
import com.pineypiney.mtt.dnd.race.Subrace
import com.pineypiney.mtt.dnd.server.ServerDNDEngine
import com.pineypiney.mtt.dnd.spells.Spell
import com.pineypiney.mtt.dnd.traits.Abilities
import com.pineypiney.mtt.util.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import java.util.*
import java.util.function.Supplier
import kotlin.random.Random

class Prefab(
	val name: Supplier<String>,
	val race: Race,
	val subrace: Subrace?,
	val modelID: String,
	val dndClass: DNDClass,
	val abilities: Abilities,
	val inventory: Set<Supplier<Pair<Int, ItemStack>>>,
	val spells: Set<Spell>
) {

	fun createCharacter(level: Int, engine: DNDEngine): SimpleCharacter {
		var maxHealth = dndClass.healthDie
		repeat(level - 1) { maxHealth += Random.nextInt(dndClass.healthDie) }
		val params = SimpleCharacter.Params(name.get(), race, subrace, modelID, dndClass, level, maxHealth, spells)
		params.abilities.setValues(abilities)
		val char = SimpleCharacter(params, UUID.randomUUID(), engine)
		for (item in inventory) {
			val (slot, stack) = item.get()
			if (slot == -1) char.inventory.insertStack(-1, stack)
			else char.inventory.setStack(slot, stack)
		}

		return char
	}

	companion object {
		fun fromJson(json: JsonObject, engine: ServerDNDEngine): Prefab {
			val name: Supplier<String> = when (val nameJson = json["name"]) {
				is JsonObject -> {
					(if (nameJson.containsKey("generator")) {
						val generator = engine.nameGenerators[MTT.identifier(nameJson.string("generator"))]
						val e = if (generator != null) Supplier(generator::generate) else Supplier { "" }
						e
					} else Supplier { "" })
				}

				is JsonPrimitive -> Supplier { nameJson.content }
				else -> Supplier { "" }
			}
			val race = Race.findById(json.string("race", "human"))
			val subrace = race.getSubrace(json.string("subrace"))
			val classId = json.string("class")
			val dndClass = DNDClass.classes.findOrError("No DND Class with ID $classId") { it.id == classId }
			val abilities = Abilities()
			abilities.setValues(json.array("abilities").map { it.jsonPrimitive.int }.toIntArray())

			val inventory = mutableSetOf<Supplier<Pair<Int, ItemStack>>>()
			for (obj in json.array("inventory")) {
				when (obj) {
					is JsonObject -> {
						val itemStr = obj.stringOrNull("item") ?: continue
						val amount = obj.int("amount", 1)
						addInventorySupplier(inventory, itemStr, amount, -1)
					}

					is JsonPrimitive -> {
						val itemStr = obj.content
						addInventorySupplier(inventory, itemStr, 1, -1)
					}

					else -> continue
				}
			}

			val equipmentJson = json.obj("equipment")
			addEquipment(inventory, equipmentJson, 0, "helmet")
			addEquipment(inventory, equipmentJson, 2, "cloak")
			addEquipment(inventory, equipmentJson, 4, "armour")
			addEquipment(inventory, equipmentJson, 6, "bracers")
			addEquipment(inventory, equipmentJson, 8, "boots")

			addEquipment(inventory, equipmentJson, 10, "melee_main")
			addEquipment(inventory, equipmentJson, 11, "melee_offhand")
			addEquipment(inventory, equipmentJson, 12, "range_main")
			addEquipment(inventory, equipmentJson, 13, "range_offhand")

			val spells = mutableSetOf<Spell>()
			if (json.containsKey("spells")) {
				for (spellJson in json.array("spells")) {
					if (spellJson is JsonObject) {
						val spell = Spell.findById(spellJson.string("spell"))
						spells.add(spell)
					}
				}
			}

			return Prefab(name, race, subrace, race.models.first().id, dndClass, abilities, inventory, spells)
		}

		private fun addEquipment(
			inventory: MutableSet<Supplier<Pair<Int, ItemStack>>>,
			equipmentJson: JsonObject,
			slot: Int,
			id: String
		) {
			addInventorySupplier(inventory, equipmentJson.string(id), 1, slot)
		}

		private fun addInventorySupplier(
			inventory: MutableSet<Supplier<Pair<Int, ItemStack>>>,
			itemStr: String,
			amount: Int = 1,
			slot: Int = -1
		) {
			val itemId = MTT.identifier(itemStr)
			val item = Registries.ITEM.get(itemId)
			if (item != Items.AIR) inventory.add(Supplier { slot to ItemStack(item, amount) })
		}
	}
}