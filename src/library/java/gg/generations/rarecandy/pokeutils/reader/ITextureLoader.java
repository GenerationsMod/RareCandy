package gg.generations.rarecandy.pokeutils.reader;

import gg.generations.rarecandy.renderer.loading.ITexture;

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

    public abstract TextureReference generateDirectReference(String path);

    public void reload() {
        register("dark", generateDirectReference("dark.png"));
        register("neutral", generateDirectReference("neutral.png"));
        register("bright", generateDirectReference("bright.png"));
        register("paradox_mask", generateDirectReference("paradox.png"));
        register("blank", generateDirectReference("blank.png"));
        register("burnt_concrete", generateDirectReference("burnt_concrete.png"));
        register("concrete", generateDirectReference("concrete.png"));
        register("glass", generateDirectReference("glass.png"));
        register("metal", generateDirectReference("metal.png"));
        register("silver", generateDirectReference("silver.png"));


    }
    public void register(String name, TextureReference reference) {
        register(name, loadFromReference(reference));
    }

    protected abstract ITexture loadFromReference(TextureReference reference);


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
