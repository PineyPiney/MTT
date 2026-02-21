package com.pineypiney.mtt.client.gui

import com.pineypiney.mtt.client.MTTClient
import com.pineypiney.mtt.item.dnd.DNDGameItem
import com.pineypiney.mtt.item.dnd.equipment.DNDMeleeItem
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class MTTTooltips {

	companion object {
		fun registerTooltips(){
			ItemTooltipCallback.EVENT.register { stack, ctx, type, list ->
				val item = stack.item
				if (item !is DNDGameItem) return@register
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

				val rarity = DNDGameItem.getRarity(stack)
				list.add(Text.translatable("item.mtt.tooltip.rarity").append(": ").append(Text.translatable("item.mtt.tooltip.${rarity.lowercase()}").withColor(rarity.colour)))

				val gold = DNDGameItem.getValue(stack)
				val weight = DNDGameItem.getWeight(stack)
				list.add(Text.literal(gold.toString()).withColor(Formatting.GOLD.colorValue!!))
				list.add(Text.literal("$weight lb."))
			}
		}
	}
}