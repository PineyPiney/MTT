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
import net.minecraft.util.math.MathHelper

class MTTRenderers {

	companion object {

		val BIPED_MODELS = mutableMapOf<String, Pair<BipedModelData, EntityModelLayer>>()
		val EQUIPMENT_MODELS = Array(5) { mutableMapOf<String, EntityModelLayer>() }

		fun registerRenderers(){
			EntityRendererRegistry.register(MTTEntities.PLAYER, ::DNDPlayerEntityRenderer)
			EntityRendererRegistry.register(MTTEntities.TEST, ::TestEntityRenderer)
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
				try {
					addBipedModelPart(json, EntityModelPartNames.HEAD, root)
					addBipedModelPart(json, EntityModelPartNames.BODY, root)
					addBipedModelPart(json, EntityModelPartNames.LEFT_LEG, root)
					addBipedModelPart(json, EntityModelPartNames.RIGHT_LEG, root)
					addBipedModelPart(json, EntityModelPartNames.LEFT_ARM, root)
					addBipedModelPart(json, EntityModelPartNames.RIGHT_ARM, root)
				}
				catch (e: Exception){
					MTT.logger.error("Failed to load entity model $name")
					throw e
				}
				TexturedModelData.of(data, textureSize?.int(0) ?: 64, textureSize?.int(1) ?: 32)
			}

			val bipedData = BipedModelData(
				maxOf(getSize(json, "right_leg", 1), getSize(json, "left_leg", 1)),
				getSize(json, "body", 1),
				getOrigin(json, "head", 1),
				getSize(json, "head", 1),
				getSize(json, "head", 0)
			)
			return bipedData to layer
		}

		fun addBipedModelPart(json: JsonObject, name: String, root: ModelPartData){
			val partJson = json[name]?.jsonObject ?: throw Exception(name)

			val pivotJson = partJson["pivot"]?.jsonArray
			val cubesJson = partJson["cubes"] ?: return
			val builder = ModelPartBuilder.create()
			val pivotVec = ModelTransform.origin(-(pivotJson?.float(0) ?: 0f), -(pivotJson?.float(1) ?: 0f), pivotJson?.float(2) ?: 0f)
			val rotated = mutableListOf<JsonObject>()
			when(cubesJson){
				is JsonObject -> addEntityCuboid(builder, cubesJson, pivotVec)
				is JsonArray -> cubesJson.filterIsInstance<JsonObject>().forEach { if(it.contains("rotation")) rotated.add(it) else addEntityCuboid(builder, it, pivotVec) }
				else -> {}
			}
			val part = root.addChild(name, builder, pivotVec)

			var i = 0
			for(rotatedCube in rotated){
				val rotatedBuilder = ModelPartBuilder.create()
				val rotatedPivotJson = rotatedCube["pivot"]?.jsonArray
				val rotationJson = rotatedCube["rotation"]!!.jsonArray
				val childPivot = ModelTransform.of(
					-(rotatedPivotJson?.float(0) ?: 0f) - pivotVec.x, -(rotatedPivotJson?.float(1) ?: 0f) - pivotVec.y, (rotatedPivotJson?.float(2) ?: 0f) - pivotVec.z,
					-rotationJson.float(0) * MathHelper.RADIANS_PER_DEGREE, -rotationJson.float(1) * MathHelper.RADIANS_PER_DEGREE,rotationJson.float(2) * MathHelper.RADIANS_PER_DEGREE
					//0f, 0f, 15f
				)
				addEntityChildCuboid(rotatedBuilder, rotatedCube, childPivot, pivotVec)
				part.addChild(rotatedCube["name"]?.jsonPrimitive?.content ?: "$name part ${i++}", rotatedBuilder, childPivot)
			}
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
					val pivotVec = ModelTransform.origin(pivotJson?.float(0) ?: 0f, pivotJson?.float(1) ?: 0f, pivotJson?.float(2) ?: 0f)
					for(cuboidJson in cubes.filterIsInstance<JsonObject>()) addEquipmentCuboid(builder, cuboidJson)
					root.addChild(part, builder, pivotVec)
				}
				TexturedModelData.of(data, textureSize.int(0), textureSize.int(1))
			}
			return layer
		}

		fun addEntityCuboid(builder: ModelPartBuilder, partJson: JsonObject, pivot: ModelTransform){
			val uv = partJson["uv"]!!.jsonArray
			val pos = partJson["pos"]!!.jsonArray
			val sizeJson = partJson["size"]!!.jsonArray
			val size = arrayOf(sizeJson.float(0), sizeJson.float(1), sizeJson.float(2))

			builder
				.uv(uv.int(0), uv.int(1))
				.cuboid(
					-pivot.x - (pos.float(0) + size[0]), -pivot.y - (pos.float(1) + size[1]), pos.float(2) - pivot.z,
					size[0], size[1], size[2]
				)
		}

		fun addEntityChildCuboid(builder: ModelPartBuilder, partJson: JsonObject, pivot: ModelTransform, parentPivot: ModelTransform){
			val uv = partJson["uv"]!!.jsonArray
			val pos = partJson["pos"]!!.jsonArray
			val sizeJson = partJson["size"]!!.jsonArray
			val size = arrayOf(sizeJson.float(0), sizeJson.float(1), sizeJson.float(2))

			builder
				.uv(uv.int(0), uv.int(1))
				.cuboid(														// -(-4)
					-(parentPivot.x + pivot.x) - (pos.float(0) + size[0]), -(parentPivot.y + pivot.y) - (pos.float(1) + size[1]), pos.float(2) - (parentPivot.z + pivot.z),
					size[0], size[1], size[2]
				)
		}

		fun addEquipmentCuboid(builder: ModelPartBuilder, partJson: JsonObject){
			val uv = partJson["uv"]!!.jsonArray
			val pos = partJson["pos"]!!.jsonArray
			val sizeJson = partJson["size"]!!.jsonArray
			val size = arrayOf(sizeJson.float(0), sizeJson.float(1), sizeJson.float(2))
			builder
				.uv(uv.int(0), uv.int(1))
				.cuboid(
					-(pos.float(0) + size[0]), -(pos.float(1) + size[1]), pos.float(2),
					size[0], size[1], size[2], Dilation(.9f)
				)
		}

		fun getMainCube(json: JsonObject, part: String): JsonObject{
			val cubesJson = json[part]!!.jsonObject["cubes"]!!
			return when(cubesJson){
				is JsonObject -> cubesJson
				is JsonArray -> cubesJson.filterIsInstance<JsonObject>().firstOrNull { it["name"]?.jsonPrimitive?.content == part } ?:
				throw Exception("Part $part has multiple cubes, one should be named $part")
				is JsonPrimitive -> throw Exception("Cubes should be array or object")
			}
		}

		fun getOrigin(json: JsonObject, part: String, axis: Int): Int{
			return getMainCube(json, part)["pos"]?.jsonArray?.int(axis) ?: 0
		}

		fun getSize(json: JsonObject, part: String, axis: Int): Int{
			return getMainCube(json, part)["size"]?.jsonArray?.int(axis) ?: 0
		}
	}
}