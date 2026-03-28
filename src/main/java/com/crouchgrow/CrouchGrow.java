package com.crouchgrow;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.Random;

public class CrouchGrow extends JavaPlugin {

    private final Random random = new Random();

    public Random getRandom() { return random; }

    @Override
    public void onEnable() {
        // Save default config.yml if it doesn't exist
        saveDefaultConfig();

        // Validate the radius value on startup
        int radius = getConfig().getInt("radius", 1);
        if (radius < 0) {
            getLogger().warning("Invalid radius value (" + radius + ") in config.yml — resetting to default (1).");
            getConfig().set("radius", 1);
            saveConfig();
        }

        getLogger().info("CrouchGrow enabled! Grow radius: " + getConfig().getInt("radius", 1));
        getServer().getPluginManager().registerEvents(new CrouchGrowListener(this), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("CrouchGrow has been disabled!");
    }

    /** Returns the half-extent radius (0 = 1x1, 1 = 3x3, 2 = 5x5, etc.) */
    public int getGrowRadius() {
        return getConfig().getInt("radius", 1);
    }
}
