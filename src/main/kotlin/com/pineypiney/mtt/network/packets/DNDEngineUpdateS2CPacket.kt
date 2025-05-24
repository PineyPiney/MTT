package com.pineypiney.mtt.network.packets

import net.minecraft.network.NetworkSide
import net.minecraft.network.listener.ClientCommonPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.PacketType
import net.minecraft.util.Identifier

class DNDEngineUpdateS2CPacket(val int: Int) : Packet<ClientCommonPacketListener>  {

	override fun getPacketType(): PacketType<DNDEngineUpdateS2CPacket> = PACKET_TYPE

	override fun apply(listener: ClientCommonPacketListener?) {
		println("Int is $int\nListener is $listener")
	}

	companion object {
		val PACKET_TYPE: PacketType<DNDEngineUpdateS2CPacket> = PacketType(NetworkSide.CLIENTBOUND, Identifier.of("mtt", "engine"))
	}
}