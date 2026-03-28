package com.crouchgrow;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bamboo;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.Set;

public class CrouchGrowListener implements Listener {

    private final CrouchGrow plugin;

    // Blocks that can be bone-mealed in vanilla Minecraft 1.21.x
    private static final Set<Material> BONEMEALABLE = Set.of(
            Material.WHEAT,
            Material.CARROTS,
            Material.POTATOES,
            Material.BEETROOTS,
            Material.MELON_STEM,
            Material.PUMPKIN_STEM,
            Material.TORCHFLOWER_CROP,
            Material.PITCHER_CROP,
            Material.NETHER_WART,
            Material.COCOA,
            Material.SWEET_BERRY_BUSH,
            Material.KELP,
            Material.BAMBOO_SAPLING,
            Material.BAMBOO,
            Material.CAVE_VINES,
            Material.CAVE_VINES_PLANT,
            Material.TWISTING_VINES,
            Material.TWISTING_VINES_PLANT,
            Material.WEEPING_VINES,
            Material.WEEPING_VINES_PLANT,
            Material.OAK_SAPLING,
            Material.SPRUCE_SAPLING,
            Material.BIRCH_SAPLING,
            Material.JUNGLE_SAPLING,
            Material.ACACIA_SAPLING,
            Material.DARK_OAK_SAPLING,
            Material.CHERRY_SAPLING,
            Material.MANGROVE_PROPAGULE,
            Material.SHORT_GRASS,
            Material.TALL_GRASS,
            Material.FERN,
            Material.LARGE_FERN,
            Material.DANDELION,
            Material.POPPY,
            Material.BLUE_ORCHID,
            Material.ALLIUM,
            Material.AZURE_BLUET,
            Material.RED_TULIP,
            Material.ORANGE_TULIP,
            Material.WHITE_TULIP,
            Material.PINK_TULIP,
            Material.OXEYE_DAISY,
            Material.CORNFLOWER,
            Material.LILY_OF_THE_VALLEY,
            Material.SUNFLOWER,
            Material.LILAC,
            Material.ROSE_BUSH,
            Material.PEONY,
            Material.PITCHER_PLANT,
            Material.TORCHFLOWER,
            Material.MOSS_BLOCK,
            Material.GRASS_BLOCK,
            Material.MYCELIUM,
            Material.SEAGRASS,        // fixed: was SEA_GRASS
            Material.TALL_SEAGRASS,
            Material.BROWN_MUSHROOM,
            Material.RED_MUSHROOM,
            Material.CRIMSON_FUNGUS,
            Material.WARPED_FUNGUS
    );

    public CrouchGrowListener(CrouchGrow plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        // Only trigger when the player starts sneaking (crouch down)
        if (!event.isSneaking()) return;

        Player player = event.getPlayer();

        // Check permission
        if (!player.hasPermission("crouchgrow.use")) return;

        int radius = plugin.getGrowRadius();

        // Centre of the scan: the block the player is standing on (or feet block)
        Block origin = player.getLocation().getBlock();
        int originX = origin.getX();
        int originY = origin.getY();
        int originZ = origin.getZ();

        boolean anyGrew = false;

        // Scan a (2*radius+1) x (2*radius+1) column around the player.
        // For each XZ position check the feet layer and the one below it.
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Block feet    = origin.getWorld().getBlockAt(originX + dx, originY,     originZ + dz);
                Block beneath = origin.getWorld().getBlockAt(originX + dx, originY - 1, originZ + dz);

                Block target = null;
                if (BONEMEALABLE.contains(feet.getType())) {
                    target = feet;
                } else if (BONEMEALABLE.contains(beneath.getType())) {
                    target = beneath;
                }

                if (target != null && applyBoneMeal(target)) {
                    anyGrew = true;
                }
            }
        }

        // Play a single sound at the player's position if anything grew
        if (anyGrew) {
            player.getWorld().playSound(
                    player.getLocation(),
                    org.bukkit.Sound.ITEM_BONE_MEAL_USE,
                    1.0f,
                    1.0f
            );
        }
    }

    /**
     * Attempts to advance the growth stage of a block by one step,
     * mirroring what a bone meal application does in vanilla.
     * Spawns happy-villager particles on success.
     *
     * @return true if the block's age was advanced
     */
    private boolean applyBoneMeal(Block block) {
        BlockData data = block.getBlockData();

        // Special case: Bamboo uses its own BlockData type
        if (data instanceof Bamboo bamboo) {
            // Bamboo bone-meal just adds a block on top; skip age logic
            // and let vanilla handle it via a block-grow event simulation.
            // We simply nudge the age if it has one, otherwise return false.
            return false;
        }

        // Most growable blocks implement Ageable
        if (data instanceof Ageable ageable) {
            int current = ageable.getAge();
            int max     = ageable.getMaximumAge();

            if (current >= max) return false; // already fully grown

            // Advance by a random 2–5 stages (vanilla bone meal behaviour)
            int advance = 2 + plugin.getRandom().nextInt(4);
            ageable.setAge(Math.min(current + advance, max));
            block.setBlockData(ageable);

            spawnParticles(block);
            return true;
        }

        return false;
    }

    private void spawnParticles(Block block) {
        block.getWorld().spawnParticle(
                org.bukkit.Particle.HAPPY_VILLAGER,
                block.getLocation().add(0.5, 0.5, 0.5),
                6,
                0.3, 0.3, 0.3,
                0
        );
    }
}
