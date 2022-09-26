package com.pixelmongenerations.pkl;

import com.pixelmongenerations.pkl.reader.FileReader;
import com.pixelmongenerations.pkl.reader.GlbReader;
import com.pixelmongenerations.pkl.reader.InternalFileType;
import com.pixelmongenerations.pkl.scene.Scene;
import com.pixelmongenerations.pkl.scene.material.Texture;
import com.pixelmongenerations.pkl.scene.objects.Mesh;
import com.pixelmongenerations.rarecandy.animation.Animation;
import com.pixelmongenerations.rarecandy.components.AnimatedSolid;
import com.pixelmongenerations.rarecandy.components.RenderObject;
import com.pixelmongenerations.rarecandy.components.RenderObjects;
import com.pixelmongenerations.rarecandy.components.Solid;
import com.pixelmongenerations.rarecandy.pipeline.Pipeline;
import com.pixelmongenerations.rarecandy.rendering.Bone;
import com.pixelmongenerations.rarecandy.rendering.RareCandy;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.joml.Matrix4f;
import org.lwjgl.assimp.AIAnimation;
import org.lwjgl.assimp.AITexture;
import org.lwjgl.assimp.Assimp;
import org.tukaani.xz.XZ;
import org.tukaani.xz.XZInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Pixelmon Asset (.pk) file.
 */
public class PixelAsset {

    public final Scene scene;
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

    public PixelAsset(InputStream stream) {
        try {
            TarFile tarFile = getTarFile(stream);
            this.reader = findFormat(tarFile).reader;
            this.scene = this.reader.read(tarFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load scene", e);
        }
    }

    public RenderObject createStaticObject(Pipeline pipeline) {
        var objects = new RenderObjects();

        for (Mesh mesh : scene.meshes) {
            var renderObject = new Solid();
            renderObject.upload(mesh, pipeline, scene.textures);
            objects.add(renderObject);
        }

        return objects;
    }

    public AnimatedSolid createAnimatedObject(Pipeline pipeline) {
        var aiScene = ((GlbReader) reader).rawScene;
        if (aiScene.mNumAnimations() == 0) RareCandy.fatal("the imported file does not contain any animations. Assimp Error String: " + Assimp.aiGetErrorString());

        var mesh = scene.meshes.get(0);
        var bones = new Bone[mesh.getBones().length];
        var animations = new Animation[aiScene.mNumAnimations()];

        for (var i = 0; i < mesh.getBones().length; i++) {
            bones[i] = mesh.getBones()[i];
        }

        for (var i = 0; i < aiScene.mNumAnimations(); i++) {
            var aiAnim = AIAnimation.create(aiScene.mAnimations().get(i)); // Can't close as it's used in Animation later
            animations[i] = new Animation(aiAnim, bones, aiScene.mRootNode());
        }

        var rawTextures = new ArrayList<AITexture>();

        var pTextures = aiScene.mTextures();
        if (pTextures != null) {
            for (var i = 0; i < aiScene.mNumTextures(); i++) {
                rawTextures.add(AITexture.create(pTextures.get(i)));
            }
        } else {
            throw new RuntimeException("How do you expect us to render without textures? Use colours? we don't support that yet!");
        }

        var textures = new ArrayList<Texture>();
        for (var rawTexture : rawTextures) {
            if (rawTexture.mHeight() > 0) {
                throw new RuntimeException(".glb file had texture with height of 0");
            } else {
                textures.add(new Texture(rawTexture.pcDataCompressed(), rawTexture.mFilename().dataString()));
            }
        }

        var object = new AnimatedSolid(animations, new Matrix4f[bones.length]);
        object.upload(mesh, pipeline, textures);
        return object;
    }

    /**
     * We change 1 bit to make file readers fail to load the file or find its format. I would rather not have reforged digging through the assets honestly.
     */
    public static byte[] lockArchive(byte[] originalBytes) {
        originalBytes[0] = (byte) 6;
        return originalBytes;
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
}
