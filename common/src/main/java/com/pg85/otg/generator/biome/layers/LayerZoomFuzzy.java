package com.pg85.otg.generator.biome.layers;

public class LayerZoomFuzzy extends LayerZoom
{

    LayerZoomFuzzy(long seed, Layer childLayer)
    {
        super(seed, childLayer);
    }

    @Override
    protected int getRandomOf4(int a, int b, int c, int d)
    {
        return this.getRandomInArray(a, b, c, d);
    }

}