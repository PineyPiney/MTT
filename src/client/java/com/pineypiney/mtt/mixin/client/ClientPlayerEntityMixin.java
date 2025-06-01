package com.pineypiney.mtt.mixin.client;

import com.mojang.authlib.GameProfile;
import com.pineypiney.mtt.MTT;
import com.pineypiney.mtt.dnd.DNDEngine;
import com.pineypiney.mtt.dnd.characters.SheetCharacter;
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

	@Shadow @Final protected MinecraftClient client;

	public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
		super(world, profile);
	}

	//@Inject(method = "tickMovementInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;applyMovementSpeedFactors(Lnet/minecraft/util/math/Vec2f;)Lnet/minecraft/util/math/Vec2f;"))

	@ModifyVariable(method = "tickMovementInput", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/network/ClientPlayerEntity;applyMovementSpeedFactors(Lnet/minecraft/util/math/Vec2f;)Lnet/minecraft/util/math/Vec2f;"), ordinal = 0)
	private Vec2f freezeDndPlayers(Vec2f vec){
		DNDEngine engine = ((DNDEngineHolder<?>) client).mtt$getDNDEngine();
		if(engine == null){
			MTT.Companion.getLogger().warn("Client does not have DNDEngine");
			return vec;
		}
		if(!engine.getRunning() || uuid.equals(engine.getDM())) return vec;
		SheetCharacter character = engine.getPlayerCharacter(uuid);
		if(character == null) return vec;
		return engine.isInCombat(character) ? new Vec2f(0f, 0f) : vec;
	}
}
