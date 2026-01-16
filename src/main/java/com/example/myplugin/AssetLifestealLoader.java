package com.example.myplugin;

import com.hypixel.hytale.server.core.asset.type.item.config.Item;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Scans loaded Item assets and populates LifestealRegistry.
 *
 * Usage:
 * - Call AssetLifestealLoader.populateFromAssets() after assets have been loaded (or on demand)
 *   to register lifesteal values found in item assets.
 *
 * Behavior:
 * - Tries multiple heuristics to find a lifesteal value:
 *   1) Direct getter on Item: getLifesteal(), getLifeSteal(), etc.
 *   2) item.getConfig() -> config getter/field
 *   3) Any Map-returning method on Item that contains key "Lifesteal" (case variants)
 *
 * Notes:
 * - This is intentionally resilient: uses reflection and ignores exceptions for maximum compatibility.
 * - To make combat code use these values, ensure your LifestealSystems checks LifestealRegistry.getItemLifesteal(itemId).
 */
public final class AssetLifestealLoader {
    private AssetLifestealLoader() {}

    public static void populateFromAssets() {
        try {
            Object assetMapObj = Item.getAssetMap().getAssetMap();
            if (!(assetMapObj instanceof Map)) {
                System.out.println("[AssetLifestealLoader] item asset map not present or unexpected type.");
                return;
            }
            @SuppressWarnings("unchecked")
            Map<String, ?> assetMap = (Map<String, ?>) assetMapObj;

            int found = 0;
            for (Object v : assetMap.values()) {
                if (!(v instanceof Item)) continue;
                Item it = (Item) v;
                String id = safeGetItemId(it);
                Double value = extractLifestealFromItem(it);
                if (value != null) {
                    // Register into the registry for runtime use
                    LifestealRegistry.setItemLifesteal(id, value);
                    found++;
                }
            }
            System.out.println("[AssetLifestealLoader] populated lifesteal for " + found + " items");
        } catch (Throwable t) {
            System.out.println("[AssetLifestealLoader] unexpected error while populating lifesteal from assets:");
            t.printStackTrace();
        }
    }

    private static String safeGetItemId(Item item) {
        try {
            String id = item.getId();
            return (id != null) ? id : "unknown";
        } catch (Throwable ignored) {
            return "unknown";
        }
    }

    private static Double extractLifestealFromItem(Item item) {
        try {
            // 1) Try direct getters on Item
            for (String methodName : new String[] { "getLifesteal", "getLifeSteal", "getLifestealValue", "lifesteal" }) {
                try {
                    Method m = item.getClass().getMethod(methodName);
                    Object out = m.invoke(item);
                    Double d = toDouble(out);
                    if (d != null) return d;
                } catch (NoSuchMethodException ignore) {}
            }

            // 2) Try item.getConfig() -> fields or getters
            try {
                Method getConfig = item.getClass().getMethod("getConfig");
                Object cfg = getConfig.invoke(item);
                if (cfg != null) {
                    for (String methodName : new String[] { "getLifesteal", "getLifeSteal", "lifesteal" }) {
                        try {
                            Method cm = cfg.getClass().getMethod(methodName);
                            Object out = cm.invoke(cfg);
                            Double d = toDouble(out);
                            if (d != null) return d;
                        } catch (NoSuchMethodException ignore) {}
                    }
                    for (String fieldName : new String[] { "Lifesteal", "lifesteal", "lifeSteal" }) {
                        try {
                            Field f = cfg.getClass().getField(fieldName);
                            Object out = f.get(cfg);
                            Double d = toDouble(out);
                            if (d != null) return d;
                        } catch (NoSuchFieldException ignore) {}
                    }
                }
            } catch (NoSuchMethodException ignore) {}

            // 3) Try any Map-returning method on Item and look for keys
            for (Method mm : item.getClass().getMethods()) {
                if (mm.getParameterCount() != 0) continue;
                Class<?> rt = mm.getReturnType();
                if (Map.class.isAssignableFrom(rt)) {
                    try {
                        Object out = mm.invoke(item);
                        if (out instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> map = (Map<String, Object>) out;
                            for (String key : new String[] { "Lifesteal", "lifesteal", "lifeSteal" }) {
                                if (map.containsKey(key)) {
                                    Double d = toDouble(map.get(key));
                                    if (d != null) return d;
                                }
                            }
                        }
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable t) {
            // ignore and return null to allow fallback heuristics
        }
        return null;
    }

    private static Double toDouble(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(o));
        } catch (Exception ignored) {}
        return null;
    }
}