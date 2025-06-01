package com.pineypiney.mtt.item.dnd

import com.mojang.datafixers.util.Function3
import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.component.MTTComponents
import com.pineypiney.mtt.dnd.traits.Rarity
import com.pineypiney.mtt.dnd.traits.proficiencies.ArmourType
import com.pineypiney.mtt.dnd.traits.proficiencies.WeaponType
import com.pineypiney.mtt.item.dnd.equipment.*
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.DyedColorComponent
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier


class DNDItems {

	companion object {

		// https://roll20.net/compendium/dnd5e/Weapons#toc_3
		val DAGGER = registerWeapon("dagger", WeaponType.DAGGER, 2, 1f, ::DNDMeleeItem)
		val SHORT_SWORD = registerWeapon("short_sword", WeaponType.SHORT_SWORD, 10, 2f, ::DNDMeleeItem)
		val LONG_SWORD = registerWeapon("long_sword", WeaponType.LONG_SWORD, 15, 3f, ::DNDMeleeItem)
		val GREAT_SWORD = registerWeapon("great_sword", WeaponType.GREAT_SWORD, 50, 6f, ::DNDMeleeItem)
		val GREAT_CLUB = registerWeapon("great_club", WeaponType.GREAT_CLUB, 1, 10f, ::DNDMeleeItem)

		val SHORTBOW = registerWeapon("shortbow", WeaponType.SHORTBOW, 25, 2f, ::DNDRangedItem)
		val LONGBOW = registerWeapon("longbow", WeaponType.LONGBOW, 50, 2f, ::DNDRangedItem)
		val LIGHT_CROSSBOW = registerWeapon("light_crossbow", WeaponType.LIGHT_CROSSBOW, 25, 5f, ::DNDRangedItem)
		val HEAVY_CROSSBOW = registerWeapon("heavy_crossbow", WeaponType.HEAVY_CROSSBOW, 50, 18f, ::DNDRangedItem)

		// https://roll20.net/compendium/dnd5e/Armor#content
		val LEATHER_ARMOUR = registerArmour("leather_armour", 10, 10f, "armour", "leather_armour", 11, ArmourType.LIGHT, false, Item.Settings().component(DataComponentTypes.DYED_COLOR, DyedColorComponent(-6265536)))
		val SCALE_MAIL = registerArmour("scale_mail", 50, 45f, "armour", "scale_mail", 14, ArmourType.MEDIUM, true, Item.Settings())
		val SPLINT = registerArmour("splint", 200, 60f, "armour", "splint", 17, ArmourType.HEAVY, true, Item.Settings())

		val STEEL_HELMET = registerVisibleAccessory("steel_helmet", 25, 3f, "horned_helmet", "steel_helmet", DNDEquipmentType.HELMET, Item.Settings().component(MTTComponents.ARMOUR_CLASS_BONUS_TYPE, 1))
		val SHIELD = register("shield"){ s -> DNDShieldItem(s, 10, 6f, 2) }

		@Suppress("UNCHECKED_CAST")
		fun <E: DNDItem> register(mishapartyy: String, settings: Item.Settings = Item.Settings(), factory: (Item.Settings) -> E): E{
			return Items.register(
				RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MTT.MOD_ID, mishapartyy)),
				{ s -> factory(s) },
				settings
			) as E
		}

		fun register(path: String, factory: Function3<Item.Settings, Float, Float, Item>, value: Float, weight: Float, settings: Item.Settings): Item {
			return Items.register(
				RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MTT.MOD_ID, path)),
				{ s -> factory.apply(s, value, weight) },
				settings
			)
		}

		private fun registerArmour(path: String, value: Int, weight: Float, model: String, texture: String, armourClass: Int, armourType: ArmourType, stealthDisadvantage: Boolean, settings: Item.Settings, rarity: Rarity = Rarity.COMMON): DNDArmourItem {
			return Items.register(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MTT.MOD_ID, path)), { s ->
				DNDArmourItem(s, value, weight, model, texture, armourClass, armourType, stealthDisadvantage, rarity)
			}, settings.maxCount(1)) as DNDArmourItem
		}

		private fun registerVisibleAccessory(path: String, value: Int, weight: Float, model: String, texture: String, type: DNDEquipmentType, settings: Item.Settings, rarity: Rarity = Rarity.COMMON): VisibleAccessoryItem {
			return Items.register(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MTT.MOD_ID, path)), { s ->
				VisibleAccessoryItem(s, value, weight, type, model, texture, rarity)
			}, settings.maxCount(1)) as VisibleAccessoryItem
		}

		private fun registerAccessory(path: String, value: Int, weight: Float, type: DNDEquipmentType, settings: Item.Settings, rarity: Rarity = Rarity.COMMON): DNDAccessoryItem {
			return Items.register(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MTT.MOD_ID, path)), { s ->
				DNDAccessoryItem(s, value, weight, type, rarity)
			}, settings.maxCount(1)) as DNDAccessoryItem
		}

		@Suppress("UNCHECKED_CAST")
		private fun <E: DNDWeaponItem> registerWeapon(path: String, type: WeaponType, value: Int, weight: Float, factory: (Item.Settings, WeaponType, Int, Float, Rarity) -> E, settings: Item.Settings = Item.Settings(), rarity: Rarity = Rarity.COMMON): E{
			return Items.register(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MTT.MOD_ID, path)), { s -> factory(s, type, value, weight, rarity) }, settings.maxCount(1)) as E
		}
	}
}