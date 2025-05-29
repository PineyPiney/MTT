package com.pineypiney.mtt.item.dnd.equipment

import com.pineypiney.mtt.dnd.traits.Rarity
import com.pineypiney.mtt.dnd.traits.proficiencies.WeaponType
import com.pineypiney.mtt.entity.DNDEntity
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.MergedComponentMap
import net.minecraft.component.type.LoreComponent
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import kotlin.math.max

open class DNDMeleeItem(settings: Settings, override val weaponType: WeaponType, override val value: Int, override val weight: Float, override val rarity: Rarity = Rarity.COMMON) : DNDWeaponItem(settings) {

	override val type: DNDEquipmentType = DNDEquipmentType.MELEE_WEAPON

	override fun addToCharacter(entity: DNDEntity, stack: ItemStack) {
		val components = (stack.components as? MergedComponentMap) ?: return
		val lore = mutableListOf<Text>(Text.translatable("mtt.item_lore.damage").append(": "))

		val bonus = if(weaponType.finesse) max(entity.character.abilities.strMod, entity.character.abilities.dexMod) else entity.character.abilities.strMod
		val details = StringBuilder(": ${weaponType.numDice}d${weaponType.sides}")
		if(bonus > 0) details.append(" + $bonus")
		lore.add(Text.translatable("mtt.damage_type.${weaponType.damageType.id}").append(details.toString()))


		components[DataComponentTypes.LORE] = LoreComponent(lore)
	}
}