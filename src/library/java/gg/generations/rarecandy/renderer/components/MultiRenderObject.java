package gg.generations.rarecandy.renderer.components;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.pokeutils.ModelConfig;
import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.loading.ModelLoader;
import gg.generations.rarecandy.renderer.model.RenderModel;
import gg.generations.rarecandy.renderer.model.Variant;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import gg.generations.rarecandy.renderer.rendering.RenderStage;
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance;
import gg.generations.rarecandy.renderer.textures.TextureArray;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 */
public class MultiRenderObject {
    public Map<String, Integer> meshNameToId;
    public Map<String, Integer> materialNameToId;
    public Map<String, Integer> variantNameToId;
    public RenderModel[] meshes;
    public Material[] materials;
    public Variant[] variants;
    public int[][] variantRelationships;
    public TextureArray images;

    public Animation[] animations = new Animation[0];
    public String[] animationNames = new String[0];
    public Map<String, Integer> animationNameToId = Map.of();
    public boolean[][] hideDuringAnimation;

    private final List<Consumer<MultiRenderObject>> queue = new ArrayList<>();

    public final Vector3f dimensions = new Vector3f();
    public float scale = 1.0f;

    private Matrix4f rootTransformation = new Matrix4f();

    public MultiRenderObject(ModelLoader.Names names, TextureArray images) {
        meshes = new RenderModel[names.meshes().size()];
        meshNameToId = listToMap(names.meshes());
        materials = new Material[names.materials().size()];
        materialNameToId = listToMap(names.materials());
        variants = new Variant[names.variants().size()];
        variantNameToId = listToMap(names.variants());
        variantRelationships = new int[meshes.length][variants.length];
        this.images = images;
    }

    public static Map<String, Integer> listToMap(List<String> list) {
        return IntStream.range(0, list.size())
                .boxed()
                .collect(Collectors.toMap(
                        list::get,
                        Function.identity()
                ));
    }

    public void onUpdate(Consumer<MultiRenderObject> consumer) {
        queue.add(consumer);
    }

    public Matrix4f getRootTransformation() {
        return rootTransformation;
    }

    public void setRootTransformation(Matrix4f rootTransformation) {
        this.rootTransformation = rootTransformation;
    }

    public void applyRootTransformation(ObjectInstance state) {
        state.transformationMatrix().mul(rootTransformation, state.transformationMatrix());
    }

    public Vector3f getDimensions() {
        return dimensions;
    }

    public void update() {
        for (var consumer : queue) {
                consumer.accept(this);
        }
        queue.clear();
    }

    public Set<String> availableVariants() {
        return variantNameToId.keySet();
    }

    public Material getMaterial(int mesh, int variant) {
        return materials[getVariant(mesh,variant).material()];
    }

    public void render(TraditionalPipeline pipeline, RenderStage stage, List<ObjectInstance> instances) {
        for (var instance : instances) {

            for (int mesh = 0; mesh < this.meshes.length; mesh++) {
                if (!shouldRender(mesh, instance)) {
                    System.out.println("Blep: " + mesh + " " + instance.variant());


                    continue;
                }

                var model = this.meshes[mesh];
                if (model != null) {
                    System.out.println("Yes!");

                    pipeline.bindInstance(instance, this, mesh);

                    var material = getMaterial(mesh, instance.variant());
                    pipeline.preDraw(material);
                    var transparent = material.blendType() != BlendType.None;

                    if(transparent && stage == RenderStage.TRANSPARENT || stage == RenderStage.SOLID) {
                        model.runDrawCalls();
                    }

                    pipeline.postDraw(material);
                }
            }
        }
    }

    public boolean shouldRender(int mesh, ObjectInstance instance) {
        if(instance instanceof AnimatedObjectInstance animationInstance) {
            var animation = animationInstance.currentAnimation;

            if(animation != null) {
                var animId = animationInstance.currentAnimation.getAnimation().id;
                if(hideDuringAnimation[mesh][animId]) {

                    System.out.println(":D");
                    return true;
                }
            }
        }

        var maybe = !getVariant(mesh, instance.variant()).hide();

        System.out.println(">:3: " + maybe);

        return maybe;
    }

    public Variant getVariant(int mesh, int variant) {
        return variants[variantRelationships[mesh][variant]];
    }

    public void updateDimensions() {
        for (var mesh : meshes) {
            dimensions.max(mesh.getDimensions());
        }
    }

    public void close() throws IOException {
        images.close();
        for (Material material : this.materials) {
            material.close();
        }

        for (RenderModel mesh : this.meshes) {
            mesh.close();
        }
    }
}
