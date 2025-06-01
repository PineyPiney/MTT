package com.pineypiney.mtt.render.entity

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.entity.DNDEntity
import com.pineypiney.mtt.item.dnd.equipment.VisibleAccessoryItem
import com.pineypiney.mtt.render.MTTRenderers
import com.pineypiney.mtt.render.entity.model.BipedModelData
import com.pineypiney.mtt.render.entity.model.DNDBipedEntityModel
import com.pineypiney.mtt.render.entity.model.HelmetModel
import com.pineypiney.mtt.render.entity.state.DNDBipedEntityRenderState
import net.minecraft.client.model.ModelPart
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import java.io.File
import kotlin.math.cos

abstract class DNDBipedEntityRenderer<E: DNDEntity, S: DNDBipedEntityRenderState, M: DNDBipedEntityModel<S>>(ctx: EntityRendererFactory.Context, modelMaker: (ModelPart) -> M): EntityRenderer<E, S>(ctx) {

	val bipedModels = MTTRenderers.BIPED_MODELS.mapValues { (id, pair) -> pair.first to modelMaker(ctx.getPart(pair.second)) }
	val equipmentModels = MTTRenderers.EQUIPMENT_MODELS.map { type -> type.mapValues { (name, layer) -> ctx.getPart(layer) } }

	val allTextures = fetchAllTextures()

	override fun updateRenderState(entity: E, state: S, tickProgress: Float) {
		super.updateRenderState(entity, state, tickProgress)
		state.name = entity.name
		state.limbSwingAnimationProgress = entity.limbAnimator.getAnimationProgress(tickProgress)
		state.limbSwingAmplitude = entity.limbAnimator.getAmplitude(tickProgress)
		state.character = entity.character

		state.relativeHeadYaw = cos(state.limbSwingAnimationProgress * 0.666f) * .2f
	}

	override fun render(state: S, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int) {

		val (data, model: M) = bipedModels[state.character.model] ?: bipedModels["default"] ?: return

		matrices.push()
		matrices.scale(-1f, -1f, 1f)
		model.setAngles(state)
		val spec = if(state.character.model == "short") "dwarf" else "human"
		val texture = Identifier.of(MTT.MOD_ID, "textures/entity/$spec/default.png")
		val vertexConsumer: VertexConsumer? = vertexConsumers.getBuffer(model.getLayer(texture))
		model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV)
		renderHelmet(state, data, matrices, vertexConsumers, light)
		matrices.pop()
		super.render(state, matrices, vertexConsumers, light)
	}

	fun fetchAllTextures(): Map<String, Set<String>> {
		val textures = mutableMapOf<String, MutableSet<String>>()
		val resourcePacks = File("resourcepacks").listFiles()
		if(resourcePacks == null) return textures
		for(pack in resourcePacks){
			val speciesDirs = File(pack, "assets/mtt/textures/entity").listFiles()
			if(speciesDirs == null) continue
			for(species in speciesDirs){
				if(!species.isDirectory) continue
				val list = textures.getOrElse(species.name){ mutableSetOf<String>().apply { textures[species.name] = this } }
				for(file in species.listFiles()){
					if(file.extension == "png"){
						list.add(file.nameWithoutExtension)
					}
				}
			}
		}
		return textures
	}

	fun renderHelmet(state: S, data: BipedModelData, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int){
		val inventory = state.character.inventory

		// Try to render the aesthetic helmet, otherwise the functional helmet, otherwise none
		val renderedHelmet = inventory.getVisualHelmet()
		val item = renderedHelmet.item as? VisibleAccessoryItem ?: return
		val part = equipmentModels[0][item.model] ?: return
		val model = HelmetModel(part)

		val texture = Identifier.of(MTT.MOD_ID, "textures/equipment/${item.texture}.png")
		val consumer = vertexConsumers.getBuffer(model.getLayer(texture))

		matrices.push()
		matrices.translate(0f, .5f - (data.headTop * .0625f), 0f)
		model.setAngles(state)
		model.render(matrices, consumer, light, OverlayTexture.DEFAULT_UV)
		matrices.pop()
	}
}