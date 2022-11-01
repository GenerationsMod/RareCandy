package com.pokemod.pokeutils.gui;

import com.pokemod.pokeutils.PixelAsset;
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

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class RareCandyCanvas extends AWTGLCanvas {
    public static Matrix4f projectionMatrix;
    public final Matrix4f viewMatrix = new Matrix4f().lookAt(0.1f, 0.01f, -2, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    public double startTime = System.currentTimeMillis();
    private RareCandy renderer;
    public MutiRenderObject<AnimatedMeshObject> object;
    private int scaleModifier = 0;
    public String currentAnimation = null;

    public RareCandyCanvas() {
        super(defaultData());
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(100), (float) getWidth() / getHeight(), 0.1f, 1000.0f);
            }
        });
    }

    private static GLData defaultData() {
        var data = new GLData();
        data.profile = GLData.Profile.CORE;
        data.forwardCompatible = true;
        data.api = GLData.API.GL;
        data.majorVersion = 3;
        data.minorVersion = 2;
        return data;
    }

    public void openFile(PixelAsset pkFile) {
        currentAnimation = null;
        renderer.clearAllInstances();
        this.object = loadPokemonModel(renderer, pkFile, model -> {
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
    public void initGL() {
        projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(100), (float) getWidth() / getHeight(), 0.1f, 1000.0f);
        GL.createCapabilities(true);
        GuiPipelines.onInitialize();
        this.renderer = new RareCandy(new Settings(TransparencyMethod.NONE, 2));
        GL11C.glClearColor(60 / 255f, 63 / 255f, 65 / 255f, 1);
        GL11C.glFrontFace(GL11C.GL_CW);
        GL11C.glCullFace(GL11C.GL_FRONT);
        GL11C.glEnable(GL11C.GL_CULL_FACE);
        GL11C.glEnable(GL11C.GL_DEPTH_TEST);
    }

    @Override
    public void paintGL() {
        GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
        renderer.render(true, false);
        swapBuffers();

        if (object != null) {
            var timePassed = ((System.currentTimeMillis() - startTime) / 16000);
            object.onUpdate(object -> {
                if (object.animations.containsKey(currentAnimation)) {
                    object.activeAnimation = currentAnimation;
                }

                if (object.activeAnimation != null && object.getAnimation(object.activeAnimation) != null)
                    object.animationTime = object.getAnimation(object.activeAnimation).getAnimationTime(timePassed);
            });

            for (var rendererObject : renderer.getObjects()) {
                if (scaleModifier != 0) {
                    var newScale = 1 - (scaleModifier * 0.1f);
                    rendererObject.transformationMatrix().scale(newScale);
                }
            }
            scaleModifier = 0;
        }
    }

    protected MutiRenderObject<AnimatedMeshObject> loadPokemonModel(RareCandy renderer, PixelAsset is, Consumer<MutiRenderObject<AnimatedMeshObject>> onFinish) {
        return load(renderer, is, this::getPokemonPipeline, onFinish, AnimatedMeshObject::new);
    }

    protected <T extends MeshObject> MutiRenderObject<T> load(RareCandy renderer, PixelAsset is, Function<String, Pipeline> pipelineFactory, Consumer<MutiRenderObject<T>> onFinish, Supplier<T> supplier) {
        var loader = renderer.getLoader();
        return loader.createObject(
                () -> is,
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

    public void onMouseScroll(MouseWheelEvent e) {
        scaleModifier += e.getWheelRotation();
    }
}
