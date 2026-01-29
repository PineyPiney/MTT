package com.pineypiney.mtt.component

import com.mojang.serialization.Codec
import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.CoinValue
import net.minecraft.component.ComponentType
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

class MTTComponents {

	companion object {

		val VALUE_OVERRIDE_TYPE = register("value_override", Codec.LONG.xmap(::CoinValue, CoinValue::coppers))
		val WEIGHT_OVERRIDE_TYPE = register("weight_override", Codec.FLOAT)
		val RARITY_OVERRIDE_TYPE = register("rarity_override", Codec.INT)

		val DAMAGE_ROLL_TYPE = register("damage_rolls", DamageRolls.SINGLE_CODEC)
		val ARMOUR_CLASS_BONUS_TYPE = register("armour_class", Codec.INT)
		val HIT_BONUS_TYPE = register("hit_bonus", Codec.INT)
		val DAMAGE_BONUS_TYPE = register("damage_bonus", Codec.INT)
		val EXTRA_DAMAGE = register("extra_damage", DamageRolls.CODEC)

		private fun <E> register(type: String, codec: Codec<E>): ComponentType<E>{
			return Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MTT.MOD_ID, type), ComponentType.builder<E>().codec(codec).build())
		}
	}
}