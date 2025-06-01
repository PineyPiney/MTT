package com.pineypiney.mtt.item.dnd.equipment

import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.traits.Rarity
import com.pineypiney.mtt.dnd.traits.proficiencies.WeaponType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.consume.UseAction
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.world.World

class DNDRangedItem(settings: Settings, override val weaponType: WeaponType, override val value: Int, override val weight: Float, override val rarity: Rarity = Rarity.COMMON): DNDWeaponItem(settings) {
	override val type: DNDEquipmentType = DNDEquipmentType.RANGED_WEAPON

	override fun equip(character: Character) {

	}

	override fun onStoppedUsing(
		stack: ItemStack?,
		world: World?,
		user: LivingEntity?,
		remainingUseTicks: Int
	): Boolean {
		return super.onStoppedUsing(stack, world, user, remainingUseTicks)
	}

	override fun getMaxUseTime(stack: ItemStack, user: LivingEntity): Int {
		return 72000
	}

	override fun getUseAction(stack: ItemStack): UseAction {
		return UseAction.BOW
	}

	override fun use(world: World, user: PlayerEntity, hand: Hand): ActionResult {
		user.setCurrentHand(hand)
		return ActionResult.CONSUME
	}
}