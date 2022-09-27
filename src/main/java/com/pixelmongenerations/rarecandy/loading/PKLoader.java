package com.pixelmongenerations.rarecandy.loading;

import com.pixelmongenerations.pkl.reader.AssetReference;
import com.pixelmongenerations.pkl.PixelAsset;
import com.pixelmongenerations.rarecandy.components.MeshRenderObject;
import com.pixelmongenerations.rarecandy.components.RenderObjects;
import com.pixelmongenerations.rarecandy.pipeline.Pipeline;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PKLoader {
    private final ExecutorService modelLoadingPool;

    public PKLoader(int modelLoadingThreads) {
        this.modelLoadingPool = Executors.newFixedThreadPool(modelLoadingThreads);
    }

    public <T extends MeshRenderObject> RenderObjects<T> createObject(@NotNull InputStream is, AssetReference.Type type, Pipeline pipeline) {
        var reference = new AssetReference(is, type);
        var objects = new RenderObjects<T>();
        modelLoadingPool.submit(() -> new PixelAsset(reference).upload(objects, pipeline));
        return objects;
    }

    /**
     * Stops everything until the model is loaded. This is a horrible idea
     */
    public <T extends MeshRenderObject> RenderObjects<T> createObjectNow(@NotNull InputStream is, AssetReference.Type type, Pipeline pipeline) {
        var reference = new AssetReference(is, type);
        var objects = new RenderObjects<T>();
        var model = new PixelAsset(reference);
        model.upload(objects, pipeline);
        objects.update();
        return objects;
    }
}
