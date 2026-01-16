package com.example.myplugin;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nullable;

/**
 * Simple runtime registry for item lifesteal values.
 *
 * - Call setItemLifesteal(itemId, value) to register a value at runtime.
 * - Call getItemLifesteal(itemId) to retrieve a value (may return null).
 *
 * This avoids changing engine classes directly; adapt LifestealSystems to check this registry.
 */
public final class LifestealRegistry {
    private static final ConcurrentMap<String, Double> ITEM_LIFESTEAL_MAP = new ConcurrentHashMap<>();

    private LifestealRegistry() {}

    /**
     * Set or override the lifesteal fraction for an item id (e.g. "items/dagger_basic").
     * @param itemId exact item asset id
     * @param lifesteal fraction (0.12 = 12%)
     */
    public static void setItemLifesteal(String itemId, double lifesteal) {
        if (itemId == null) return;
        ITEM_LIFESTEAL_MAP.put(itemId, lifesteal);
        System.out.println("[LifestealRegistry] set " + itemId + " -> " + lifesteal);
    }

    /**
     * Get the lifesteal value for an item id, or null if none registered.
     */
    @Nullable
    public static Double getItemLifesteal(String itemId) {
        if (itemId == null) return null;
        return ITEM_LIFESTEAL_MAP.get(itemId);
    }

    /**
     * Clear all registered mappings.
     */
    public static void clear() {
        ITEM_LIFESTEAL_MAP.clear();
    }
}