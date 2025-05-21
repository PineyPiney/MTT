package com.pineypiney.mtt.render.entity

import com.pineypiney.mtt.MTT
import com.pineypiney.mtt.entity.DNDEntity
import com.pineypiney.mtt.render.entity.model.DNDBipedEntityModel
import com.pineypiney.mtt.render.entity.state.DNDBipedEntityRenderState
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import java.io.File

abstract class DNDBipedEntityRenderer<E: DNDEntity, S: DNDBipedEntityRenderState, M: DNDBipedEntityModel<S>>(ctx: EntityRendererFactory.Context): EntityRenderer<E, S>(ctx) {

	abstract val model: M
	val texture = Identifier.of(MTT.MOD_ID, "textures/entity/dwarf/default.png")

	val allTextures = fetchAllTextures()

	override fun updateRenderState(entity: E, state: S, tickProgress: Float) {
		super.updateRenderState(entity, state, tickProgress)
		state.name = entity.name
		state.limbSwingAnimationProgress = entity.limbAnimator.getAnimationProgress(tickProgress)
		state.limbSwingAmplitude = entity.limbAnimator.getAmplitude(tickProgress)
	}

	override fun render(
		state: S   ,
		matrices: MatrixStack,
		vertexConsumers: VertexConsumerProvider,
		light: Int
	) {
		matrices.push()
		matrices.scale(-1f, -1f, 1f)
		model.setAngles(state)
		val vertexConsumer: VertexConsumer? = vertexConsumers.getBuffer(model.getLayer(texture))
		model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV)

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
}