package com.pineypiney.mtt.network.codec

import net.minecraft.network.codec.PacketCodecs
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.min

class BoolByte(val byte: Byte) {

	constructor(b1: Boolean, b2: Boolean) : this(compress(b1, b2))
	constructor(vararg bools: Boolean) : this(compress(*bools))

	operator fun get(i: Int) = (byte and (1 shl i).toByte()) > 0
	operator fun component1() = (byte and 1) > 0
	operator fun component2() = (byte and 2) > 0
	operator fun component3() = (byte and 4) > 0
	operator fun component4() = (byte and 8) > 0
	operator fun component5() = (byte and 16) > 0
	operator fun component6() = (byte and 32) > 0
	operator fun component7() = (byte and 64) > 0
	operator fun component8() = (byte and -127) > 0


	companion object {
		fun compress(vararg bools: Boolean): Byte {
			var b = 0.toByte()
			for (i in 0..<min(8, bools.size)) {
				if (bools[i]) b = b or (1 shl i).toByte()
			}
			return b
		}

		val CODEC = PacketCodecs.BYTE.xmap(::BoolByte, BoolByte::byte)
	}
}