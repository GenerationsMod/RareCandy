package com.pokemod.pokeutils.gui;

import com.pokemod.rarecandy.components.AnimatedMeshObject;
import com.pokemod.rarecandy.components.MeshObject;
import com.pokemod.rarecandy.components.MutiRenderObject;
import com.pokemod.rarecandy.loading.GogoatLoader;
import com.pokemod.rarecandy.pipeline.Pipeline;
import com.pokemod.rarecandy.rendering.InstanceState;
import com.pokemod.rarecandy.rendering.RareCandy;
import com.pokemod.rarecandy.settings.Settings;
import com.pokemod.rarecandy.settings.TransparencyMethod;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class RareCandyCanvas extends AWTGLCanvas {
    public static Matrix4f projectionMatrix;
    public final Matrix4f viewMatrix = new Matrix4f().lookAt(0.1f, 0.01f, -2, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    private RareCandy renderer;

    public RareCandyCanvas() {
        super(new GLData());
        projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(90), (float) getWidth() / getHeight(), 0.1f, 1000.0f);
    }

    @Override
    public void initGL() {
        GL.createCapabilities();
        GuiPipelines.onInitialize();
        this.renderer = new RareCandy(new Settings(TransparencyMethod.NONE, 2));
        GL11C.glClearColor(0.6f, 0.4f, 0.4f, 1);
        GL11C.glFrontFace(GL11C.GL_CW);
        GL11C.glCullFace(GL11C.GL_FRONT);
        GL11C.glEnable(GL11C.GL_CULL_FACE);
        GL11C.glEnable(GL11C.GL_DEPTH_TEST);

        loadPokemonModel(renderer, "sobble", model -> {
            var i = 0;
            var variants = List.of("none-normal", "none-shiny");

            for (String variant : variants) {
                var instance = new InstanceState(new Matrix4f(), viewMatrix, variant, 0xFFFFFFFF);
                instance.transformationMatrix().translate(new Vector3f(i * 8 - 4, -2f, 8)).scale(1).rotate((float) Math.toRadians(180), new Vector3f(0, 1, 0)).scale(0.3f);
                renderer.addObject(model, instance);
                i++;
            }
        });
    }

    @Override
    public void paintGL() {
        GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
        renderer.render(true, false);
        swapBuffers();
    }

    protected MutiRenderObject<AnimatedMeshObject> loadPokemonModel(RareCandy renderer, String name, Consumer<MutiRenderObject<AnimatedMeshObject>> onFinish) {
        return load(renderer, name, this::getPokemonPipeline, onFinish, AnimatedMeshObject::new);
    }

    protected <T extends MeshObject> MutiRenderObject<T> load(RareCandy renderer, String name, Function<String, Pipeline> pipelineFactory, Consumer<MutiRenderObject<T>> onFinish, Supplier<T> supplier) {
        var loader = renderer.getLoader();
        return loader.createObject(
                () -> RareCandyCanvas.class.getResourceAsStream("/new/" + name + ".pk"),
                (gltfModel, smdFileMap, object) -> {
                    var glCalls = new ArrayList<Runnable>();
                    GogoatLoader.create(object, gltfModel, smdFileMap, glCalls, pipelineFactory, supplier);
                    return glCalls;
                },
                onFinish
        );
    }

    private Pipeline getPokemonPipeline(String materialName) {
        System.out.println("E");
        return GuiPipelines.ANIMATED;
    }
}
