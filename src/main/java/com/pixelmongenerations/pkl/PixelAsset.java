package com.pixelmongenerations.pkl;

import com.pixelmongenerations.pkl.reader.AssetReference;
import com.pixelmongenerations.pkl.reader.GlbReader;
import com.pixelmongenerations.pkl.scene.Scene;
import com.pixelmongenerations.rarecandy.animation.Animation;
import com.pixelmongenerations.rarecandy.animation.CachedAnimation;
import com.pixelmongenerations.rarecandy.components.AnimatedSolid;
import com.pixelmongenerations.rarecandy.components.MeshRenderObject;
import com.pixelmongenerations.rarecandy.components.RenderObjects;
import com.pixelmongenerations.rarecandy.components.Solid;
import com.pixelmongenerations.rarecandy.pipeline.Pipeline;
import com.pixelmongenerations.rarecandy.settings.Settings;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.tukaani.xz.XZ;
import org.tukaani.xz.XZInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Pixelmon Asset (.pk) file.
 */
public class PixelAsset {
    public static final GlbReader READER = new GlbReader();
    public Scene scene;

    public PixelAsset(AssetReference reference) {
        try {
            switch (reference.type()) {
                case PK -> {
                    TarFile tarFile = getTarFile(reference.is());
                    this.scene = READER.read(tarFile);
                }

                case GLB -> this.scene = READER.read(reference.is());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load scene", e);
        }
    }

    public <T extends MeshRenderObject> void upload(RenderObjects<T> objects, Pipeline pipeline, Settings settings) {

        for (var mesh : scene.meshes) {
            T object = (T) new Solid();

            if (scene.gltf.getAnimationModels().size() > 0) {
                var animations = scene.gltf.getAnimationModels()
                        .stream()
                        .map(anim -> settings.preCacheAnimations() ? new CachedAnimation(anim, mesh.getBones()) : new Animation(anim, mesh.getBones()))
                        .toArray(Animation[]::new);

                object = (T) new AnimatedSolid(animations, scene.rootNode);
            }

            object.upload(mesh, pipeline, scene.textures);
            objects.add(object);
        }

        objects.allObjectsAdded = true;
    }

    private TarFile getTarFile(InputStream inputStream) {
        try {
            InputStream unlockedInputStream = unlockArchive(inputStream.readAllBytes());
            XZInputStream xzInputStream = new XZInputStream(unlockedInputStream);
            return new TarFile(xzInputStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file.", e);
        }
    }

    /**
     * We change 1 bit to make file readers fail to load the file or find its format. I would rather not have reforged digging through the assets, honestly.
     */
    private InputStream unlockArchive(byte[] originalBytes) {
        System.arraycopy(XZ.HEADER_MAGIC, 0, originalBytes, 0, XZ.HEADER_MAGIC.length);
        return new ByteArrayInputStream(originalBytes);
    }
}