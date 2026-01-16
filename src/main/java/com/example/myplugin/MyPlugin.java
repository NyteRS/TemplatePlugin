package com.example.myplugin;

import com.example.myplugin.command.ReloadLifestealCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * MyPlugin (updated) — registers reload command and populates lifesteal on setup.
 *
 * Replace or merge this file with your existing MyPlugin implementation; if you already have a setup()
 * implementation, add the command registration line and the AssetLifestealLoader.populateFromAssets() call there.
 */
public class MyPlugin extends JavaPlugin {

    private static MyPlugin instance;

    public MyPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    public static MyPlugin get() { return instance; }

    @Override
    protected void setup() {
        instance = this;

        // Your existing setup logic (components, systems, etc.)
        // registerComponents();
        // registerSystems();
        // registerEvents();

        // Register the reload lifesteal command
        getCommandRegistry().registerCommand(new ReloadLifestealCommand());

        // Initialize DescriptionEditor (if your plugin uses it). Keep as in your repo or remove.


        // Populate lifesteal mapping now (assets should be loaded by the time plugin setup runs in most setups).
        // If assets are not yet available at this point in your server lifecycle, call AssetLifestealLoader.populateFromAssets()
        // later (e.g. on a post-asset-load event) or use the /reloadlifesteal command.
        try {
            AssetLifestealLoader.populateFromAssets();
        } catch (Throwable t) {
            System.out.println("[MyPlugin] warning: initial lifesteal population failed; use /reloadlifesteal after server finished loading assets.");
            t.printStackTrace();
        }

        System.out.println("MyPlugin setup complete!");
    }

    @Override
    protected void start() {
        System.out.println("MyPlugin started!");
    }

    @Override
    protected void shutdown() {
        System.out.println("MyPlugin shutting down!");
    }
}