package com.pineypiney.mtt.mixin_interfaces;

import com.pineypiney.mtt.client.dnd.spell_selector.SpellSelector;
import net.minecraft.util.hit.HitResult;
import org.jspecify.annotations.Nullable;

public interface DNDClient {

	@Nullable HitResult mTT$getDndCrosshairTarget();

	void mTT$setDndCrosshairTarget(@Nullable HitResult dndCrosshairTarget);

	@Nullable SpellSelector mTT$getSpellSelector();

	void mTT$setSpellSelector(@Nullable SpellSelector selector);
}
