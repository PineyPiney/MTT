package com.pineypiney.mtt.client.render.entity

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.client.dnd.ClientDNDEngine
import com.pineypiney.mtt.client.dnd.network.CharacterGameText
import com.pineypiney.mtt.client.dnd.network.ClientDNDEntity
import com.pineypiney.mtt.client.render.MTTRenderers
import com.pineypiney.mtt.client.render.entity.model.BipedModelData
import com.pineypiney.mtt.client.render.entity.model.DNDBipedEntityModel
import com.pineypiney.mtt.client.render.entity.model.HelmetModel
import com.pineypiney.mtt.client.render.entity.state.DNDBipedEntityRenderState
import com.pineypiney.mtt.dnd.characters.Character
import com.pineypiney.mtt.entity.DNDEntity
import com.pineypiney.mtt.item.dnd.equipment.VisibleAccessoryItem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.command.OrderedRenderCommandQueue
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.state.CameraRenderState
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.EntityPose
import net.minecraft.text.Style
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
			ClientDNDEngine.getInstance().running && ClientDNDEngine.getClientCharacterUUID() == state.character.uuid
		}
		else false

		val g = MathHelper.lerpAngleDegrees(f, entity.lastHeadYaw, entity.headYaw)
		state.bodyYaw = clampBodyYaw(entity, g, f)
		state.relativeHeadYaw = MathHelper.wrapDegrees(g - state.bodyYaw)
		state.pitch = entity.getLerpedPitch(f)
		state.displayName = entity.customName

		state.limbSwingAnimationProgress = entity.limbAnimator.getAnimationProgress(f)
		state.limbSwingAmplitude = entity.limbAnimator.getAmplitude(f)
		if (entity !is ClientDNDEntity) return
		state.outlineColor = entity.selectionColour
		state.texts.clear()
		state.texts.addAll(entity.texts)
	}

	override fun render(
		state: S,
		matrices: MatrixStack,
		queue: OrderedRenderCommandQueue,
		cameraState: CameraRenderState
	) {
		if (!state.ready() || state.invisibleToPlayer) return

		var modelPath = state.character.race.id + '/' + state.character.model.id
		if(!bipedModels.contains(modelPath)) modelPath = bipedModels.keys.firstOrNull { it.startsWith(state.character.race.id + '/') } ?: return
		val (data, model: M) = bipedModels[modelPath]!!

		matrices.push()
		setupTransforms(state, matrices)
		matrices.scale(-1f, -1f, 1f)
		model.setAngles(state)
		val texture = Identifier.of(MTT.MOD_ID, "textures/entity/$modelPath/default.png")
		queue.submitModel(
			model,
			state,
			matrices,
			model.getLayer(texture),
			state.light,
			655360,
			-1,
			null,
			state.outlineColor,
			null
		)
		renderHelmet(state, data, matrices, queue, state.light)
		matrices.pop()

		for (text in state.texts) renderText(text, queue, matrices, state, cameraState)
		renderLabelIfPresent(state, matrices, queue, cameraState)
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
		queue: OrderedRenderCommandQueue,
		light: Int
	) {
		val inventory = state.character.inventory

		// Try to render the aesthetic helmet, otherwise the functional helmet, otherwise none
		val renderedHelmet = inventory.getVisualHelmet()
		val item = renderedHelmet.item as? VisibleAccessoryItem ?: return
		val part = equipmentModels[0][item.model] ?: return
		val model = HelmetModel<S>(part)

		val texture = Identifier.of(MTT.MOD_ID, "textures/equipment/${item.texture}.png")

		matrices.push()
		val scale = data.headHeight * .125f
		matrices.scale(scale, scale, scale)
		matrices.translate(0f, .5f - ((data.headTop * .0625f) / scale), 0f)
		model.setAngles(state)
		queue.submitModel(
			model,
			state,
			matrices,
			model.getLayer(texture),
			state.light,
			655360,
			-1,
			null,
			state.outlineColor,
			null
		)
		matrices.pop()
	}

	fun renderText(text: CharacterGameText, queue: OrderedRenderCommandQueue, matrices: MatrixStack, state: S, cameraState: CameraRenderState) {

		val client = MinecraftClient.getInstance()
		matrices.push()
		matrices.translate(text.pos)
		matrices.multiply(cameraState.orientation)
		val s = .015f
		matrices.scale(s, -s, s)
		val width = 150
		val lines = client.textRenderer.textHandler.wrapLines(text.text, width, Style.EMPTY)
		val height = lines.size * 9
		for ((i, line) in lines.withIndex()) {
			val x = client.textRenderer.getWidth(line) * -.5f
			val y = i * 9 - (height * .5f)
			queue.submitText(matrices, x, y, text.text.asOrderedText(), false, TextRenderer.TextLayerType.NORMAL, state.light, -1, 0, 0)
		}
		matrices.pop()
	}

	private fun clampBodyYaw(entity: E, degrees: Float, tickProgress: Float): Float {
		return MathHelper.lerpAngleDegrees(tickProgress, entity.lastBodyYaw, entity.bodyYaw)
	}

	fun getModel(character: Character): Pair<M, Identifier>? {
		var modelPath = character.race.id + '/' + character.model.id
		if(!bipedModels.contains(modelPath)) modelPath = bipedModels.keys.firstOrNull { it.startsWith(character.race.id + '/') } ?: return null
		return bipedModels[modelPath]!!.second to Identifier.of(MTT.MOD_ID, "textures/entity/$modelPath/default.png")
	}
}