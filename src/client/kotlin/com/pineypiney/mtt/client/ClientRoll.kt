package com.pineypiney.mtt.client

import com.pineypiney.mtt.dice.RollResult

data class ClientRoll(val id: Int, val rolls: List<RollResult>, val success: Int) {
	var endTime = -1L
}