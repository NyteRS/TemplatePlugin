package com.example.myplugin;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.entities.player.Player;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Lifesteal systems.
 *
 * - Uses Damage.getAmount() as the healed base.
 * - Default lifesteal: 12% for items categorized as "Dagger".
 * - Applies heal by adding to the Health stat via EntityStatMap.addStatValue(...)
 */
public final class LifestealSystems {

    private LifestealSystems() { /* utility */ }

    public static class LifestealOnDamage extends DamageEventSystem {

        // Fallback percentage for dagger items (12%).
        private static final double DEFAULT_DAGGER_LIFESTEAL = 0.12;

        public LifestealOnDamage() {
            super();
        }

        @Override
        public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Damage damageEvent) {
            // ignore cancelled or zero damage events
            try {
                if (damageEvent == null) return;
                if (damageEvent.isCancelled()) return;

                float damageAmount = damageEvent.getAmount();
                if (damageAmount <= 0f) return;

                Damage.Source source = damageEvent.getSource();
                if (!(source instanceof Damage.EntitySource)) return;

                Ref<EntityStore> attackerRef = ((Damage.EntitySource) source).getRef();
                if (attackerRef == null || !attackerRef.isValid()) return;

                // Get the attacker entity (helper available in repo)
                @Nullable Entity ent = EntityUtils.getEntity(attackerRef, store);
                if (!(ent instanceof LivingEntity)) return;

                LivingEntity attacker = (LivingEntity) ent;

                // Attempt to get the held item (hotbar active item / item in hand)
                ItemStack held = getHeldItem(attacker);
                if (held == null || ItemStack.isEmpty(held)) return;

                double lifesteal = getLifestealFromItem(held);
                if (lifesteal <= 0.0) return;

                double heal = damageAmount * lifesteal;
                if (heal <= 0.0) return;

                // Apply heal to attacker's health stat via EntityStatMap
                EntityStatMap statMap = (EntityStatMap) store.getComponent(attackerRef, EntityStatMap.getComponentType());
                if (statMap == null) return;

                // AddStatValue expects a float; ensure we don't overshoot with a huge number
                float healFloat = (float) heal;
                statMap.addStatValue(DefaultEntityStatTypes.getHealth(), healFloat);

            } catch (Throwable t) {
                // log if you want (avoid throwing from an event handler)
                com.hypixel.hytale.server.npc.NPCPlugin.get().getLogger().at(java.util.logging.Level.WARNING).withCause(t).log("LifestealOnDamage handler error");
            }
        }

        // Helper: obtain the attacker held item: prefer active hotbar item, fall back to item-in-hand.
        @Nullable
        private ItemStack getHeldItem(@Nonnull LivingEntity attacker) {
            try {
                // Many entity implementations expose getInventory(); Player and NPC entity types do.
                if (attacker instanceof Player) {
                    Player p = (Player) attacker;
                    // Example methods in template: getInventory().getActiveHotbarItem(), getInventory().getItemInHand()
                    // Use whichever is present in the runtime; below we try common names.
                    try {
                        return p.getInventory().getActiveHotbarItem();
                    } catch (NoSuchMethodError | NoClassDefFoundError e) {
                        // fallback
                    }
                    try {
                        return p.getInventory().getItemInHand();
                    } catch (NoSuchMethodError | NoClassDefFoundError e) {
                        // fallback
                    }
                } else {
                    // Generic LivingEntity: try getInventory().getActiveHotbarItem() then getItemInHand()
                    try {
                        return attacker.getInventory().getActiveHotbarItem();
                    } catch (NoSuchMethodError e) {}
                    try {
                        return attacker.getInventory().getItemInHand();
                    } catch (NoSuchMethodError e) {}
                }
            } catch (Throwable ignored) {}
            return null;
        }

        // Determine lifesteal percent from the item asset (fallback for daggers).
        // Currently: if the item has category "Dagger" (case-insensitive), returns DEFAULT_DAGGER_LIFESTEAL.
        // You can extend this to read a custom attribute from item metadata or asset later.
        private double getLifestealFromItem(@Nonnull ItemStack itemStack) {
            if (itemStack == null) return 0.0;
            Item item = itemStack.getItem();
            if (item == null) return 0.0;

            // Check categories for "Dagger"
            String[] cats = item.getCategories();
            if (cats != null) {
                for (String c : cats) {
                    if (c != null && c.equalsIgnoreCase("Dagger")) {
                        return DEFAULT_DAGGER_LIFESTEAL;
                    }
                }
            }

            // As a last simple heuristic: check item id containing "dagger"
            String itemId = itemStack.getItemId();
            if (itemId != null && itemId.toLowerCase().contains("dagger")) {
                return DEFAULT_DAGGER_LIFESTEAL;
            }

            // No lifesteal configured
            return 0.0;
        }
    }
}