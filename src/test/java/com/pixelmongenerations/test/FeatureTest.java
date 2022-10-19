package com.pixelmongenerations.test;

import com.pixelmongenerations.pkl.PixelAsset;
import com.pixelmongenerations.pkl.reader.AssetType;
import com.pixelmongenerations.rarecandy.components.AnimatedMeshObject;
import com.pixelmongenerations.rarecandy.components.MeshObject;
import com.pixelmongenerations.rarecandy.components.RenderObjects;
import com.pixelmongenerations.rarecandy.rendering.RareCandy;
import org.joml.Matrix4f;

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
        /*var loader = renderer.getLoader();
        return loader.createObject(
                () -> FeatureTest.class.getResourceAsStream("/new/" + name + ".pk"),
                asset -> {
                    var gltfModel = loader.read(asset);
                    return MeshObject.create(gltfModel, Pipelines.staticPipeline(() -> FeatureTester.PROJECTION_MATRIX));
                }
        );*/
        throw new RuntimeException("Tmp broken");
    }

    protected RenderObjects<AnimatedMeshObject> loadAnimatedModel(RareCandy renderer, String name) {
        var loader = renderer.getLoader();
        return loader.createObject(
                () -> new PixelAsset(FeatureTest.class.getResourceAsStream("/new/" + name + ".pk"), AssetType.PK),
                asset -> (AnimatedMeshObject) MeshObject.create(asset, Pipelines.animatedPipeline(() -> FeatureTester.PROJECTION_MATRIX))
        );
    }
}
