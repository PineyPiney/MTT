package com.pineypiney.mtt.dnd.conditions

import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.traits.Source
import com.pineypiney.mtt.dnd.traits.SourceMap
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import com.pineypiney.mtt.network.payloads.s2c.CharacterConditionsS2CPayload
import io.netty.buffer.Unpooled
import net.minecraft.nbt.NbtCompound
import kotlin.jvm.optionals.getOrElse

class ConditionManager(val character: Character, val map: SourceMap<Condition.ConditionState<*>>) {

	fun apply(src: Source, condition: Condition.ConditionState<*>) {
		map.add(src, condition)
		updateConditions()
	}

	fun cleanse(condition: Condition<*>): Boolean {
		var removed = false
		for ((_, value) in map) removed = value.removeAll { it.condition == condition } || removed
		if (removed) updateConditions()
		return removed
	}

	fun clear() {
		map.clear()
		updateConditions()
	}

	fun <R> mapConditions(transform: (Condition.ConditionState<*>) -> R) = map.mapEntries(transform)

	fun forEachState(action: (Condition.ConditionState<*>) -> Unit) = map.forEachEntry(action)

	fun forEachCondition(action: (Condition<*>) -> Unit) {
		val conditions = mutableSetOf<Condition<*>>()
		map.forEachEntry { state ->
			if (!conditions.contains(state.condition)) {
				action(state.condition)
				conditions.add(state.condition)
			}
		}
	}

	private fun updateConditions() {
		if (!character.engine.isClient()) {
			val nbt = NbtCompound()
			writeNbt(nbt)
			character.engine.updates.add(CharacterConditionsS2CPayload(character.uuid, nbt))
		}
	}

	fun readNbt(nbt: NbtCompound) {
		val bytes = nbt.getByteArray("map").getOrElse { return }
		val buf = Unpooled.copiedBuffer(bytes)
		map.decode(buf, MTTPacketCodecs.smallCollection(MTTPacketCodecs.CONDITION, ::mutableSetOf))
	}

	fun writeNbt(nbt: NbtCompound) {
		val buf = Unpooled.buffer()
		map.encode(buf, MTTPacketCodecs.smallCollection(MTTPacketCodecs.CONDITION, ::mutableSetOf))
		if (buf.hasArray()) nbt.putByteArray("map", buf.array())
	}
}