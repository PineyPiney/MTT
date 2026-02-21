package com.pineypiney.mtt.mixin.client;

import com.pineypiney.mtt.client.dnd.ClientDNDEngine;
import com.pineypiney.mtt.client.dnd.spell_selector.SpellSelector;
import com.pineypiney.mtt.client.render.MTTRenderers;
import com.pineypiney.mtt.dnd.characters.Character;
import com.pineypiney.mtt.dnd.characters.SheetCharacter;
import com.pineypiney.mtt.entity.DNDEntity;
import com.pineypiney.mtt.item.dnd.DNDItem;
import com.pineypiney.mtt.mixin_interfaces.DNDClient;
import com.pineypiney.mtt.mixin_interfaces.DNDEngineHolder;
import com.pineypiney.mtt.network.payloads.c2s.CastSpellC2SPayload;
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
import net.minecraft.client.world.ClientWorld;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin implements DNDEngineHolder<ClientDNDEngine>, DNDClient {

	@Shadow
	@Final
	private static Text SOCIAL_INTERACTIONS_NOT_AVAILABLE;
	@Shadow
	@Nullable
	public ClientPlayerEntity player;

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
	@Nullable
	public ClientWorld world;
	@Shadow
	@Final
	private NarratorManager narratorManager;
	@Shadow
	@Nullable
	public HitResult crosshairTarget;
	@Unique
	@Nullable
	public HitResult dndCrosshairTarget = null;
	@Unique
	ClientDNDEngine dndEngine;

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

	@Unique
	@Nullable
	SpellSelector selector = null;
	@Shadow
	@Nullable
	private TutorialToast socialInteractionsToast;

	@Shadow
	public abstract void setScreen(@Nullable Screen screen);

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(RunArgs args, CallbackInfo ci) {
		MTTRenderers.Companion.registerBipedModels();
		MTTRenderers.Companion.registerEquipmentModels();
		dndEngine = new ClientDNDEngine((MinecraftClient) (Object) this);
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
				doUseItem(character);
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
	}

	@Unique
	void doAttack(Character character) {
		if (player == null) return;
		if (selector != null) {
			if (player.isSneaking()) {
				if (selector.selectionsMade() == selector.getSpell().getTargetCount()) {
					List<Vec3d> locations = selector.getLocations();
					List<Float> angles = selector.getAngles();
					ClientPlayNetworking.send(new CastSpellC2SPayload(selector.getSpell(), 1, locations, angles));
					selector.cancel();
					selector = null;
				}
			} else if (selector.select()) {
				player.swingHand(Hand.MAIN_HAND);
			}
		} else {
			if (crosshairTarget instanceof EntityHitResult entityHitResult && crosshairTarget.getType() == HitResult.Type.ENTITY) {
				if (entityHitResult.getEntity() instanceof DNDEntity dndEntity) {
					if (dndEntity.getCharacter() != null && dndEntity.getCharacter() != character) {
						ClientPlayNetworking.send(new CharacterInteractCharacterC2SPayload(dndEntity.getCharacter().getUuid()));
					}
				}
			}
		}
		player.swingHand(Hand.MAIN_HAND);
	}

	@Unique
	void doUseItem(Character character) {
		if (selector != null && player != null) {
			if (player.isSneaking()) {
				selector.cancel();
				selector = null;
				player.swingHand(Hand.MAIN_HAND);
			} else if (selector.unselect()) player.swingHand(Hand.MAIN_HAND);
		} else {
			ItemStack stack = character.getInventory().getHeldStack();
			Item item = stack.getItem();
			if (item instanceof DNDItem dndItem) {
				dndItem.use(dndEngine, character);
			}
		}
	}

	@Override
	public ClientDNDEngine mtt$getDNDEngine() {
		return dndEngine;
	}

	@Override
	public @Nullable SpellSelector mTT$getSpellSelector() {
		return selector;
	}

	@Override
	public void mTT$setSpellSelector(@Nullable SpellSelector selector) {
		this.selector = selector;
	}

	@Override
	public @Nullable HitResult mTT$getDndCrosshairTarget() {
		return dndCrosshairTarget;
	}

	@Override
	public void mTT$setDndCrosshairTarget(@Nullable HitResult dndCrosshairTarget) {
		this.dndCrosshairTarget = dndCrosshairTarget;
	}

	@Unique
	@Nullable
	public SheetCharacter getDNDCharacter(){
		ClientPlayerEntity player = this.player;
		if(player == null) return null;
		return dndEngine.getCharacterFromPlayer(player.getUuid());
	}
}