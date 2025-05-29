package com.pineypiney.mtt.dnd.traits.features

import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.traits.Ability
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodecs

open class Feature(val id: String) {
	open fun onCreateActions(sheet: CharacterSheet){}
	open fun onShortRest(sheet: CharacterSheet){}
	open fun onLongRest(sheet: CharacterSheet){}
	open fun onSavingThrow(sheet: CharacterSheet, ability: Ability, type: String, initialRoll: Int): Int = initialRoll
	open fun onAttackRoll(sheet: CharacterSheet, roll: Int, target: Int){}

	open fun modifyAbility(sheet: CharacterSheet, ability: Ability, initialAbility: Int): Int = initialAbility
	open fun modifyArmour(sheet: CharacterSheet, initialArmourClass: Int): Int = initialArmourClass

	open fun encode(buf: ByteBuf){
		PacketCodecs.STRING.encode(buf, id)
	}
}