package com.pixelmongenerations.rarecandy.components;

import com.pixelmongenerations.pkl.scene.material.Material;
import com.pixelmongenerations.pkl.scene.material.Texture;
import com.pixelmongenerations.pkl.scene.objects.Mesh;
import com.pixelmongenerations.rarecandy.animation.Animation;
import com.pixelmongenerations.rarecandy.core.VertexLayout;
import com.pixelmongenerations.rarecandy.pipeline.Pipeline;
import com.pixelmongenerations.rarecandy.rendering.InstanceState;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;

import java.util.List;

public class AnimatedSolid extends MeshRenderObject {
    public final Animation[] animations;
    public Matrix4f[] boneTransforms;
    public int activeAnimation;
    public double animationTime;
    private VertexLayout layout;

    public AnimatedSolid(Animation[] animations, Matrix4f[] boneTransforms) {
        this.animations = animations;
        this.boneTransforms = boneTransforms;
    }

    @Override
    public void upload(Mesh mesh, Pipeline pipeline, List<Texture> diffuseTextures) {
        super.upload(mesh, pipeline, diffuseTextures);

        layout = new VertexLayout(
                vao,
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

    public Material getMaterial(String materialId) {
        return variants.getOrDefault(materialId, materials.get(0));
    }

    @Override
    public void update() {
        this.boneTransforms = animations[activeAnimation].getTransformsForFrame(animationTime);
    }
}
