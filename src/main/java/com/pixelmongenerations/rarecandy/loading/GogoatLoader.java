package com.pixelmongenerations.rarecandy.loading;

import com.pixelmongenerations.pkl.PixelAsset;
import com.pixelmongenerations.pkl.reader.AssetType;
import com.pixelmongenerations.rarecandy.ThreadSafety;
import com.pixelmongenerations.rarecandy.components.RenderObject;
import com.pixelmongenerations.rarecandy.components.ObjectHolder;
import com.pixelmongenerations.rarecandy.settings.Settings;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.io.GltfModelReader;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class GogoatLoader {
    private final GltfModelReader reader = new GltfModelReader();
    private final ExecutorService modelLoadingPool;

    public GogoatLoader(Settings settings) {
        this.modelLoadingPool = Executors.newFixedThreadPool(settings.modelLoadingThreads());
    }

    public <T extends RenderObject> ObjectHolder<T> createObject(@NotNull Supplier<PixelAsset> asset, BiFunction<GltfModel, List<Runnable>, T> objectBuilder, Runnable onFinish) {
        var objects = new ObjectHolder<T>();
        var runnable = new ArrayList<Runnable>();
        modelLoadingPool.submit(ThreadSafety.wrapException(() -> {
            var model = read(asset.get());
            objects.add(objectBuilder.apply(model, runnable));
            if (onFinish != null) onFinish.run();

            ThreadSafety.runOnContextThread(() -> {
                runnable.forEach(Runnable::run);
            });
        }));
        return objects;
    }

    /**
     * Stops everything until the model is loaded. This is a horrible idea
     */
    public <T extends RenderObject> ObjectHolder<T> createObjectNow(@NotNull InputStream is, AssetType type, Function<PixelAsset, T> consumer) {
        var objects = new ObjectHolder<T>();
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
