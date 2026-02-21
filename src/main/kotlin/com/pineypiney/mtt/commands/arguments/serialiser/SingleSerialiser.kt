package com.pineypiney.mtt.commands.arguments.serialiser

import com.google.gson.JsonObject
import com.mojang.brigadier.arguments.ArgumentType
import io.netty.buffer.ByteBuf
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.serialize.ArgumentSerializer
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec

class SingleSerialiser<T, A : ArgumentType<*>>(val codec: PacketCodec<in ByteBuf, T>, val to: (T) -> A, val from: (A) -> T, val writeJson: JsonObject.(String, T) -> Unit) :
	ArgumentSerializer<A, SingleSerialiser<T, A>.Properties> {

	override fun writePacket(properties: Properties, buf: PacketByteBuf) {
		codec.encode(buf, properties.value)
	}

	override fun fromPacket(buf: PacketByteBuf): Properties {
		return Properties(codec.decode(buf))
	}

	override fun writeJson(properties: Properties, json: JsonObject) {
		json.writeJson("value", properties.value)
	}

	override fun getArgumentTypeProperties(argumentType: A): Properties {
		return Properties(from(argumentType))
	}

	inner class Properties(val value: T) : ArgumentSerializer.ArgumentTypeProperties<A> {
		override fun createType(commandRegistryAccess: CommandRegistryAccess): A {
			return this@SingleSerialiser.to(value)
		}

		override fun getSerializer(): ArgumentSerializer<A, Properties> {
			return this@SingleSerialiser
		}
	}
}