package com.pineypiney.mtt.mixin_interfaces;

import com.pineypiney.mtt.dnd.DNDEngine;

public interface DNDEngineHolder<T extends DNDEngine<?>> {
	T mtt$getDNDEngine();
}
