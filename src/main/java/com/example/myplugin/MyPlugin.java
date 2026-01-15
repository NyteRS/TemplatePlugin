package com.example.myplugin;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import javax.annotation.Nonnull;
import com.example.myplugin.command.HelloCommand;


public class MyPlugin extends JavaPlugin {

    private static MyPlugin instance;

    public MyPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    public static MyPlugin get() {
        return instance;
    }

    @Override
    protected void setup() {
        instance = this;

        // Register components
        registerComponents();

        // Register systems
        registerSystems();

        // Register commands
        registerCommands();

        // Register events
        registerEvents();
        getCommandRegistry().registerCommand(new HelloCommand());

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

    private void registerComponents() {
        // Component registration here
    }

    private void registerSystems() {
        // System registration here
    }

    private void registerCommands() {
        // Adjust this line to match the actual method name you see on `this.`
    }


    private void registerEvents() {
        // Event registration here
    }
}