package com.pg85.otg.bukkit.events;

import com.pg85.otg.bukkit.util.WorldHelper;
import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.exception.BiomeNotFoundException;
import com.pg85.otg.generator.resource.SaplingGen;
import com.pg85.otg.generator.resource.SaplingType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.BlockState;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.List;
import java.util.Random;

class SaplingListener
{
    void onStructureGrow(StructureGrowEvent event)
    {
        LocalWorld world = WorldHelper.toLocalWorld(event.getWorld());
        if (world == null)
        {
            return;
        }

        Location location = event.getLocation();

        // Query the OTG biome, if no sapling of the specified type is found 
        // and the biome has a replaceToBiome, query the parent biome.
        
        LocalBiome biome;
        try
        {
        	// Get the biome by OTG id
            biome = world.getBiome(location.getBlockX(), location.getBlockZ());
        }
        catch (BiomeNotFoundException e)
        {
            return;
        }

        BiomeConfig biomeConfig = biome.getBiomeConfig();
        
        // Get sapling type
        SaplingType saplingType = toSaplingType(event.getSpecies());
        
        if (saplingType == null && biomeConfig.inheritSaplingResource && biomeConfig.replaceToBiomeName != null && biomeConfig.replaceToBiomeName.trim().length() > 0)
        {
        	biome = null;
            try
            {
            	// Get the biome by saved id (parent biome)
                biome = world.getSavedBiome(location.getBlockX(), location.getBlockZ());
            } catch (BiomeNotFoundException e)
            {
                return;
            }
            biomeConfig = biome.getBiomeConfig();
            saplingType = toSaplingType(event.getSpecies());
        }
                  
        if(saplingType == null)
        {
            return;
        }

        // Adjust position for bigger saplings
        boolean wideTrunk = saplingType.requiresFourSaplings();
        if (wideTrunk)
        {
            Location lowestXZ = searchLowestXZLocation(location.getBlockY(), event.getBlocks());
            if (lowestXZ == null)
            {
                return;
            }
            location = lowestXZ;
        }

        // Optimistically remove the saplings.
        for (BlockState b : event.getBlocks()) {
            b.getBlock().setType(Material.AIR);
        }

        // Get generator
        SaplingGen sapling = biomeConfig.getSaplingGen(saplingType);
        if (sapling == null)
        {
            return;
        }

        // Try 10 times to spawn tree
        boolean success = false;
        Random random = new Random();
        for (int i = 0; i < 10; i++)
        {
            if (sapling.growSapling(world, random, wideTrunk, location.getBlockX(), location.getBlockY(), location.getBlockZ()))
            {
                success = true;
                break;
            }
        }

        if (success)
        {
            // Just spawned the tree, clear the blocks list to prevent
            // Bukkit spawning another tree
            event.getBlocks().clear();
        } else
        {
            // Cannot grow, so restore the saplings.
            event.setCancelled(true);
            for (BlockState b : event.getBlocks()) {
                b.update(true, false);
            }
        }
    }

    /**
     * Unfortunately, Bukkit provides no way to get the corner of 2x2 sapling
     * structures. (It just returns the location of the sapling the player
     * clicked on with bonemeal.) We need this position to properly spawn
     * custom objects. This method scans all blocks that are going to be
     * placed, and finds the block of type <code>LOG</code> or
     * <code>LOG_2</code> with the lowest x/z position.
     * 
     * @param y The y to search on. This is the y at which the sapling was
     *            placed.
     * @param blocks The blocks of the grown vanilla tree to search.
     * @return The lowest x/z location.
     */
    private Location searchLowestXZLocation(int y, List<BlockState> blocks)
    {
        BlockState lowestXZ = null;
        for (BlockState blockState : blocks)
        {
            // Ignore blocks at incorrect y
            if (blockState.getY() != y)
            {
                continue;
            }

            // Ignore blocks that are not a log
            if (blockState.getType() != Material.LOG && blockState.getType() != Material.LOG_2)
            {
                continue;
            }

            if (lowestXZ == null)
            {
                // Found a candidate
                lowestXZ = blockState;
                continue;
            }

            if (blockState.getX() <= lowestXZ.getX() && blockState.getZ() <= lowestXZ.getZ())
            {
                // Found a better candidate
                lowestXZ = blockState;
                continue;
            }
        }

        if (lowestXZ == null)
        {
            return null;
        } else
        {
            return lowestXZ.getLocation();
        }
    }

    private SaplingType toSaplingType(TreeType treeType)
    {
        switch (treeType)
        {
            case REDWOOD:
            case TALL_REDWOOD: // Both share the same sapling
                return SaplingType.Redwood;
            case BIRCH:
            case TALL_BIRCH:
                return SaplingType.Birch;
            case JUNGLE:
                return SaplingType.BigJungle;
            case SMALL_JUNGLE:
                return SaplingType.SmallJungle;
            case TREE:
            case BIG_TREE: // Both share the same sapling
                return SaplingType.Oak;
            case RED_MUSHROOM:
                return SaplingType.RedMushroom;
            case BROWN_MUSHROOM:
                return SaplingType.BrownMushroom;
            case ACACIA:
                return SaplingType.Acacia;
            case DARK_OAK:
                return SaplingType.DarkOak;
            case MEGA_REDWOOD:
                return SaplingType.HugeRedwood;
            default:
                return null;
        }
    }
}
