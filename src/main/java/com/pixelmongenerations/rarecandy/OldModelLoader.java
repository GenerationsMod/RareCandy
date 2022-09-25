package com.pixelmongenerations.rarecandy;

import com.pixelmongenerations.pkl.scene.Scene;
import com.pixelmongenerations.pkl.scene.material.Texture;
import com.pixelmongenerations.rarecandy.components.AnimatedSolid;
import com.pixelmongenerations.rarecandy.pipeline.Pipeline;
import com.pixelmongenerations.rarecandy.rendering.Bone;
import org.joml.Matrix4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIAnimation;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AITexture;
import org.lwjgl.assimp.Assimp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Deprecated
public class OldModelLoader {

    @Deprecated
    public static AnimatedSolid loadAnimatedFile(Scene scene, AIScene aiScene, Pipeline pipeline) {
        if (aiScene.mNumAnimations() == 0) {
            System.err.println("the imported file does not contain any animations.");
            System.out.println(Assimp.aiGetErrorString());
            System.exit(0);
        }

        var mesh = scene.meshes.get(0);
        var inverseRootTransformation = scene.rootTransform;
        var bones = new Bone[mesh.getBones().length];

        for (int b = 0; b < mesh.getBones().length; b++) {
            bones[b] = mesh.getBones()[b];
        }

        AnimatedSolid object = new AnimatedSolid();

        List<AITexture> rawTextures = new ArrayList<>();

        // Retrieve Textures
        PointerBuffer pTextures = aiScene.mTextures();
        if (pTextures != null) {
            for (int i = 0; i < aiScene.mNumTextures(); i++) {
                rawTextures.add(AITexture.create(pTextures.get(i)));
            }
        } else {
            throw new RuntimeException("How do you expect us to render without textures? Use colours? we don't support that yet!");
        }

        // Try to load the textures into rosella
        List<Texture> textures = new ArrayList<>();
        for (AITexture rawTexture : rawTextures) {
            if (rawTexture.mHeight() > 0) {
                throw new RuntimeException(".glb file had texture with height of 0");
            } else {
                textures.add(new Texture(rawTexture.pcDataCompressed(), rawTexture.mFilename().dataString()));
            }
        }

        object.upload(mesh, pipeline, textures);
        object.animation = AIAnimation.create(Objects.requireNonNull(aiScene.mAnimations()).get(aiScene.mNumAnimations() - 1));
        object.bones = bones;
        object.boneTransforms = new Matrix4f[bones.length];
        object.root = aiScene.mRootNode();
        object.globalInverseTransform = inverseRootTransformation;
        return object;
    }
}
