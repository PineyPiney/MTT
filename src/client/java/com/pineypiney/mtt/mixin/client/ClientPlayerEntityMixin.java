package com.pineypiney.mtt.mixin.client;

import com.mojang.authlib.GameProfile;
import com.pineypiney.mtt.MTT;
import com.pineypiney.mtt.client.dnd.ClientDNDEngine;
import com.pineypiney.mtt.dnd.DNDEngine;
import com.pineypiney.mtt.dnd.characters.SheetCharacter;
import com.pineypiney.mtt.entity.DNDEntity;
import com.pineypiney.mtt.mixin_interfaces.CharacterController;
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder;
import com.pineypiney.mtt.network.payloads.c2s.CharacterMoveC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerLikeState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity implements CharacterController {

	@Shadow @Final protected MinecraftClient client;

	@Shadow
	public Input input;
	@Unique
	public double characterLastXClient;
	@Unique
	public double characterLastYClient;
	@Unique
	public double characterLastZClient;
	@Unique
	public float characterLastYawClient;
	@Unique
	public float characterLastPitchClient;
	@Unique
	public boolean characterLastHorizontalCollision;
	@Unique
	public int ticksSinceLastCharacterPositionPacketSent;
	@Unique
	public float characterRenderYaw;
	@Unique
	public float characterRenderPitch;
	@Unique
	public float characterLastRenderYaw;
	@Unique
	public float characterLastRenderPitch;
	@Unique
	public ClientPlayerLikeState state = new ClientPlayerLikeState();
	@Unique
	private boolean characterLastOnGround;

	@Redirect(method = "getCrosshairTarget(Lnet/minecraft/entity/Entity;DDF)Lnet/minecraft/util/hit/HitResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"))
	private static EntityHitResult raycastDNDEntities(Entity entity, Vec3d min, Vec3d max, Box box, Predicate<Entity> predicate, double maxDistance) {
		var engine = ClientDNDEngine.Companion.getInstance();
		if (engine.getRunning() && entity instanceof DNDEntity dndEntity) {
			var character = dndEntity.getCharacter();
			if (character != null) {
				return ProjectileUtil.raycast(entity, min, max, box, e -> e instanceof DNDEntity other && other.canBeHit(character), maxDistance);
			}
		}
		return ProjectileUtil.raycast(entity, min, max, box, predicate, maxDistance);
	}

	public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
		super(world, profile);
	}

	@Shadow
	protected abstract Vec2f applyMovementSpeedFactors(Vec2f input);

	@Override
	public ItemStack getActiveOrMainHandStack() {
		var character = ClientDNDEngine.Companion.getRunningAndPlayerCharacter(this);
		if (character == null) return super.getActiveOrMainHandStack();
		else return character.getInventory().getHeldStack();
	}

	@Override
	public void changeLookDirection(double cursorDeltaX, double cursorDeltaY) {
		DNDEntity entity = ClientDNDEngine.Companion.getRunningAndPlayerCharacterEntity(this);
		if (entity == null) super.changeLookDirection(cursorDeltaX, cursorDeltaY);
		else entity.changeLookDirection(cursorDeltaX, cursorDeltaY);
	}

	@Inject(method = "tickMovementInput()V", at = @At("HEAD"), cancellable = true)
	private void moveCharacter(CallbackInfo ci) {
		DNDEngine engine = ((DNDEngineHolder<?>) client).mtt$getDNDEngine();
		if(engine == null){
			MTT.Companion.getLogger().warn("Client does not have DNDEngine");
			return;
		}
		if (!engine.getRunning() || uuid.equals(engine.getDM())) return;
		SheetCharacter character = engine.getCharacterFromPlayer(uuid);
		if (character == null) return;

		ci.cancel();
		DNDEntity entity = engine.getEntityFromPlayer(uuid);
		if (entity == null) return;

		Vec2f vec2f = this.applyMovementSpeedFactors(this.input.getMovementInput());
		entity.setSidewaysSpeed(vec2f.x);
		entity.setForwardSpeed(vec2f.y);
		entity.setJumping(this.input.playerInput.jump());

		this.characterLastRenderYaw = this.characterRenderYaw;
		this.characterLastRenderPitch = this.characterRenderPitch;
		this.characterRenderPitch = this.characterRenderPitch + (entity.getPitch() - this.characterRenderPitch) * 0.5F;
		this.characterRenderYaw = this.characterRenderYaw + (entity.getYaw() - this.characterRenderYaw) * 0.5F;
	}

	@Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)
	private void sendCharacterMovementPackets(CallbackInfo ci) {
		DNDEntity entity = ClientDNDEngine.Companion.getRunningAndPlayerCharacterEntity(this);
		if (entity == null) return;

		ci.cancel();
		double d = entity.getX() - this.characterLastXClient;
		double e = entity.getY() - this.characterLastYClient;
		double f = entity.getZ() - this.characterLastZClient;
		double g = entity.getYaw() - this.characterLastYawClient;
		double h = entity.getPitch() - this.characterLastPitchClient;
		this.ticksSinceLastCharacterPositionPacketSent++;
		boolean bl = MathHelper.squaredMagnitude(d, e, f) > MathHelper.square(2.0E-4) || this.ticksSinceLastCharacterPositionPacketSent >= 20;
		boolean bl2 = g != 0.0 || h != 0.0;
		if (bl && bl2) {
			ClientPlayNetworking.send(new CharacterMoveC2SPayload(entity.getEntityPos(), entity.getYaw(), entity.getPitch(), entity.isOnGround(), characterLastHorizontalCollision));
//			this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(this.getEntityPos(), this.getYaw(), this.getPitch(), this.isOnGround(), this.characterLastHorizontalCollision));
		} else if (bl) {
			ClientPlayNetworking.send(new CharacterMoveC2SPayload(entity.getEntityPos(), entity.isOnGround(), characterLastHorizontalCollision));
//			this.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(this.getEntityPos(), this.isOnGround(), this.horizontalCollision));
		} else if (bl2) {
			ClientPlayNetworking.send(new CharacterMoveC2SPayload(entity.getYaw(), entity.getPitch(), entity.isOnGround(), characterLastHorizontalCollision));
//			this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(this.getYaw(), this.getPitch(), this.isOnGround(), this.horizontalCollision));
		} else if (this.characterLastOnGround != entity.isOnGround() || this.characterLastHorizontalCollision != entity.horizontalCollision) {
			ClientPlayNetworking.send(new CharacterMoveC2SPayload(entity.isOnGround(), characterLastHorizontalCollision));
//			this.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(this.isOnGround(), this.horizontalCollision));
		}

		if (bl) {
			this.characterLastXClient = entity.getX();
			this.characterLastYClient = entity.getY();
			this.characterLastZClient = entity.getZ();
			this.ticksSinceLastCharacterPositionPacketSent = 0;
		}

		if (bl2) {
			this.characterLastYawClient = entity.getYaw();
			this.characterLastPitchClient = entity.getPitch();
		}

		this.characterLastOnGround = entity.isOnGround();
		this.characterLastHorizontalCollision = entity.horizontalCollision;
//		this.autoJumpEnabled = this.client.options.getAutoJump().getValue();
	}

	@Override
	public float mTT$getYaw() {
		return characterRenderYaw;
	}

	@Override
	public float mTT$getPitch() {
		return characterRenderPitch;
	}

	@Override
	public float mTT$getLastYaw() {
		return characterLastRenderYaw;
	}

	@Override
	public float mTT$getLastPitch() {
		return characterLastRenderPitch;
	}
}
