package com.pineypiney.mtt.entity

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.network.codec.MTTPacketCodecs
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricTrackedDataRegistry
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.data.TrackedDataHandler
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import java.util.*

class MTTEntities {

	companion object {

		val UUID_TRACKER = TrackedDataHandler.create<UUID>(MTTPacketCodecs.UUID_CODEC)
		val OPTIONAL_UUID_TRACKER = TrackedDataHandler.create<Optional<UUID>>(PacketCodecs.optional(MTTPacketCodecs.UUID_CODEC))

		init {
			FabricTrackedDataRegistry.register(Identifier.of(MTT.MOD_ID, "optional_uuid"), OPTIONAL_UUID_TRACKER)
			FabricTrackedDataRegistry.register(Identifier.of(MTT.MOD_ID, "uuid"), UUID_TRACKER)
		}

		val PLAYER = Registry.register(Registries.ENTITY_TYPE, Identifier.of(MTT.MOD_ID, "player"),
			EntityType.Builder.create(::DNDPlayerEntity, SpawnGroup.MISC).dimensions(.6f, 1.8f)
				.build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MTT.MOD_ID, "player")))
		)

		val TEST: EntityType<TestEntity> = Registry.register(Registries.ENTITY_TYPE, Identifier.of(MTT.MOD_ID, "test"),
			EntityType.Builder.create(::TestEntity, SpawnGroup.MISC).dimensions(.6f, 1.8f)
				.build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MTT.MOD_ID, "test")))
		)

		fun registerEntities(){

			FabricDefaultAttributeRegistry.register(TEST, LivingEntity.createLivingAttributes())
		}
	}
}