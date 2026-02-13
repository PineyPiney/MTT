package com.pineypiney.mtt.client.render.entity.model

data class BipedModelData(
	val legLength: Int,
	val bodyHeight: Int,
	val headOrigin: Int,
	val headHeight: Int,
	val headWidth: Int
){

	val headTop get() = headOrigin + headHeight
}