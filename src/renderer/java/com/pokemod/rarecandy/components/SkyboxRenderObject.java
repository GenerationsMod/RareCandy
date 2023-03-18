package com.pokemod.rarecandy.components;

import com.pokemod.rarecandy.loading.CubeMapTexture;
import com.pokemod.rarecandy.pipeline.ShaderPipeline;
import com.pokemod.rarecandy.rendering.ObjectInstance;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import java.util.List;

public class SkyboxRenderObject extends RenderObject {
    protected static final float[] SKYBOX_VERTICES = {
            // positions
            -1.0f, 1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,

            -1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,

            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,

            -1.0f, -1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,

            -1.0f, 1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, -1.0f,

            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f
    };
    private final int vao;
    public final CubeMapTexture texture;

    public SkyboxRenderObject(ShaderPipeline pipeline) {
        this.texture = new CubeMapTexture("D:\\Projects\\PixelmonGenerations\\RareCandy\\src\\renderer\\resources\\cubemap\\panorama_");
        this.shaderPipeline = pipeline;
        this.vao = GL30.glGenVertexArrays();
        var vbo = GL20C.glGenBuffers();

        try (var stack = MemoryStack.stackPush()) {
            var buffer = stack.mallocFloat(SKYBOX_VERTICES.length);
            for (var skyboxVertex : SKYBOX_VERTICES) buffer.put(skyboxVertex);
            buffer.rewind();

            GL30C.glBindVertexArray(vao);
            GL20C.glBindBuffer(GL20C.GL_ARRAY_BUFFER, vbo);
            GL20C.glBufferData(GL20C.GL_ARRAY_BUFFER, buffer, GL20C.GL_STATIC_DRAW);

            GL20.glEnableVertexAttribArray(0);
            GL20.glVertexAttribPointer(
                    0,
                    3,
                    GL11C.GL_FLOAT,
                    false,
                    3 * Float.BYTES,
                    0);
        }
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void render(List<ObjectInstance> instances) {
        if (instances.size() > 1) throw new RuntimeException("only 1 skybox is allowed");

        GL11C.glDepthFunc(GL11C.GL_LEQUAL);
        shaderPipeline.bind();
        shaderPipeline.updateOtherUniforms(instances.get(0), this);
        shaderPipeline.updateTexUniforms(instances.get(0), this);
        GL30C.glBindVertexArray(vao);
        GL11C.glDrawArrays(GL11C.GL_TRIANGLES, 0, 36);
        GL30C.glBindVertexArray(0);
        shaderPipeline.unbind();
        GL11C.glDepthFunc(GL11C.GL_LESS);
    }
}
