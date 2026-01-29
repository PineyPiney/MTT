package com.pineypiney.mtt.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// The buffer manager keeps calling glBindVertexBuffer with zeroed parameters for some reason
// This is not an optimisation mod but the constant OpenGL warnings in the console were pissing me off
@Mixin(targets = "net.minecraft.client.gl.BufferManager$ARBBufferManager", remap = false)
public class ARBBufferManagerMixin {

	@Redirect(method = "setupBuffer", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/ARBVertexAttribBinding;glBindVertexBuffer(IIJI)V", ordinal = 1))
	private void preventGLError(int bindingindex, int buffer, long offset, int stride){}
}
