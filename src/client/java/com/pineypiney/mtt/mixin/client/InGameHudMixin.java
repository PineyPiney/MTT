package com.pineypiney.mtt.mixin.client;

import com.pineypiney.mtt.client.dnd.ClientDNDEngine;
import com.pineypiney.mtt.client.dnd.network.ClientCharacter;
import com.pineypiney.mtt.dnd.characters.Character;
import com.pineypiney.mtt.dnd.combat.CombatManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

	@Shadow @Final private static Identifier HOTBAR_TEXTURE;

	@Shadow @Final private static Identifier HOTBAR_SELECTION_TEXTURE;

	@Shadow @Final private static Identifier HOTBAR_OFFHAND_LEFT_TEXTURE;

	@Shadow @Final private static Identifier HOTBAR_OFFHAND_RIGHT_TEXTURE;

	@Shadow protected abstract void renderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed);

	@Shadow @Final private MinecraftClient client;

	@Shadow
	@Final
	private static Identifier ARMOR_FULL_TEXTURE;
	@Shadow
	@Final
	private static Identifier ARMOR_HALF_TEXTURE;

	@Shadow
	protected abstract void drawHeart(DrawContext context, InGameHud.HeartType type, int x, int y, boolean hardcore, boolean blinking, boolean half);

	@Shadow
	public abstract TextRenderer getTextRenderer();

	@Shadow
	protected abstract void renderMiscOverlays(DrawContext context, RenderTickCounter tickCounter);

	@Shadow
	protected abstract void renderCrosshair(DrawContext context, RenderTickCounter tickCounter);

	@Shadow
	protected abstract void renderStatusEffectOverlay(DrawContext context, RenderTickCounter tickCounter);

	@Shadow
	protected abstract void renderBossBarHud(DrawContext context, RenderTickCounter tickCounter);

	@Shadow
	protected abstract void renderDemoTimer(DrawContext context, RenderTickCounter tickCounter);

	@Shadow
	protected abstract void renderOverlayMessage(DrawContext context, RenderTickCounter tickCounter);

	@Shadow
	protected abstract void renderTitleAndSubtitle(DrawContext context, RenderTickCounter tickCounter);

	@Shadow
	protected abstract void renderChat(DrawContext context, RenderTickCounter tickCounter);

	@Shadow
	protected abstract void renderPlayerList(DrawContext context, RenderTickCounter tickCounter);

	@Shadow
	protected abstract void renderSubtitlesHud(DrawContext context, boolean defer);

	@Shadow
	protected abstract void renderHeldItemTooltip(DrawContext context);

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void renderDndHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		ClientDNDEngine engine = ClientDNDEngine.Companion.getInstance();
		ClientCharacter character = ClientDNDEngine.Companion.getRunningAndClientCharacter();
		ClientPlayerEntity player = client.player;
		if (character == null) return;
		CombatManager combat = engine.getCombat(character);

		ci.cancel();
		if (!(this.client.currentScreen instanceof LevelLoadingScreen)) {
			if (!this.client.options.hudHidden) {
				this.renderMiscOverlays(context, tickCounter);
				this.renderCrosshair(context, tickCounter);
				context.createNewRootLayer();
				this.renderMainDndHud(context, player, character, tickCounter);
				this.renderStatusEffectOverlay(context, tickCounter);
				this.renderBossBarHud(context, tickCounter);
				this.renderDemoTimer(context, tickCounter);
				if (combat != null) this.renderCombatCards(context, combat);
				this.renderOverlayMessage(context, tickCounter);
				this.renderTitleAndSubtitle(context, tickCounter);
				this.renderChat(context, tickCounter);
				this.renderPlayerList(context, tickCounter);
				this.renderSubtitlesHud(context, this.client.currentScreen == null || this.client.currentScreen.deferSubtitles());
			}
		}
	}

	@Unique
	private void renderMainDndHud(DrawContext ctx, ClientPlayerEntity player, ClientCharacter character, RenderTickCounter tickCounter) {
		this.renderDNDHotbar(ctx, player, character, tickCounter);
		this.renderStatusBars(ctx, character);
		this.renderHeldItemTooltip(ctx);
	}

	@Unique
	private void renderDNDHotbar(DrawContext context, ClientPlayerEntity player, ClientCharacter character, RenderTickCounter tickCounter) {
		Arm offhandArm = player.getMainArm().getOpposite();
		int i = context.getScaledWindowWidth() / 2;
		context.getMatrices().pushMatrix();
		context.getMatrices().translate(0.0F, 0.0F);
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HOTBAR_TEXTURE, i - 91, context.getScaledWindowHeight() - 22, 182, 22);
		context.drawGuiTexture(
				RenderPipelines.GUI_TEXTURED,
				HOTBAR_SELECTION_TEXTURE,
				i - 92 + character.getInventory().getSelectedSlot() * 20,
				context.getScaledWindowHeight() - 23,
				24,
				23
		);
		var offhandStack = character.getInventory().getOffhandSlotIcon();
		if (!offhandStack.isEmpty()) {
			if (offhandArm == Arm.LEFT)
				context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_TEXTURE, i - 91 - 29, context.getScaledWindowHeight() - 23, 29, 24);
			else
				context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_RIGHT_TEXTURE, i + 91, context.getScaledWindowHeight() - 23, 29, 24);
		}
		context.getMatrices().popMatrix();

		var stackY = context.getScaledWindowHeight() - 19;
		for(int j = 0; j < 4;){
			int n = i - 88 + j * 20;
			renderHotbarItem(context, n, stackY, tickCounter, player, character.getInventory().getHotbarSlotStack(j), ++j);
		}

		if(!offhandStack.isEmpty()){
			if (offhandArm == Arm.LEFT) renderHotbarItem(context, i - 117, stackY, tickCounter, player, offhandStack, 4);
			else renderHotbarItem(context, i + 101, stackY, tickCounter, player, offhandStack, 4);
		}
	}

	@Unique
	private void renderStatusBars(DrawContext context, ClientCharacter character) {
		int x = context.getScaledWindowWidth() / 2 - 91;
		int y = context.getScaledWindowHeight() - 39;
		y = renderHealth(context, character, x, y);
		y = renderArmour(context, character, x, y);
	}

	@Unique
	private int renderHealth(DrawContext context, Character character, int x, int y) {
		int maxHealth = character.getMaxHealth();
		int health = character.getHealth();
		int maxTempHealth = 2;
		int tempHealth = 1;
		int baseHearts = MathHelper.ceil(maxHealth * .5f);
		int tempHearts = MathHelper.ceil(maxTempHealth * .5f);
		for (int i = 0; i < baseHearts + tempHearts; i++) {
			int heartX = x + (i % 10) * 8;
			int heartY = y - (i / 10) * 8;
			drawHeart(context, InGameHud.HeartType.CONTAINER, heartX, heartY, false, false, false);

			boolean temp = i >= baseHearts;
			int heartValue = temp ? (i - baseHearts) * 2 : i * 2;
			int ref = temp ? tempHealth : health;
			if (heartValue >= ref) continue;
			boolean half = heartValue + 1 == ref;
			drawHeart(context, temp ? InGameHud.HeartType.ABSORBING : InGameHud.HeartType.NORMAL, heartX, heartY, false, false, half);
		}

		return y - (MathHelper.ceilDiv(baseHearts, 10) * 8);
	}

	@Unique
	private int renderArmour(DrawContext context, ClientCharacter character, int x, int y) {
		int armour = character.getTotalArmour();
		int armourIcons = MathHelper.ceilDiv(armour, 2);
		for (int i = 0; i < armourIcons; i++) {
			int iconX = x + (i % 10) * 8;
			int iconY = y - (i / 10) * 8;
			Identifier texture = armour > (i << 1) + 1 ? ARMOR_FULL_TEXTURE : ARMOR_HALF_TEXTURE;
			context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, texture, iconX, iconY, 9, 9);
		}
		return y - (MathHelper.ceilDiv(armourIcons, 10) * 8);
	}

	@Unique
	private void renderCombatCards(DrawContext context, CombatManager combat) {

		Text text = Text.literal("Combat Turns");
		int i = this.getTextRenderer().getWidth(text);
		int j = i;
		int k = this.getTextRenderer().getWidth(": ");

		for (CombatManager.Combatant combatEntry : combat) {
			j = Math.max(j, getTextRenderer().getWidth(combatEntry.getCharacter().getName()) + k + getTextRenderer().getWidth(String.valueOf(combatEntry.getInitiative())));
		}

		int m = combat.size();
		int n = m * 9;
		int o = context.getScaledWindowHeight() / 2 + n / 3;
		int q = context.getScaledWindowWidth() - j - 3;
		int r = context.getScaledWindowWidth() - 3 + 2;
		int s = this.client.options.getTextBackgroundColor(0.3F);
		int t = this.client.options.getTextBackgroundColor(0.4F);
		int u = o - m * 9;
		context.fill(q - 2, u - 9 - 1, r, u - 1, t);
		context.fill(q - 2, u - 1, r, o, s);
		context.drawText(this.getTextRenderer(), text, q + j / 2 - i / 2, u - 9, Colors.WHITE, false);

		int v = 0;
		CombatManager.Combatant current = combat.getCurrentCombatant();
		for (CombatManager.Combatant combatEntry : combat) {
			int y = o - (m - v++) * 9;
			MutableText nameText = Text.literal(combatEntry.getCharacter().getName());
			MutableText scoreText = Text.literal(String.valueOf(combatEntry.getInitiative())).formatted(Formatting.RED);
			if (combatEntry == current) nameText.withColor(-0xFF3FD0);
			context.drawText(this.getTextRenderer(), nameText, q, y, Colors.WHITE, false);
			context.drawText(this.getTextRenderer(), scoreText, r - getTextRenderer().getWidth(scoreText), y, Colors.WHITE, false);
		}
	}

	@ModifyVariable(method = "tick()V", at = @At("STORE"), ordinal = 0)
	private ItemStack getHeldStack(ItemStack playerStack) {
		var character = ClientDNDEngine.Companion.getRunningAndPlayerCharacter(client.player);
		return character == null ? playerStack : character.getInventory().getHeldStack();
	}
}
