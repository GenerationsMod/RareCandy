package com.pixelmongenerations.pkl;

import com.pixelmongenerations.pkl.reader.FileReader;
import com.pixelmongenerations.pkl.reader.GlbReader;
import com.pixelmongenerations.pkl.reader.InternalFileType;
import com.pixelmongenerations.pkl.scene.Scene;
import com.pixelmongenerations.rarecandy.animation.Animation;
import com.pixelmongenerations.rarecandy.components.AnimatedSolid;
import com.pixelmongenerations.rarecandy.components.RenderObjects;
import com.pixelmongenerations.rarecandy.components.Solid;
import com.pixelmongenerations.rarecandy.pipeline.Pipeline;
import com.pixelmongenerations.rarecandy.rendering.Bone;
import com.pixelmongenerations.rarecandy.rendering.RareCandy;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.lwjgl.assimp.AIAnimation;
import org.lwjgl.assimp.Assimp;
import org.tukaani.xz.XZ;
import org.tukaani.xz.XZInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Pixelmon Asset (.pk) file.
 */
@SuppressWarnings("unused") // This is an API so it has unused methods
public class PixelAsset {

    public Scene scene;
    public FileReader reader;

    public PixelAsset(Path path) {
        if (!path.getFileName().toString().endsWith(".pk")) {
            System.err.println("It is recommended you name all Pixelmon Asset files with .pk");
        }

        try {
            TarFile tarFile = getTarFile(path);
            this.scene = findFormat(tarFile).reader.read(tarFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load scene", e);
        }
    }

    public PixelAsset(InputStream stream, Type assetType) {
        try {
            switch (assetType) {
                case PK -> {
                    TarFile tarFile = getTarFile(stream);
                    this.reader = findFormat(tarFile).reader;
                    this.scene = this.reader.read(tarFile);
                }

                case GLB -> {
                    this.reader = new GlbReader();
                    this.scene = ((GlbReader) this.reader).read(stream);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load scene", e);
        }
    }

    public RenderObjects<Solid> createStaticObject(Pipeline pipeline) {
        var objects = new RenderObjects<Solid>();

        for (var mesh : scene.meshes) {
            var renderObject = new Solid();
            renderObject.upload(mesh, pipeline, scene.textures);
            objects.add(renderObject);
        }

        return objects;
    }

    public RenderObjects<AnimatedSolid> createAnimatedObject(Pipeline pipeline) {
        var aiScene = ((GlbReader) reader).rawScene;
        if (aiScene.mNumAnimations() == 0) RareCandy.fatal("the imported file does not contain any animations. Assimp Error String: " + Assimp.aiGetErrorString());
        var objects = new RenderObjects<AnimatedSolid>();

        for (var mesh : scene.meshes) {
            var bones = new Bone[mesh.getBones().length];
            var animations = new Animation[aiScene.mNumAnimations()];

            for (var i = 0; i < mesh.getBones().length; i++) {
                bones[i] = mesh.getBones()[i];
            }

            for (var i = 0; i < aiScene.mNumAnimations(); i++) {
                var aiAnim = AIAnimation.create(aiScene.mAnimations().get(i)); // Can't close as it's used in Animation later
                animations[i] = new Animation(aiAnim, bones, aiScene.mRootNode());
            }

            var object = new AnimatedSolid(animations);
            object.upload(mesh, pipeline, scene.textures);
            objects.add(object);
        }

        return objects;
    }

    private InternalFileType findFormat(TarFile file) {
        InternalFileType type = null;
        for (TarArchiveEntry entry : file.getEntries()) {
            String name = entry.getName();

            if (name.endsWith(".glb")) {
                type = InternalFileType.GRAPHICS_LANGUAGE_BINARY;
            }

            if (name.endsWith(".gltf")) {
                type = InternalFileType.GRAPHICS_LANGUAGE_JSON;
            }
        }

        return type;
    }

    private TarFile getTarFile(Path path) {
        try {
            return getTarFile(Files.newInputStream(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file.", e);
        }
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

    public enum Type {
        GLB, PK
    }
}
