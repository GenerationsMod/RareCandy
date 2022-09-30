package com.pixelmongenerations.rarecandy.loading;

import com.pixelmongenerations.pkl.reader.AssetReference;
import com.pixelmongenerations.pkl.PixelAsset;
import com.pixelmongenerations.rarecandy.components.MeshRenderObject;
import com.pixelmongenerations.rarecandy.components.RenderObjects;
import com.pixelmongenerations.rarecandy.pipeline.Pipeline;
import com.pixelmongenerations.rarecandy.rendering.RareCandy;
import com.pixelmongenerations.rarecandy.settings.Settings;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PKLoader {
    private final ExecutorService modelLoadingPool;
    private final Settings settings;
    private final RareCandy rareCandy;

    public PKLoader(Settings settings, RareCandy rareCandy) {
        this.settings = settings;
        this.rareCandy = rareCandy;
        this.modelLoadingPool = Executors.newFixedThreadPool(settings.modelLoadingThreads());
    }

    public <T extends MeshRenderObject> RenderObjects<T> createObject(@NotNull InputStream is, AssetReference.Type type, Pipeline pipeline) {
        var reference = new AssetReference(is, type);
        var objects = new RenderObjects<T>();
        modelLoadingPool.submit(() -> safe(() -> new PixelAsset(reference).upload(objects, pipeline, settings)));
        return objects;
    }

    private void safe(Runnable r) {
        try {
            r.run();
        } catch (Exception e) {
            rareCandy.throwExceptionLater(e);
        }
    }

    /**
     * Stops everything until the model is loaded. This is a horrible idea
     */
    public <T extends MeshRenderObject> RenderObjects<T> createObjectNow(@NotNull InputStream is, AssetReference.Type type, Pipeline pipeline) {
        var reference = new AssetReference(is, type);
        var objects = new RenderObjects<T>();
        var model = new PixelAsset(reference);
        model.upload(objects, pipeline, settings);
        objects.update();
        return objects;
    }

    public void close() {
         modelLoadingPool.shutdown();
    }
}
