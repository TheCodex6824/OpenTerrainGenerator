package com.pg85.otg.generator.resource;

import com.pg85.otg.common.LocalMaterialData;
import com.pg85.otg.common.LocalWorld;
import com.pg85.otg.configuration.biome.BiomeConfig;
import com.pg85.otg.configuration.standard.PluginStandardValues;
import com.pg85.otg.exception.InvalidConfigException;
import com.pg85.otg.util.helpers.MaterialHelper;
import com.pg85.otg.util.helpers.RandomHelper;
import com.pg85.otg.util.minecraft.defaults.DefaultMaterial;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SmallLakeGen extends Resource
{
    private final boolean[] BooleanBuffer = new boolean[2048];
    private int maxAltitude;
    private int minAltitude;

    public SmallLakeGen(BiomeConfig biomeConfig, List<String> args) throws InvalidConfigException
    {
        super(biomeConfig);
        assureSize(5, args);
        material = readMaterial(args.get(0));
        frequency = readInt(args.get(1), 1, 100);
        rarity = readRarity(args.get(2));
        minAltitude = readInt(args.get(3), PluginStandardValues.WORLD_DEPTH,
                PluginStandardValues.WORLD_HEIGHT);
        maxAltitude = readInt(args.get(4), minAltitude,
                PluginStandardValues.WORLD_HEIGHT);
    }

    @Override
    public boolean equals(Object other)
    {
        if (!super.equals(other))
            return false;
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (getClass() != other.getClass())
            return false;
        final SmallLakeGen compare = (SmallLakeGen) other;

        return this.minAltitude == compare.minAltitude
               && this.maxAltitude == compare.maxAltitude
               && Arrays.equals(this.BooleanBuffer, compare.BooleanBuffer);
    }

    @Override
    public int getPriority()
    {
        return 1;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 41 * hash + super.hashCode();
        hash = 41 * hash + Arrays.hashCode(this.BooleanBuffer);
        hash = 41 * hash + this.minAltitude;
        hash = 41 * hash + this.maxAltitude;
        return hash;
    }

    @Override
    public String toString()
    {
        return "SmallLake(" + material + "," + frequency + "," + rarity + "," + minAltitude + "," + maxAltitude + ")";
    }

    @Override
    public void spawn(LocalWorld world, Random rand, boolean villageInChunk, int x, int z)
    {
        if (villageInChunk)
        {
            // Lakes and villages don't like each other.
            return;
        }

        x -= 8;
        z -= 8;

        int y = RandomHelper.numberInRange(rand, minAltitude, maxAltitude);

        // Search any free space
        while ((y > 5) && (world.isNullOrAir(x, y, z, false)))
        {
            y--;
        }

        if (y <= 4)
        {
            return;
        }

        // y = floor
        y -= 4;

        synchronized (BooleanBuffer)
        {
            LocalMaterialData air = MaterialHelper.toLocalMaterialData(DefaultMaterial.AIR, 0);
            boolean[] BooleanBuffer = new boolean[2048];
            int i = rand.nextInt(4) + 4;
            for (int j = 0; j < i; j++)
            {
                double d1 = rand.nextDouble() * 6.0D + 3.0D;
                double d2 = rand.nextDouble() * 4.0D + 2.0D;
                double d3 = rand.nextDouble() * 6.0D + 3.0D;

                double d4 = rand.nextDouble() * (16.0D - d1 - 2.0D) + 1.0D + d1 / 2.0D;
                double d5 = rand.nextDouble() * (8.0D - d2 - 4.0D) + 2.0D + d2 / 2.0D;
                double d6 = rand.nextDouble() * (16.0D - d3 - 2.0D) + 1.0D + d3 / 2.0D;

                for (int k = 1; k < 15; k++)
                {
                    for (int m = 1; m < 15; m++)
                    {
                        for (int n = 1; n < 7; n++)
                        {
                            double d7 = (k - d4) / (d1 / 2.0D);
                            double d8 = (n - d5) / (d2 / 2.0D);
                            double d9 = (m - d6) / (d3 / 2.0D);
                            double d10 = d7 * d7 + d8 * d8 + d9 * d9;
                            if (d10 >= 1.0D)
                            {
                                continue;
                            }
                            BooleanBuffer[((k * 16 + m) * 8 + n)] = true;
                        }
                    }
                }
            }
            int i1;
            int i2;
            for (int j = 0; j < 16; j++)
            {
                for (i1 = 0; i1 < 16; i1++)
                {
                    for (i2 = 0; i2 < 8; i2++)
                    {
                        boolean flag = (!BooleanBuffer[((j * 16 + i1) * 8 + i2)]) && (((j < 15) && (BooleanBuffer[(((j + 1) * 16 + i1) * 8 + i2)])) || ((j > 0) && (BooleanBuffer[(((j - 1) * 16 + i1) * 8 + i2)])) || ((i1 < 15) && (BooleanBuffer[((j * 16 + (i1 + 1)) * 8 + i2)])) || ((i1 > 0) && (BooleanBuffer[((j * 16 + (i1 - 1)) * 8 + i2)])) || ((i2 < 7) && (BooleanBuffer[((j * 16 + i1) * 8 + (i2 + 1))])) || ((i2 > 0) && (BooleanBuffer[((j * 16 + i1) * 8 + (i2 - 1))])));

                        if (flag)
                        {
                            LocalMaterialData localMaterialData = world.getMaterial(x + j, y + i2, z + i1, false);
                            if ((i2 >= 4) && (localMaterialData.isLiquid()))
                            {
                                return;
                            }
                            if ((i2 < 4) && (!localMaterialData.isSolid()) && !world.getMaterial(x + j, y + i2, z + i1, false).equals(material))
                            {
                                return;
                            }
                        }
                    }
                }
            }

            for (int j = 0; j < 16; j++)
            {
                for (i1 = 0; i1 < 16; i1++)
                {
                    for (i2 = 0; i2 < 4; i2++)
                    {
                        if (BooleanBuffer[((j * 16 + i1) * 8 + i2)])
                        {
                            world.setBlock(x + j, y + i2, z + i1, material, null, false);
                            BooleanBuffer[((j * 16 + i1) * 8 + i2)] = false;
                        }
                    }
                    for (i2 = 4; i2 < 8; i2++)
                    {
                        if (BooleanBuffer[((j * 16 + i1) * 8 + i2)])
                        {
                            world.setBlock(x + j, y + i2, z + i1, air, null, false);
                            BooleanBuffer[((j * 16 + i1) * 8 + i2)] = false;
                        }
                    }
                }
            }

        }
    }    
}
