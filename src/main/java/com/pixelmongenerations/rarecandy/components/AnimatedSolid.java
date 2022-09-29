package com.pixelmongenerations.rarecandy.components;

import com.pixelmongenerations.pkl.reader.TextureReference;
import com.pixelmongenerations.pkl.scene.objects.Mesh;
import com.pixelmongenerations.rarecandy.animation.Animation;
import com.pixelmongenerations.rarecandy.animation.ModelNode;
import com.pixelmongenerations.rarecandy.pipeline.Pipeline;
import com.pixelmongenerations.rarecandy.rendering.InstanceState;
import com.pixelmongenerations.rarecandy.rendering.VertexLayout;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;

import java.util.List;

public class AnimatedSolid extends MeshRenderObject {
    public final Animation[] animations;
    public final ModelNode node;
    public Matrix4f[] boneTransforms;
    public int activeAnimation;
    public double animationTime;

    public AnimatedSolid(Animation[] animations, ModelNode node) {
        this.animations = animations;
        this.boneTransforms = new Matrix4f[0];
        this.node = node;
    }

    @Override
    public void upload(Mesh mesh, Pipeline pipeline, List<TextureReference> diffuseTextures) {
        createUploadTask(
                mesh,
                pipeline,
                diffuseTextures,
                new VertexLayout.AttribLayout(3, GL11C.GL_FLOAT, "inPosition"),
                new VertexLayout.AttribLayout(2, GL11C.GL_FLOAT, "inTexCoords"),
                new VertexLayout.AttribLayout(3, GL11C.GL_FLOAT, "inNormal"),
                new VertexLayout.AttribLayout(3, GL11C.GL_FLOAT, "inTangent"),
                new VertexLayout.AttribLayout(4, GL11C.GL_FLOAT, "boneDataA"),
                new VertexLayout.AttribLayout(4, GL11C.GL_FLOAT, "boneDataB")
        );
    }

    @Override
    public void render(List<InstanceState> instances) {
        pipeline.bind();
        layout.bind();
        GL15C.glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, this.ebo);

        for (var instance : instances) {
            pipeline.updateUniforms(instance, this);
            GL11C.glDrawElements(GL11C.GL_TRIANGLES, this.indexCount, GL11C.GL_UNSIGNED_INT, 0);
        }
    }

    @Override
    public void update() {
        super.update();
        this.boneTransforms = animations[activeAnimation].getFrameTransform(animationTime, this.node);
    }
}
