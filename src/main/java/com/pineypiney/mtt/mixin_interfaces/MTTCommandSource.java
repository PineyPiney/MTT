package com.pineypiney.mtt.mixin_interfaces;

import com.pineypiney.mtt.dnd.DNDEngine;
import com.pineypiney.mtt.dnd.characters.Character;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface MTTCommandSource {
	DNDEngine<?> mTT$getEngine();

	PlayerEntity mTT$getPlayer();

	default Collection<? extends Character> getCharacterSuggestions(boolean playersOnly) {
		DNDEngine<?> engine = mTT$getEngine();
		return playersOnly ? engine.getAllPlayableCharacters() : engine.getAllCharacters();
	}

	@Nullable Vec3d mTT$getPosition();
}
