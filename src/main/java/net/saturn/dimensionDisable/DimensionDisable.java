package net.saturn.dimensionDisable;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class DimensionDisable extends JavaPlugin {

    private static DimensionDisable instance;
    private boolean netherDisabled;
    private boolean endDisabled;

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        loadSettings();

        getServer().getPluginManager().registerEvents(new DimensionListener(this), this);

        // Paper plugins must register commands via the lifecycle API, not getCommand()
        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            new DimensionCommand(this).register(commands);
        });

        getLogger().info("DimensionDisable enabled! Nether disabled: " + netherDisabled + ", End disabled: " + endDisabled);
    }

    @Override
    public void onDisable() {
        getLogger().info("DimensionDisable disabled.");
    }

    public void loadSettings() {
        reloadConfig();
        FileConfiguration config = getConfig();
        netherDisabled = config.getBoolean("disable-nether", false);
        endDisabled = config.getBoolean("disable-end", false);
    }

    public boolean isNetherDisabled() {
        return netherDisabled;
    }

    public boolean isEndDisabled() {
        return endDisabled;
    }

    public void setNetherDisabled(boolean value) {
        netherDisabled = value;
        getConfig().set("disable-nether", value);
        saveConfig();
    }

    public void setEndDisabled(boolean value) {
        endDisabled = value;
        getConfig().set("disable-end", value);
        saveConfig();
    }

    public static DimensionDisable getInstance() {
        return instance;
    }
}