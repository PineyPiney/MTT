package com.pineypiney.mtt.mixin.client;

import com.pineypiney.mtt.client.dnd.DNDClientEngine;
import com.pineypiney.mtt.client.render.MTTRenderers;
import com.pineypiney.mtt.dnd.characters.Character;
import com.pineypiney.mtt.dnd.characters.SheetCharacter;
import com.pineypiney.mtt.entity.DNDEntity;
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder;
import com.pineypiney.mtt.network.payloads.c2s.CharacterInteractCharacterC2SPayload;
import com.pineypiney.mtt.network.payloads.c2s.OpenDNDScreenC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.toast.TutorialToast;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin implements DNDEngineHolder<DNDClientEngine> {
	@Shadow @Nullable public ClientPlayerEntity player;

	@Shadow
	@Final
	private static Text SOCIAL_INTERACTIONS_NOT_AVAILABLE;
	@Shadow
	@Final
	public GameOptions options;
	@Shadow
	@Final
	public InGameHud inGameHud;

	@Shadow
	@Nullable
	public ClientPlayerInteractionManager interactionManager;

	@Shadow
	@Nullable
	public Screen currentScreen;

	@Shadow
	@Nullable
	public Overlay overlay;
	@Shadow
	@org.jspecify.annotations.Nullable
	public HitResult crosshairTarget;
	@Shadow
	private int itemUseCooldown;
	@Shadow
	@Final
	private NarratorManager narratorManager;

	@Shadow public abstract void setScreen(@Nullable Screen screen);

	@Shadow
	private @Nullable TutorialToast socialInteractionsToast;

	@Shadow
	protected abstract void doItemUse();

	@Shadow
	protected abstract void doItemPick();

	@Shadow
	public abstract void openChatScreen(ChatHud.ChatMethod method);

	@Shadow
	@Nullable
	public abstract ClientPlayNetworkHandler getNetworkHandler();

	@Shadow
	public abstract boolean isCtrlPressed();

	@Shadow
	protected abstract Optional<RegistryEntry<Dialog>> getQuickActionsDialog();

	@Shadow
	protected abstract boolean isConnectedToServer();

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(RunArgs args, CallbackInfo ci) {
		MTTRenderers.Companion.registerBipedModels();
		MTTRenderers.Companion.registerEquipmentModels();
	}

	@Inject(method = "handleInputEvents", at = @At(value = "JUMP", opcode = Opcodes.IF_ICMPGE, ordinal = 0), cancellable = true)
	private void handleInputs(CallbackInfo ci) {
		Character character = getDNDCharacter();
		if (!dndEngine.getRunning() || character == null || player == null) return;

		ci.cancel();
		for (int i = 0; i < 9; i++) {
			if (this.options.hotbarKeys[i].wasPressed()) {
				if (i < character.getInventory().getHotbarSize()) character.getInventory().setSelectedSlot(i);
			}
		}

		while (this.options.socialInteractionsKey.wasPressed()) {
			if (!this.isConnectedToServer() && !SharedConstants.SOCIAL_INTERACTIONS) {
				this.player.sendMessage(SOCIAL_INTERACTIONS_NOT_AVAILABLE, true);
				this.narratorManager.narrateSystemImmediately(SOCIAL_INTERACTIONS_NOT_AVAILABLE);
			} else {
				if (this.socialInteractionsToast != null) {
					this.socialInteractionsToast.hide();
					this.socialInteractionsToast = null;
				}

				this.setScreen(new SocialInteractionsScreen());
			}
		}

		while (this.options.inventoryKey.wasPressed()) {
			CustomPayload payload = new OpenDNDScreenC2SPayload(0);
			ClientPlayNetworking.send(payload);
		}

		while (this.options.advancementsKey.wasPressed()) {
			this.setScreen(new AdvancementsScreen(this.player.networkHandler.getAdvancementHandler()));
		}

		while (this.options.quickActionsKey.wasPressed()) {
			this.getQuickActionsDialog().ifPresent(dialog -> this.player.networkHandler.showDialog(dialog, this.currentScreen));
		}

		while (this.options.swapHandsKey.wasPressed()) {
			if (!this.player.isSpectator() && getNetworkHandler() != null) {
				this.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
			}
		}

		while (this.options.dropKey.wasPressed()) {
			if (!this.player.isSpectator() && this.player.dropSelectedItem(this.isCtrlPressed())) {
				this.player.swingHand(Hand.MAIN_HAND);
			}
		}

		while (this.options.chatKey.wasPressed()) {
			this.openChatScreen(ChatHud.ChatMethod.MESSAGE);
		}

		if (this.currentScreen == null && this.overlay == null && this.options.commandKey.wasPressed()) {
			this.openChatScreen(ChatHud.ChatMethod.COMMAND);
		}

		if (this.player.isUsingItem()) {
			if (!this.options.useKey.isPressed() && interactionManager != null) {
				this.interactionManager.stopUsingItem(this.player);
			}

			while (this.options.attackKey.wasPressed()) {
			}
			while (this.options.useKey.wasPressed()) {
			}
			while (this.options.pickItemKey.wasPressed()) {
			}
		} else {
			while (this.options.attackKey.wasPressed()) {
				doAttack(character);
			}

			while (this.options.useKey.wasPressed()) {
				this.doItemUse();
			}

			while (this.options.pickItemKey.wasPressed()) {
				this.doItemPick();
			}

			if (this.player.isSpectator()) {
				while (this.options.spectatorHotbarKey.wasPressed()) {
					this.inGameHud.getSpectatorHud().useSelectedCommand();
				}
			}
		}

		if (this.options.useKey.isPressed() && this.itemUseCooldown == 0 && !this.player.isUsingItem()) {
			this.doItemUse();
		}
	}

	@Unique
	void doAttack(Character character) {
		if (crosshairTarget instanceof EntityHitResult entityHitResult && crosshairTarget.getType() == HitResult.Type.ENTITY) {
			if (entityHitResult.getEntity() instanceof DNDEntity dndEntity) {
				if (dndEntity.getCharacter() != null && dndEntity.getCharacter() != character) {
					character.attack(dndEntity.getCharacter());
					ClientPlayNetworking.send(new CharacterInteractCharacterC2SPayload(dndEntity.getCharacter().getUuid()));
				}
			}
		}
		if (player != null) player.swingHand(Hand.MAIN_HAND);
	}

	@Unique
	DNDClientEngine dndEngine = new DNDClientEngine((MinecraftClient)(Object)this);

	@Override
	public DNDClientEngine mtt$getDNDEngine() {
		return dndEngine;
	}

	@Unique
	@Nullable
	public SheetCharacter getDNDCharacter(){
		ClientPlayerEntity player = this.player;
		if(player == null) return null;
		return dndEngine.getCharacterFromPlayer(player.getUuid());
	}
}