package com.pg85.otg.forge.generator.structure;

import com.pg85.otg.common.LocalBiome;
import com.pg85.otg.configuration.biome.BiomeConfig.VillageType;
import com.pg85.otg.forge.biomes.ForgeBiome;
import com.pg85.otg.forge.world.ForgeWorld;
import com.pg85.otg.network.ServerConfigProvider;
import com.pg85.otg.util.minecraft.defaults.StructureNames;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.StructureStart;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OTGVillageGen extends OTGMapGenStructure
{
    /**
     * A list of all the biomes villages can spawn in.
     */
    public List<Biome> villageSpawnBiomes;

    /**
     * Village size, 0 for normal, 1 for flat map
     */
    private int size;
    private int distance;
    private int minimumDistance;

    public OTGVillageGen(ServerConfigProvider configs, ForgeWorld world)
    {
    	super(world);
        this.size = configs.getWorldConfig().villageSize;
        this.distance = configs.getWorldConfig().villageDistance;
        this.minimumDistance = 8;

        // Add all village biomes to the list
        this.villageSpawnBiomes = new ArrayList<Biome>();
        for (LocalBiome biome : configs.getBiomeArrayByOTGId())
        {
            if (biome == null)
                continue;
            if (biome.getBiomeConfig().villageType != VillageType.disabled)
            {
                this.villageSpawnBiomes.add(((ForgeBiome) biome).getHandle());
            }
        }
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ)
    {
        int var3 = chunkX;
        int var4 = chunkZ;

        if (chunkX < 0)
        {
            chunkX -= this.distance - 1;
        }

        if (chunkZ < 0)
        {
            chunkZ -= this.distance - 1;
        }

        int var5 = chunkX / this.distance;
        int var6 = chunkZ / this.distance;
        Random var7 = this.world.setRandomSeed(var5, var6, 10387312);
        var5 *= this.distance;
        var6 *= this.distance;
        var5 += var7.nextInt(this.distance - this.minimumDistance);
        var6 += var7.nextInt(this.distance - this.minimumDistance);

        if (var3 == var5 && var4 == var6)
        {
            boolean canSpawn = this.world.getBiomeProvider().areBiomesViable(var3 * 16 + 8, var4 * 16 + 8, 0, this.villageSpawnBiomes);

            if (canSpawn)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ)
    {
        return new OTGVillageStart(this.world, this.rand, chunkX, chunkZ, this.size);
    }

    @Override
    public String getStructureName()
    {
        return StructureNames.VILLAGE;
    }

	@Override
    public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean p_180706_3_)
    {
        this.world = worldIn;
        return findNearestStructurePosBySpacing(worldIn, this, pos, this.distance, 8, 10387312, false, 100, p_180706_3_);
    }
}
