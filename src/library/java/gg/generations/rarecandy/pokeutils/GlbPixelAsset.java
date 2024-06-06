package gg.generations.rarecandy.pokeutils;

public class GlbPixelAsset extends PixelAsset {

    private final byte[] glbFile;

    public GlbPixelAsset(String name, byte[] glbFile, ModelConfig config) {
        super(name, glbFile);
        this.glbFile = glbFile;
        this.config = config;
    }

    @Override
    public byte[] getModelFile() {
        return glbFile;
    }
}
