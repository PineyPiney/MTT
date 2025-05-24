package com.pineypiney.mtt.gui

import com.pineypiney.mtt.MTTClient
import com.pineypiney.mtt.item.dnd.DNDItem
import com.pineypiney.mtt.item.dnd.equipment.DNDMeleeItem
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class MTTTooltips {

	companion object {
		fun registerTooltips(){
			ItemTooltipCallback.EVENT.register { stack, ctx, type, list ->
				val item = stack.item
				if(item !is DNDItem) return@register
				val character = MTTClient.getClientCharacter() ?: return@register

				if(item is DNDMeleeItem) {
					val hitBonus = character.getAttackBonus(item.weaponType, stack)
					if(hitBonus > 0) list.add(Text.translatable("item.mtt.tooltip.hit_bonus").append(": $hitBonus"))

					val damageBonus = character.getDamageBonus(item.weaponType, stack)
					val damageRoll = StringBuilder(": ${item.weaponType.numDice}d${item.weaponType.sides} ")
					if(item.weaponType.versatile) damageRoll.append("(${item.weaponType.numDice}d${item.weaponType.sides + 2}) ")
					if(damageBonus > 0) damageRoll.append("+ $damageBonus ")
					list.add(Text.translatable("item.mtt.tooltip.damage").append(damageRoll.toString()).append(Text.translatable("mtt.damage_type.${item.weaponType.damageType.id}")))

					if(item.weaponType.nearDistance > 0f) list.add(Text.translatable("item.mtt.property.thrown").append(": (${item.weaponType.nearDistance}/${item.weaponType.farDistance})"))
				}

				val rarity = DNDItem.getRarity(stack)
				list.add(Text.translatable("item.mtt.tooltip.rarity").append(": ").append(Text.translatable("item.mtt.tooltip.${rarity.lowercase()}").withColor(rarity.colour)))

				val gold = DNDItem.getValue(stack)
				val weight = DNDItem.getWeight(stack)
				list.add(Text.literal("$gold gp").withColor(Formatting.GOLD.colorValue!!))
				list.add(Text.literal("$weight lb."))
			}
		}
	}
}