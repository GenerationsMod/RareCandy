package com.pixelmongenerations.test;

import com.pixelmongenerations.pixelmonassetutils.PixelAsset;
import com.pixelmongenerations.pixelmonassetutils.reader.GlbReader;
import com.pixelmongenerations.rarecandy.AnimationUtil;
import com.pixelmongenerations.rarecandy.components.RenderObject;
import com.pixelmongenerations.rarecandy.components.StaticRenderObject;
import com.pixelmongenerations.rarecandy.rendering.RenderScene;
import org.joml.Matrix4f;

import java.util.Objects;

public abstract class FeatureTest {
    public final String id;
    public final String description;

    protected FeatureTest(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public abstract void init(RenderScene scene, Matrix4f viewMatrix);

    public abstract void update(RenderScene scene);

    protected RenderObject loadStaticModel(String name) {
        PixelAsset model = new PixelAsset(Objects.requireNonNull(FeatureTest.class.getResourceAsStream("/" + name + ".pk"), "Failed to read /" + name + ".pk"));
        return StaticRenderObject.loadStaticFile(model.scene, ((GlbReader) model.reader).rawScene, 0);
    }

    protected RenderObject loadAnimatedModel(String name) {
        PixelAsset model = new PixelAsset(Objects.requireNonNull(FeatureTest.class.getResourceAsStream("/" + name + ".pk"), "Failed to read /" + name + ".pk"));
        return AnimationUtil.loadAnimatedFile(model.scene, ((GlbReader) model.reader).rawScene);
    }
}
