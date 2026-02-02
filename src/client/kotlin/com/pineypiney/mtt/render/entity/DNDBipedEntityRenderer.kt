package com.pineypiney.mtt.render.entity

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.dnd.DNDClientEngine
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.entity.DNDEntity
import com.pineypiney.mtt.item.dnd.equipment.VisibleAccessoryItem
import com.pineypiney.mtt.render.MTTRenderers
import com.pineypiney.mtt.render.entity.model.BipedModelData
import com.pineypiney.mtt.render.entity.model.DNDBipedEntityModel
import com.pineypiney.mtt.render.entity.model.HelmetModel
import com.pineypiney.mtt.render.entity.state.DNDBipedEntityRenderState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.EntityPose
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RotationAxis
import java.io.File

abstract class DNDBipedEntityRenderer<E: DNDEntity, S: DNDBipedEntityRenderState, M: DNDBipedEntityModel<S>>(ctx: EntityRendererFactory.Context, modelMaker: (ModelPart) -> M): EntityRenderer<E, S>(ctx) {

	private val bipedModels = try {
		MTTRenderers.BIPED_MODELS.mapValues { (id, pair) ->
			try {
				pair.first to modelMaker(ctx.getPart(pair.second))
			} catch (e: IllegalArgumentException) {
				MTT.logger.warn("Failed to create model $id")
				throw e
			}
		}
	} catch (e: IllegalArgumentException) {
		emptyMap()
	}
	private val equipmentModels = MTTRenderers.EQUIPMENT_MODELS.mapNotNull { type ->
		type.mapValues { (name, layer) ->
			try {
				ctx.getPart(layer)
			} catch (_: IllegalArgumentException) {
		MTT.logger.warn("Could not find model for layer ${layer.name}")
		return@mapNotNull null
	} } }

	val allTextures = fetchAllTextures()

	override fun updateRenderState(entity: E, state: S, f: Float) {
		super.updateRenderState(entity, state, f)
		state.character = entity.character ?: return

		state.invisibleToPlayer = if(MinecraftClient.getInstance().options.perspective.isFirstPerson) {
			DNDClientEngine.getInstance().running && DNDClientEngine.getClientCharacterUUID() == state.character.uuid
		}
		else false

		val g = MathHelper.lerpAngleDegrees(f, entity.lastHeadYaw, entity.headYaw)
		state.bodyYaw = clampBodyYaw(entity, g, f)
		state.relativeHeadYaw = MathHelper.wrapDegrees(g - state.bodyYaw)
		state.pitch = entity.getLerpedPitch(f)
		state.customName = entity.customName

		state.limbSwingAnimationProgress = entity.limbAnimator.getAnimationProgress(f)
		state.limbSwingAmplitude = entity.limbAnimator.getAmplitude(f)
	}

	override fun render(state: S, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int) {

		if (!state.ready() || state.invisibleToPlayer) return

		var modelPath = state.character.race.id + '/' + state.character.model
		if(!bipedModels.contains(modelPath)) modelPath = bipedModels.keys.firstOrNull { it.startsWith(state.character.race.id + '/') } ?: return
		val (data, model: M) = bipedModels[modelPath]!!

		matrices.push()
		setupTransforms(state, matrices)
		matrices.scale(-1f, -1f, 1f)
		model.setAngles(state)
		val texture = Identifier.of(MTT.MOD_ID, "textures/entity/$modelPath/default.png")
		val vertexConsumer: VertexConsumer? = vertexConsumers.getBuffer(model.getLayer(texture))
		model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV)
		renderHelmet(state, data, matrices, vertexConsumers, light)
		matrices.pop()
		super.render(state, matrices, vertexConsumers, light)
	}

	protected open fun setupTransforms(state: S, matrices: MatrixStack) {
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - state.bodyYaw))

		if (state.deathTime > 0.0f) {
			var f = (state.deathTime - 1.0f) / 20.0f * 1.6f
			f = MathHelper.sqrt(f)
			if (f > 1.0f) {
				f = 1.0f
			}

			matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f * 90f))
		}
		else if (state.isInPose(EntityPose.SLEEPING)) {
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(state.bodyYaw))
			matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90f))
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(270.0f))
		}
	}

	private fun fetchAllTextures(): Map<String, Set<String>> {
		val textures = mutableMapOf<String, MutableSet<String>>()
		val resourcePacks = File("resourcepacks").listFiles() ?: return textures
		for(pack in resourcePacks){
			val raceDirs = File(pack, "assets/mtt/textures/entity").listFiles() ?: continue
			for(race in raceDirs){
				if(!race.isDirectory) continue
				val list = textures.getOrElse(race.name){ mutableSetOf<String>().apply { textures[race.name] = this } }
				for(file in race.listFiles()){
					if(file.extension == "png"){
						list.add(file.nameWithoutExtension)
					}
				}
			}
		}
		return textures
	}

	private fun renderHelmet(
		state: S,
		data: BipedModelData,
		matrices: MatrixStack,
		vertexConsumers: VertexConsumerProvider,
		light: Int
	) {
		val inventory = state.character.inventory

		// Try to render the aesthetic helmet, otherwise the functional helmet, otherwise none
		val renderedHelmet = inventory.getVisualHelmet()
		val item = renderedHelmet.item as? VisibleAccessoryItem ?: return
		val part = equipmentModels[0][item.model] ?: return
		val model = HelmetModel(part)

		val texture = Identifier.of(MTT.MOD_ID, "textures/equipment/${item.texture}.png")
		val consumer = vertexConsumers.getBuffer(model.getLayer(texture))

		matrices.push()
		val scale = data.headHeight * .125f
		matrices.scale(scale, scale, scale)
		matrices.translate(0f, .5f - ((data.headTop * .0625f) / scale), 0f)
		model.setAngles(state)
		model.render(matrices, consumer, light, OverlayTexture.DEFAULT_UV)
		matrices.pop()
	}


	private fun clampBodyYaw(entity: E, degrees: Float, tickProgress: Float): Float {
		return MathHelper.lerpAngleDegrees(tickProgress, entity.lastBodyYaw, entity.bodyYaw)
	}

	fun getModel(character: Character): Pair<M, Identifier>? {
		var modelPath = character.race.id + '/' + character.model
		if(!bipedModels.contains(modelPath)) modelPath = bipedModels.keys.firstOrNull { it.startsWith(character.race.id + '/') } ?: return null
		return bipedModels[modelPath]!!.second to Identifier.of(MTT.MOD_ID, "textures/entity/$modelPath/default.png")
	}
}