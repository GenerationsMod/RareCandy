package com.pixelmongenerations.rarecandy.loading;

import com.pixelmongenerations.pkl.reader.AssetReference;
import com.pixelmongenerations.pkl.PixelAsset;
import com.pixelmongenerations.rarecandy.animation.Animation;
import com.pixelmongenerations.rarecandy.animation.CachedAnimation;
import com.pixelmongenerations.rarecandy.components.AnimatedSolid;
import com.pixelmongenerations.rarecandy.components.MeshRenderObject;
import com.pixelmongenerations.rarecandy.components.RenderObjects;
import com.pixelmongenerations.rarecandy.components.Solid;
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
        modelLoadingPool.submit(() -> safe(() -> upload(new PixelAsset(reference), objects, pipeline, settings)));
        return objects;
    }

    /**
     * Stops everything until the model is loaded. This is a horrible idea
     */
    public <T extends MeshRenderObject> RenderObjects<T> createObjectNow(@NotNull InputStream is, AssetReference.Type type, Pipeline pipeline) {
        var reference = new AssetReference(is, type);
        var objects = new RenderObjects<T>();
        var model = new PixelAsset(reference);
        upload(model, objects, pipeline, settings);
        objects.update();
        return objects;
    }

    public <T extends MeshRenderObject> void upload(PixelAsset asset, RenderObjects<T> objects, Pipeline pipeline, Settings settings) {

        for (var mesh : asset.scene.meshes) {
            T object = (T) new Solid();

            if (asset.scene.model.getAnimationModels().size() > 100) {
                var animations = asset.scene.model.getAnimationModels()
                        .stream()
                        .map(anim -> settings.preCacheAnimations() ? new CachedAnimation(anim, mesh.getBones()) : new Animation(anim, mesh.getBones()))
                        .toArray(Animation[]::new);

                object = (T) new AnimatedSolid(animations, asset.scene.rootNode);
            }

            object.upload(mesh, pipeline, asset.scene.textures);
            objects.add(object);
        }

        objects.allObjectsAdded = true;
    }

    private void safe(Runnable r) {
        try {
            r.run();
        } catch (Exception e) {
            rareCandy.throwExceptionLater(e);
        }
    }

    public void close() {
         modelLoadingPool.shutdown();
    }
}
