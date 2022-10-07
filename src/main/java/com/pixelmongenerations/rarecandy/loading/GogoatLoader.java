package com.pixelmongenerations.rarecandy.loading;

import com.pixelmongenerations.pkl.PixelAsset;
import com.pixelmongenerations.pkl.reader.AssetType;
import com.pixelmongenerations.pkl.reader.TextureReference;
import com.pixelmongenerations.rarecandy.ThreadSafety;
import com.pixelmongenerations.rarecandy.components.*;
import com.pixelmongenerations.rarecandy.rendering.RareCandy;
import com.pixelmongenerations.rarecandy.settings.Settings;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.io.GltfModelReader;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

public class GogoatLoader {
    private final GltfModelReader reader = new GltfModelReader();
    private final ExecutorService modelLoadingPool;
    private final Settings settings;
    private final RareCandy rareCandy;

    public GogoatLoader(Settings settings, RareCandy rareCandy) {
        this.settings = settings;
        this.rareCandy = rareCandy;
        this.modelLoadingPool = Executors.newFixedThreadPool(settings.modelLoadingThreads());
    }

    public <T extends RenderObject> RenderObjects<T> createObject(@NotNull Supplier<InputStream> is, AssetType type, Function<PixelAsset, T> consumer) {
        var objects = new RenderObjects<T>();
        modelLoadingPool.submit(ThreadSafety.wrapException(() -> {
            var asset = new PixelAsset(is.get(), type);
            ThreadSafety.runOnContextThread(() -> objects.add(consumer.apply(asset)));
        }));
        return objects;
    }

    /**
     * Stops everything until the model is loaded. This is a horrible idea
     */
    public <T extends RenderObject> RenderObjects<T> createObjectNow(@NotNull InputStream is, AssetType type,  Function<PixelAsset, T> consumer) {
        var objects = new RenderObjects<T>();
        objects.add(consumer.apply(new PixelAsset(is, type)));
        return objects;
    }

    public void close() {
         modelLoadingPool.shutdown();
    }

    public GltfModel read(PixelAsset asset) {
        try {
            return reader.readWithoutReferences(new ByteArrayInputStream(asset.modelFile));
        } catch (IOException e) {
            throw new RuntimeException("", e);
        }
    }
}
