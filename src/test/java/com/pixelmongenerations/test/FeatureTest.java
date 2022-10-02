package com.pixelmongenerations.test;

import com.pixelmongenerations.pkl.reader.AssetType;
import com.pixelmongenerations.rarecandy.components.MeshObject;
import com.pixelmongenerations.rarecandy.components.RenderObjects;
import com.pixelmongenerations.rarecandy.rendering.RareCandy;
import org.joml.Matrix4f;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class FeatureTest {
    public final String id;
    public final String description;

    protected FeatureTest(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public abstract void init(RareCandy scene, Matrix4f viewMatrix);

    public abstract void update(RareCandy scene, double deltaTime);

    protected RenderObjects<MeshObject> loadStaticModel(RareCandy renderer, String name) {
        var loader = renderer.getLoader();
        return loader.createObject(
                () -> FeatureTest.class.getResourceAsStream("/new/" + name + ".pk"),
                AssetType.PK,
                asset -> {
                    var gltfModel = loader.read(asset);
                    return MeshObject.create(gltfModel, Pipelines.staticPipeline(() -> FeatureTester.PROJECTION_MATRIX));
                }
        );
    }

    /*protected RenderObjects<AnimatedMeshObject> loadAnimatedModel(RareCandy renderer, String name) {
        return renderer.getLoader().createObject(Objects.requireNonNull(FeatureTest.class.getResourceAsStream("/new/" + name + ".pk"), "Failed to read /" + name + ".pk"), AssetType.PK, Pipelines.animatedPipeline(() -> FeatureTester.PROJECTION_MATRIX));
    }*/
}
