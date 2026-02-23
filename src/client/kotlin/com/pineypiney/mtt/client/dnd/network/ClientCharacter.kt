package com.pineypiney.mtt.client.dnd.network

import com.pineypiney.mtt.client.dnd.ClientDNDEngine
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.dnd.characters.CharacterDetails
import java.util.*

class ClientCharacter(override val details: CharacterDetails, uuid: UUID, override val engine: ClientDNDEngine) : Character(uuid)