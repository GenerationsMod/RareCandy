package com.pixelmongenerations.test;

import com.pixelmongenerations.pkl.reader.AssetReference;
import com.pixelmongenerations.rarecandy.components.AnimatedSolid;
import com.pixelmongenerations.rarecandy.components.RenderObjects;
import com.pixelmongenerations.rarecandy.components.Solid;
import com.pixelmongenerations.rarecandy.rendering.RareCandy;
import org.joml.Matrix4f;

import java.util.Objects;

public abstract class FeatureTest {
    public final String id;
    public final String description;

    protected FeatureTest(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public abstract void init(RareCandy scene, Matrix4f viewMatrix);

    public abstract void update(RareCandy scene, double deltaTime);

    protected RenderObjects<Solid> loadStaticModel(RareCandy renderer, String name) {
        return renderer.getLoader().createObject(Objects.requireNonNull(FeatureTest.class.getResourceAsStream("/new/" + name + ".pk"), "Failed to read /" + name + ".pk"), AssetReference.Type.PK, Pipelines.staticPipeline(() -> FeatureTester.PROJECTION_MATRIX));
    }

    protected RenderObjects<AnimatedSolid> loadAnimatedModel(RareCandy renderer, String name) {
        return renderer.getLoader().createObject(Objects.requireNonNull(FeatureTest.class.getResourceAsStream("/new/" + name + ".pk"), "Failed to read /" + name + ".pk"), AssetReference.Type.PK, Pipelines.animatedPipeline(() -> FeatureTester.PROJECTION_MATRIX));
    }
}
