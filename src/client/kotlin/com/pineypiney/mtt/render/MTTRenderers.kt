package com.pineypiney.mtt.render

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.entity.MTTEntities
import com.pineypiney.mtt.render.entity.DNDPlayerEntityRenderer
import com.pineypiney.mtt.render.entity.TestEntityRenderer
import com.pineypiney.mtt.render.entity.model.DNDPlayerEntityModel
import com.pineypiney.mtt.util.float
import com.pineypiney.mtt.util.int
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.float
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.model.ModelData
import net.minecraft.client.model.ModelPartBuilder
import net.minecraft.client.model.ModelPartData
import net.minecraft.client.model.ModelTransform
import net.minecraft.client.model.TexturedModelData
import net.minecraft.client.render.entity.model.EntityModelLayer
import net.minecraft.client.render.entity.model.EntityModelPartNames
import net.minecraft.util.Identifier

class MTTRenderers {

	companion object {

		val MODEL_DND_PLAYER_LAYER = EntityModelLayer(Identifier.of(MTT.MOD_ID, "dnd_player"), "main")

		val BIPED_MODELS = mutableMapOf<String, EntityModelLayer>()

		fun registerRenderers(){
			EntityRendererRegistry.register(MTTEntities.PLAYER){ ctx ->
				DNDPlayerEntityRenderer(ctx)
			}
			EntityRendererRegistry.register(MTTEntities.TEST){ ctx ->
				TestEntityRenderer(ctx)
			}

			EntityModelLayerRegistry.registerModelLayer(MODEL_DND_PLAYER_LAYER, DNDPlayerEntityModel::getTexturedModelData)
		}

		fun registerAllBipedModels(){
			val modelFiles = MinecraftClient.getInstance().resourceManager.findResources("models/entity"){ it.namespace == "mtt" && it.path.endsWith(".json")}
			for ((id, stream) in modelFiles){
				try{
					val name = id.path.removePrefix("models/entity/").removeSuffix(".json")
					BIPED_MODELS[name] = readModelFromJson(name, Json.parseToJsonElement(stream.reader.readText()).jsonObject)
				}
				catch (e: Exception){

				}
			}
			MTT.logger.info("Successfully loaded ${BIPED_MODELS.size} entity models: [${BIPED_MODELS.keys.joinToString()}]")
		}

		fun readModelFromJson(name: String, json: JsonObject): EntityModelLayer{

			val layer = EntityModelLayer(Identifier.of(MTT.MOD_ID, "${name}_biped"), "main")

			EntityModelLayerRegistry.registerModelLayer(layer) {
				val data = ModelData()
				val root = data.root
				addBipedModelPart(json, EntityModelPartNames.HEAD, root)
				addBipedModelPart(json, EntityModelPartNames.BODY, root)
				addBipedModelPart(json, EntityModelPartNames.LEFT_LEG, root)
				addBipedModelPart(json, EntityModelPartNames.RIGHT_LEG, root)
				addBipedModelPart(json, EntityModelPartNames.LEFT_ARM, root)
				addBipedModelPart(json, EntityModelPartNames.RIGHT_ARM, root)
				TexturedModelData.of(data, 64, 32)
			}

			return layer
		}

		fun addBipedModelPart(json: JsonObject, name: String, root: ModelPartData){
			if(!json.contains(name)) throw Exception(name)
			val partJson = json[name]!!.jsonObject
			val uv = partJson["uv"]!!.jsonArray
			val pos = partJson["pos"]!!.jsonArray
			val sizeJson = partJson["size"]!!.jsonArray
			val pivotJson = partJson["pivot"]!!.jsonArray
			val pivot = arrayOf(pivotJson.float(0), pivotJson.float(1), pivotJson.float(2))
			val size = arrayOf(sizeJson.float(0), sizeJson.float(1), sizeJson.float(2))


			root.addChild(name,
				ModelPartBuilder.create().uv(uv.int(0), uv.int(1)).cuboid(pivot[0] - (pos.float(0) + size[0]), pivot[1] - (pos.float(1) + size[1]), pos.float(2) - pivot[2], size[0], size[1], size[2]),
				ModelTransform.origin(-pivot[0], -pivot[1], pivot[2]))
		}
	}
}