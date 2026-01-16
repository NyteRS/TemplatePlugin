package com.example.myplugin.command;

import com.example.myplugin.AssetLifestealLoader;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * /reloadlifesteal - command to re-scan item assets and update runtime lifesteal values.
 */
public class ReloadLifestealCommand extends AbstractPlayerCommand {
    public ReloadLifestealCommand() {
        super("reloadlifesteal", "Reload lifesteal values from item assets");
    }

    @Override
    protected void execute(@Nonnull CommandContext context,
                           @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref,
                           @Nonnull PlayerRef playerRef,
                           @Nonnull World world) {
        AssetLifestealLoader.populateFromAssets();
        com.hypixel.hytale.server.core.entity.entities.Player playerComponent =
                (com.hypixel.hytale.server.core.entity.entities.Player) store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
        if (playerComponent != null) {
            playerComponent.sendMessage(com.hypixel.hytale.server.core.Message.raw("Reloaded lifesteal values from assets (see server log)."));
        }
    }
}