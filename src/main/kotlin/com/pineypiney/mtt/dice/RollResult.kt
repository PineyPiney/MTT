package com.pineypiney.mtt.dice

import com.pineypiney.mtt.network.codec.BoolByte
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import net.minecraft.network.codec.PacketCodec

class RollResult(val die: Die, val value: Int, val crit: Boolean, val critFail: Boolean) : Comparable<Int> {

	operator fun plus(int: Int) = RollResult(die, value + int, crit, critFail)
	operator fun minus(int: Int) = RollResult(die, value - int, crit, critFail)
	override fun compareTo(other: Int): Int {
		return value.compareTo(other)
	}

	companion object {
		val CODEC = PacketCodec.tuple(
			Die.CODEC, RollResult::die,
			MTTPacketCodecs.uBytInt, RollResult::value,
			BoolByte.CODEC, { BoolByte(it.crit, it.critFail) }
		) { die, value, bools -> RollResult(die, value, bools[0], bools[1]) }
	}
}