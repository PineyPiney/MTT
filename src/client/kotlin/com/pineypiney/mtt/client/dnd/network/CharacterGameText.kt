package com.pineypiney.mtt.client.dnd.network

import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d

class CharacterGameText(val text: Text, var pos: Vec3d, var drift: Vec3d?, var lifetime: Int) {
	val lastRenderPos = pos.add(0.0)

	fun tick() {
		if (drift != null) {
			pos = pos.add(drift)
			drift = drift?.multiply(.95)
		}
		lifetime--
	}
}