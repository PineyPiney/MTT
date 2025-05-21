package com.pineypiney.mtt.item.dnd

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.item.dnd.equipment.DNDArmourItem
import com.pineypiney.mtt.item.dnd.equipment.DNDWeaponItem
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class DNDItemGroups {

	companion object {
		val MELEE_WEAPON_GROUP = FabricItemGroup.builder()
			.icon { ItemStack(Items.IRON_SWORD) }
			.displayName(Text.translatable("itemGroup.mtt.melee_weapons"))
			.entries { ctx, entries ->
				DNDWeaponItem.addTo(DNDItems.DAGGER, entries, 600, 2700, 10000)
				DNDWeaponItem.addTo(DNDItems.SHORT_SWORD, entries, 800, 3000, 12000)
				DNDWeaponItem.addTo(DNDItems.LONG_SWORD, entries, 1000, 3600, 14000)
				DNDWeaponItem.addTo(DNDItems.GREAT_SWORD, entries, 1500, 5000, 18000)
				DNDWeaponItem.addTo(DNDItems.GREAT_CLUB, entries, 400, 2000, 9000)
			}
			.build()
		val RANGED_WEAPON_GROUP = FabricItemGroup.builder()
			.icon { ItemStack(Items.BOW) }
			.displayName(Text.translatable("itemGroup.mtt.ranged_weapons"))
			.entries { ctx, entries ->
				DNDWeaponItem.addTo(DNDItems.SHORTBOW, entries, 1200, 4200, 15000)
				DNDWeaponItem.addTo(DNDItems.LONGBOW, entries, 1500, 5000, 18000)
				DNDWeaponItem.addTo(DNDItems.LIGHT_CROSSBOW, entries, 1200, 4200, 15000)
				DNDWeaponItem.addTo(DNDItems.HEAVY_CROSSBOW, entries, 1500, 5000, 18000)
			}
			.build()

		val ARMOUR_GROUP = FabricItemGroup.builder()
			.icon { ItemStack(Items.IRON_CHESTPLATE) }
			.displayName(Text.translatable("itemGroup.mtt.armour"))
			.entries { ctx, entries ->
				DNDArmourItem.addTo(DNDItems.LEATHER_ARMOUR, entries, 800, 3000, 12000, 1)
				DNDArmourItem.addTo(DNDItems.SCALE_MAIL, entries, 1500, 5000, 18000, 1)
				DNDArmourItem.addTo(DNDItems.SPLINT, entries, 	3600, 10000, 21000, 1)

				entries.add(DNDItems.IRON_HELMET)

				DNDArmourItem.addTo(DNDItems.SHIELD, entries, 800, 3000, 12000, 0)
			}
			.build()

		fun registerItemGroups(){
			Registry.register(Registries.ITEM_GROUP, Identifier.of(MTT.MOD_ID, "melee_weapons_group"), MELEE_WEAPON_GROUP)
			Registry.register(Registries.ITEM_GROUP, Identifier.of(MTT.MOD_ID, "ranged_weapons_group"), RANGED_WEAPON_GROUP)
			Registry.register(Registries.ITEM_GROUP, Identifier.of(MTT.MOD_ID, "armour_group"), ARMOUR_GROUP)
		}
	}
}