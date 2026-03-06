package com.pineypiney.mtt.dice

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.network.codec.MTTPacketCodecs

enum class Die(val sides: Int, val width: Int, val height: Int, val textY: Int) {

	D2(2, 48, 48, 25),
	D4(4, 48, 48, 25),
	D6(6, 48, 48, 25),
	D8(8, 48, 48, 25),
	D10(10, 48, 48, 25),
	D12(12, 48, 48, 19),
	D20(20, 48, 48, 23),
	D100(100, 48, 48, 25);

	fun texture() = MTT.identifier("dice/${name.lowercase()}")

	companion object {
		val CODEC = MTTPacketCodecs.uBytInt.xmap({ Die.entries[it] }, Die::ordinal)
	}
}