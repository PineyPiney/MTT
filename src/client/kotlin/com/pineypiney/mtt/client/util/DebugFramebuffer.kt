package com.pineypiney.mtt.client.util

import net.minecraft.client.texture.GlTexture
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11C.*
import org.lwjgl.stb.STBImageWrite
import java.nio.ByteBuffer

object DebugFramebuffer {

	fun getColourTextureData(texture: GlTexture): ByteBuffer {
		glBindTexture(GL_TEXTURE_2D, texture.glId)
		val buffer = BufferUtils.createByteBuffer(texture.getWidth(0) * texture.getHeight(0) * 4)
		glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer)
		return buffer
	}

	fun writeToTexture(texture: GlTexture, file: String): Boolean {
		val d = getColourTextureData(texture)
		val fileName = if (file.endsWith(".png")) file else "$file.png"
		STBImageWrite.stbi_flip_vertically_on_write(true)
		return STBImageWrite.stbi_write_png(fileName, texture.getWidth(0), texture.getHeight(0), 4, d, 4 * texture.getWidth(0))
	}
}