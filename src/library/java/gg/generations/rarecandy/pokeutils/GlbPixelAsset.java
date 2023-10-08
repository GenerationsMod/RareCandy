package gg.generations.rarecandy.pokeutils;

public class GlbPixelAsset extends PixelAsset {

    private final byte[] glbFile;

    public GlbPixelAsset(String name, byte[] glbFile) {
        super(name, glbFile);
        this.glbFile = glbFile;
    }

    @Override
    public byte[] getModelFile() {
        return glbFile;
    }
}
