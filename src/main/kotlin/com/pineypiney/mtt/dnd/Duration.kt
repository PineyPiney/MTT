package com.pineypiney.mtt.dnd

import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import java.util.*

sealed class Duration : Comparable<Duration> {
	abstract fun getID(): Int

	open fun passSeconds(seconds: Int) {}
	open fun turnTaken(character: Character) {}
	open fun shortRest() {}
	open fun longRest() {}
	abstract fun passed(): Boolean


	class Time(var secondsLeft: Int) : Duration() {
		override fun getID(): Int = 0
		override fun passSeconds(seconds: Int) {
			secondsLeft -= seconds
		}

		override fun shortRest() {
			secondsLeft -= 3600
		}

		override fun longRest() {
			secondsLeft -= 28800
		}

		override fun passed(): Boolean = secondsLeft <= 0
		override fun compareTo(other: Duration): Int {
			return when (other) {
				is Time -> secondsLeft - other.secondsLeft
				is Turns -> secondsLeft - (other.turnsLeft * 6)
				else -> -1
			}
		}

		override fun toString(): String = "$secondsLeft Seconds"

		companion object {
			val PACKET_CODEC = PacketCodecs.INTEGER.xmap(::Time, Time::secondsLeft)
		}
	}

	class Turns(var character: UUID, var turnsLeft: Int) : Duration() {
		override fun getID(): Int = 1
		override fun turnTaken(character: Character) {
			if (character.uuid == this.character) turnsLeft--
		}

		override fun passed(): Boolean = turnsLeft <= 0
		override fun compareTo(other: Duration): Int {
			return when (other) {
				is Turns -> turnsLeft - other.turnsLeft
				is Time -> (turnsLeft * 6) - other.secondsLeft
				else -> -1
			}
		}

		override fun toString(): String = "$turnsLeft Turns"

		companion object {
			val PACKET_CODEC = PacketCodec.tuple(
				MTTPacketCodecs.UUID_CODEC, Turns::character,
				MTTPacketCodecs.bytInt, Turns::turnsLeft,
				::Turns
			)
		}

	}

	class ShortRest(var rested: Boolean) : Duration() {
		override fun getID(): Int = 2
		override fun shortRest() {
			rested = true
		}

		override fun longRest() {
			rested = true
		}

		override fun passed(): Boolean = rested
		override fun compareTo(other: Duration): Int {
			return when (other) {
				is Time, Turns -> 1
				is ShortRest -> 0
				else -> -1
			}
		}

		override fun toString(): String = "Until Short Rest"

		companion object {
			val PACKET_CODEC = PacketCodecs.BOOLEAN.xmap(::ShortRest, ShortRest::rested)
		}
	}

	class LongRest(var rested: Boolean) : Duration() {
		override fun getID(): Int = 3
		override fun longRest() {
			rested = true
		}

		override fun passed(): Boolean = rested
		override fun compareTo(other: Duration): Int {
			return when (other) {
				is Time, Turns, ShortRest -> 1
				else -> 0
			}
		}

		override fun toString(): String = "Until Long Rest"

		companion object {
			val PACKET_CODEC = PacketCodecs.BOOLEAN.xmap(::LongRest, LongRest::rested)
		}
	}
}