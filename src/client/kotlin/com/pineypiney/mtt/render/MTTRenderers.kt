package com.pineypiney.mtt.render

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.entity.MTTEntities
import com.pineypiney.mtt.render.entity.DNDPlayerEntityRenderer
import com.pineypiney.mtt.render.entity.TestEntityRenderer
import com.pineypiney.mtt.render.entity.model.BipedModelData
import com.pineypiney.mtt.util.float
import com.pineypiney.mtt.util.int
import kotlinx.serialization.json.*
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.model.*
import net.minecraft.client.render.entity.model.EntityModelLayer
import net.minecraft.client.render.entity.model.EntityModelPartNames
import net.minecraft.util.Identifier

class MTTRenderers {

	companion object {

		val BIPED_MODELS = mutableMapOf<String, Pair<BipedModelData, EntityModelLayer>>()
		val EQUIPMENT_MODELS = Array(5) { mutableMapOf<String, EntityModelLayer>() }

		fun registerRenderers(){
			EntityRendererRegistry.register(MTTEntities.PLAYER){ ctx ->
				DNDPlayerEntityRenderer(ctx)
			}
			EntityRendererRegistry.register(MTTEntities.TEST){ ctx ->
				TestEntityRenderer(ctx)
			}
		}

		fun registerBipedModels() = registerAllModels(BIPED_MODELS, "entity", ::readEntityModelFromJson)
		fun registerEquipmentModels() {
			registerAllModels(EQUIPMENT_MODELS[0], "equipment/head") { name, json -> readEquipmentModelFromJson(name, json, "head") }
			registerAllModels(EQUIPMENT_MODELS[1], "equipment/neck") { name, json -> readEquipmentModelFromJson(name, json, "head", "body") }
			registerAllModels(EQUIPMENT_MODELS[2], "equipment/body") { name, json -> readEquipmentModelFromJson(name, json, "body", "left_arm", "right_arm") }
			registerAllModels(EQUIPMENT_MODELS[3], "equipment/arms") { name, json -> readEquipmentModelFromJson(name, json, "left_arm", "right_arm") }
			registerAllModels(EQUIPMENT_MODELS[4], "equipment/feet") { name, json -> readEquipmentModelFromJson(name, json, "left_leg", "right_leg") }
		}

		fun <V> registerAllModels(map: MutableMap<String, V>, file: String, read: (String, JsonObject) -> V){
			val modelFiles = MinecraftClient.getInstance().resourceManager.findResources("models/$file"){ it.namespace == "mtt" && it.path.endsWith(".json")}
			for ((id, stream) in modelFiles){
				try{
					val name = id.path.removePrefix("models/$file/").removeSuffix(".json")
					map[name] = read(name, Json.parseToJsonElement(stream.reader.readText()).jsonObject)
				}
				catch (e: Exception){
					MTT.logger.warn("Failed to load $file model $id")
					e.printStackTrace()
				}
			}
			MTT.logger.info("Successfully loaded ${map.size} $file models: [${map.keys.joinToString()}]")
		}

		fun readEntityModelFromJson(name: String, json: JsonObject): Pair<BipedModelData, EntityModelLayer>{

			val layer = EntityModelLayer(Identifier.of(MTT.MOD_ID, "${name}_biped"), "main")

			EntityModelLayerRegistry.registerModelLayer(layer) {
				val data = ModelData()
				val root = data.root
				val textureSize = json["textureSize"]?.jsonArray
				addBipedModelPart(json, EntityModelPartNames.HEAD, root)
				addBipedModelPart(json, EntityModelPartNames.BODY, root)
				addBipedModelPart(json, EntityModelPartNames.LEFT_LEG, root)
				addBipedModelPart(json, EntityModelPartNames.RIGHT_LEG, root)
				addBipedModelPart(json, EntityModelPartNames.LEFT_ARM, root)
				addBipedModelPart(json, EntityModelPartNames.RIGHT_ARM, root)
				TexturedModelData.of(data, textureSize?.int(0) ?: 64, textureSize?.int(1) ?: 32)
			}

			val bipedData = BipedModelData(
				maxOf(getSize(json["right_leg"], 1), getSize(json["left_leg"], 1)),
				getSize(json["body"], 1),
				getOrigin(json["head"], 1),
				getSize(json["head"], 1),
				getSize(json["head"], 0)
			)
			return bipedData to layer
		}

		fun addBipedModelPart(json: JsonObject, name: String, root: ModelPartData){
			if(!json.contains(name)) throw Exception(name)
			addModelPart(name, json[name]!!.jsonObject, root)
		}

		fun readEquipmentModelFromJson(name: String, json: JsonObject, vararg parts: String): EntityModelLayer {
			val layer = EntityModelLayer(Identifier.of(MTT.MOD_ID, name), "main")
			val textureSize = json["textureSize"]!!.jsonArray
			EntityModelLayerRegistry.registerModelLayer(layer) {
				val data = ModelData()
				val root = data.root
				for(part in parts){
					val partJson = json[part]?.jsonObject ?: continue
					val cubes = partJson["cubes"]?.jsonArray ?: continue
					val pivotJson = partJson["pivot"]?.jsonArray
					val builder = ModelPartBuilder.create()
					for(cuboidJson in cubes.filterIsInstance<JsonObject>()) addCuboid(builder, cuboidJson)
					val pivotVec = ModelTransform.origin(pivotJson?.float(0) ?: 0f, pivotJson?.float(1) ?: 0f, pivotJson?.float(2) ?: 0f)
					root.addChild(part, builder, pivotVec)
				}
				TexturedModelData.of(data, textureSize.int(0), textureSize.int(1))
			}
			return layer
		}

		fun addModelPart(name: String, partJson: JsonObject, root: ModelPartData){
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

		fun addCuboid(builder: ModelPartBuilder, partJson: JsonObject){
			val uv = partJson["uv"]!!.jsonArray
			val pos = partJson["pos"]!!.jsonArray
			val sizeJson = partJson["size"]!!.jsonArray
			val size = arrayOf(sizeJson.float(0), sizeJson.float(1), sizeJson.float(2))
			builder.uv(uv.int(0), uv.int(1))
			builder.cuboid(
				-(pos.float(0) + size[0]), -(pos.float(1) + size[1]), pos.float(2),
				size[0], size[1], size[2], Dilation(.1f)
			)
		}

		fun getOrigin(json: JsonElement?, axis: Int): Int{
			return json?.jsonObject["pos"]?.jsonArray?.int(axis) ?: 0
		}

		fun getSize(json: JsonElement?, axis: Int): Int{
			return json?.jsonObject["size"]?.jsonArray?.int(axis) ?: 0
		}
	}
}