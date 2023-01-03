package com.pokemod.pokeutils;

public class GlbPixelAsset extends PixelAsset{

    private final byte[] glbFile;

    public GlbPixelAsset(byte[] glbFile) {
        super("unknown", glbFile);
        this.glbFile = glbFile;
    }

    @Override
    public byte[] getModelFile() {
        return glbFile;
    }
}
