package gg.generations.rarecandy.arceus.model.pk;

import gg.generations.rarecandy.legacy.pipeline.ITexture;
import gg.generations.rarecandy.legacy.pipeline.TextureReference;

import java.awt.image.BufferedImage;
import java.util.Set;

public abstract class ITextureLoader {
    private static ITextureLoader instance;

    public static ITextureLoader instance() {
        return instance;
    }

    public static void setInstance(ITextureLoader instance) {
        ITextureLoader.instance = instance;
    }

    public abstract ITexture getTexture(String name);

    public abstract void register(String name, ITexture reference);

    public abstract void remove(String name);

    public void clear() {
        getTextureEntries().forEach(this::remove);
        reload();
    }

    public abstract ITexture generateDirectReference(String path);

    public void reload() {
        register("dark", generateDirectReference("dark.png"));
        register("neutral", generateDirectReference("neutral.png"));
        register("bright", generateDirectReference("bright.png"));
        register("paradox_mask", generateDirectReference("paradox_mask_tiled.png"));
        register("blank", generateDirectReference("blank.png"));
        register("burnt_concrete", generateDirectReference("burnt_concrete.png"));
        register("concrete", generateDirectReference("concrete.png"));
        register("glass", generateDirectReference("glass.png"));
        register("metal", generateDirectReference("metal.png"));
        register("silver", generateDirectReference("silver.png"));


    }

    protected ITexture loadFromReference(BufferedImage reference) {
        return TextureReference.of(reference);
    }

    public void register(String name, BufferedImage reference) {
        register(name, loadFromReference(reference));
    }


    public ITexture getDarkFallback() {
        return getTexture("dark");
    }

    public ITexture getBrightFallback() {
        return getTexture("neutral");
    }
    public ITexture getNuetralFallback() {
        return getTexture("bright");
    }

    public abstract Set<String> getTextureEntries();
}
