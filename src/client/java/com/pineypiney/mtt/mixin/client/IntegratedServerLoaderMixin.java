package com.pineypiney.mtt.mixin.client;

import com.pineypiney.mtt.dnd.server.DNDServerEngine;
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.SaveLoading;
import net.minecraft.server.integrated.IntegratedServerLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IntegratedServerLoader.class)
public class IntegratedServerLoaderMixin {

	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(method = "load(Lnet/minecraft/server/SaveLoading$DataPacks;Lnet/minecraft/server/SaveLoading$LoadContextSupplier;Lnet/minecraft/server/SaveLoading$SaveApplierFactory;)Ljava/lang/Object;", at = @At("TAIL"))
	private <D, R> void loadMixin(SaveLoading.DataPacks dataPacks, SaveLoading.LoadContextSupplier<D> loadContextSupplier, SaveLoading.SaveApplierFactory<D, R> saveApplierFactory, CallbackInfoReturnable<R> cir) {
		if (client.getServer() instanceof DNDEngineHolder<?> holder) {
			if (holder.mtt$getDNDEngine() instanceof DNDServerEngine engine) engine.loadData();
		}
	}
}
