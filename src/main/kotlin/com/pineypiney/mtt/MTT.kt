package com.pineypiney.mtt

import com.pineypiney.mtt.commands.MTTCommands
import com.pineypiney.mtt.component.MTTComponents
import com.pineypiney.mtt.entity.MTTEntities
import com.pineypiney.mtt.item.dnd.DNDItemGroups
import com.pineypiney.mtt.item.dnd.DNDItems
import com.pineypiney.mtt.network.MTTNetwork
import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MTT : ModInitializer {

	override fun onInitialize() {
		MTTCommands.registerCommands()
		MTTNetwork.registerPayloads()
		MTTComponents
		DNDItems
		DNDItemGroups.registerItemGroups()
		MTTEntities.registerEntities()
	}

	companion object {
		const val MOD_ID = "mtt"
		val logger: Logger = LoggerFactory.getLogger("Minecraft Table Top")

		fun identifier(path: String): Identifier {
			return if (path.contains(':')) Identifier.of(path) else Identifier.of(MOD_ID, path)
		}

		fun identifierString(id: Identifier): String {
			return if (id.namespace == MOD_ID) id.path else id.toString()
		}
	}
}
