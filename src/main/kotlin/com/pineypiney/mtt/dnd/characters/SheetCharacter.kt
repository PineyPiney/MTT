package com.pineypiney.mtt.dnd.characters

import com.pineypiney.mtt.component.MTTComponents
import com.pineypiney.mtt.dnd.CharacterSheet
import com.pineypiney.mtt.dnd.traits.Abilities
import com.pineypiney.mtt.dnd.traits.CreatureType
import com.pineypiney.mtt.dnd.traits.Size
import com.pineypiney.mtt.dnd.traits.proficiencies.WeaponType
import com.pineypiney.mtt.entity.DNDEntity
import com.pineypiney.mtt.entity.DNDPlayerEntity
import com.pineypiney.mtt.entity.MTTEntities
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtDouble
import net.minecraft.nbt.NbtList
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*
import kotlin.math.max

class SheetCharacter(val sheet: CharacterSheet, uuid: UUID) : Character(uuid) {
	override val name: String get() = sheet.name
	override val type: CreatureType get() = sheet.type
	override val size: Size get() = sheet.size
	override val speed: Int get() = sheet.speed
	override val model: String get() = sheet.model
	override var health: Int
		get() = sheet.health
		set(value) { sheet.health = value }
	override val maxHealth: Int get() = sheet.maxHealth
	override val abilities: Abilities get() = sheet.abilities
	override var baseArmourClass: Int
		get() = sheet.armourClass
		set(value) { sheet.armourClass = value }

	override fun createEntity(world: World): DNDEntity {
		return DNDPlayerEntity(MTTEntities.PLAYER, world, this)
	}

	fun getAttackBonus(weaponType: WeaponType, stack: ItemStack): Int{
		var i = if(weaponType.finesse) max(abilities.strMod, abilities.dexMod) else abilities.strMod
		if(sheet.isProficientIn(weaponType)) i += sheet.calculateProficiencyBonus()
		i += stack[MTTComponents.HIT_BONUS_TYPE] ?: 0
		return i
	}

	fun getDamageBonus(weaponType: WeaponType, stack: ItemStack): Int{
		var i = if(weaponType.finesse) max(abilities.strMod, abilities.dexMod) else abilities.strMod
		i += stack[MTTComponents.DAMAGE_BONUS_TYPE] ?: 0
		return i
	}

	override fun save(buf: RegistryByteBuf) {
		PacketCodecs.STRING.encode(buf, "sheet")
		MTTPacketCodecs.CHARACTER_SHEET_CODEC.encode(buf, sheet)
		MTTPacketCodecs.UUID_CODEC.encode(buf, uuid)
		val nbt = NbtCompound()

		val posNbt = NbtList()
		posNbt.add(NbtDouble.of(pos.x))
		posNbt.add(NbtDouble.of(pos.y))
		posNbt.add(NbtDouble.of(pos.z))
		nbt.put("pos", posNbt)

		nbt.putString("world", world.value.toString())
		nbt.put("inventory", inventory.writeNbt(buf.registryManager))
		buf.writeNbt(nbt)
	}

	companion object {
		fun load(buf: RegistryByteBuf): SheetCharacter{
			val character = SheetCharacter(MTTPacketCodecs.CHARACTER_SHEET_CODEC.decode(buf), MTTPacketCodecs.UUID_CODEC.decode(buf))
			val nbt = buf.readNbt() ?: return character

			val posArray = nbt.getListOrEmpty("pos")
			character.pos = Vec3d(
				posArray.getDouble(0, 0.0),
				posArray.getDouble(1, 0.0),
				posArray.getDouble(2, 0.0)
			)

			val worldID = Identifier.of(nbt.getString("world").get())
			character.world = RegistryKey.of(RegistryKeys.WORLD, worldID)
			character.inventory.readNbt(nbt.getListOrEmpty("inventory"), buf.registryManager)
			return character
		}
	}
}