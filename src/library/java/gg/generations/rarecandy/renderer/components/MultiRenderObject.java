package gg.generations.rarecandy.renderer.components;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.animation.Transform;
import gg.generations.rarecandy.renderer.loading.ModelLoader;
import gg.generations.rarecandy.renderer.loading.SbboOffset;
import gg.generations.rarecandy.renderer.model.Variant;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import gg.generations.rarecandy.renderer.rendering.RenderStage;
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance;
import gg.generations.rarecandy.renderer.storage.SSBOBuffer;
import gg.generations.rarecandy.renderer.textures.TextureArray;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL43;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 */
public abstract class MultiRenderObject {
    private static final int TRANSFORM_ENTRY_BYTES = Float.BYTES * 4 + Integer.BYTES * 4;

    public Map<String, Integer> meshNameToId;
    public Map<String, Integer> materialNameToId;
    public Map<String, Integer> variantNameToId;
    public Map<String, Integer> imageNameToId;
    public int[] meshes;
    public Material[] materials;
    public Variant[] variants;
    public TextureArray images;
    public int[][] variantRelationships;

    public SbboOffset vertex;
    public SbboOffset index;
    public SbboOffset meshOffsets;
    public SbboOffset material;
    public SbboOffset variant;

    public SSBOBuffer instanceBuffer;
    public SSBOBuffer uvTransformBuffer;
    public SSBOBuffer drawBuffer;

    public int modelBuffer;

    public int maxVertex;

    public Animation[] animations = new Animation[0];
    public String[] animationNames = new String[0];
    public Map<String, Integer> animationNameToId = Map.of();
    public boolean[][] hideDuringAnimation;

    private final List<Consumer<MultiRenderObject>> queue = new ArrayList<>();

    public final Vector3f dimensions = new Vector3f();
    public float scale = 1.0f;

    private Matrix4f rootTransformation = new Matrix4f();

    protected final List<ObjectInstance> instances = new ArrayList<>();

    public MultiRenderObject(ModelLoader.Names names) {
        meshes = new int[names.meshes().size()];
        meshNameToId = listToMap(names.meshes());
        materials = new Material[names.materials().size()];
        materialNameToId = listToMap(names.materials());
        variants = new Variant[names.variants().size()];
        variantNameToId = listToMap(names.variants());
        variantRelationships = new int[meshes.length][variants.length];
        imageNameToId = listToMap(names.images());
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
        state.modelMatrix().mul(rootTransformation, state.modelMatrix());
    }

    public Vector3f getDimensions() {
        return dimensions;
    }

    public void update(double absoluteTime) {
        for (var consumer : queue) {
                consumer.accept(this);
        }
        queue.clear();

        for (ObjectInstance instance : instances) {
            instance.update(absoluteTime);
        }

        instances.removeIf(a -> !a.isLinked());
    }

    public Set<String> availableVariants() {
        return variantNameToId.keySet();
    }

    public Material getMaterial(int mesh, int variant) {
        return materials[getVariant(mesh,variant).material()];
    }

    abstract public void render(TraditionalPipeline pipeline, RenderStage stage, List<ObjectInstance> instances);

    public void render(TraditionalPipeline pipeline, RenderStage stage) {
        render(pipeline, stage, instances);
    }

    public boolean shouldRender(int mesh, ObjectInstance instance) {
        if(instance instanceof AnimatedObjectInstance animationInstance) {
            var animation = animationInstance.currentAnimation;

            if(animation != null) {
                var animId = animationInstance.currentAnimation.getAnimation().id;
                if(hideDuringAnimation[mesh][animId]) {
                    return false;
                }
            }
        }

        return !getVariant(mesh, instance.variant()).hide();
    }

    public Variant getVariant(int mesh, int variant) {
        return variants[variantRelationships[mesh][variant]];
    }

    public void updateDimensions() {
//        for (var mesh : meshes) {
//            dimensions.max(mesh.getDimensions());
//        }
    }

    public void close() throws IOException {
        for (Material material : this.materials) {
            material.close();
        }

        GL43.glDeleteBuffers(modelBuffer);
        this.uvTransformBuffer.delete();
        this.instanceBuffer.delete();
        this.drawBuffer.delete();
    }

    public boolean isEmpty() {
        return instances.isEmpty();
    }

    public void updateSSBOs() {
        ensureCapacity();
        resetSSBOs();

        for (int instanceId = 0; instanceId < instances.size(); instanceId++) {
            ObjectInstance instance = instances.get(instanceId);
            instance.update(instanceBuffer);

            for (int meshId = 0; meshId < meshes.length; meshId++) {

                var variant = getVariant(meshId, instance.variant());

                Transform variantTransform = variant.offset();
                Transform animationTransform = null;

                var material = variant.material();

                var isRendering = shouldRender(meshId, instance);

                if (instance instanceof AnimatedObjectInstance animatedInstance) {

                    var t = animatedInstance.getTransform(material);

                    if (t != null && !t.isUnit()) {
                        animationTransform = t;
                    }
                }

                Transform.combine(variantTransform, animationTransform).upload(uvTransformBuffer);
                uvTransformBuffer.put(variantRelationships[meshId][instance.variant()]);
                uvTransformBuffer.put(instanceId);
                uvTransformBuffer.put(isRendering);
                uvTransformBuffer.put(0);

                drawBuffer
                        .put(meshes[meshId])
                        .put(1)
                        .put(0)
                        .put(0);
            }
        }

        instanceBuffer.upload();
        uvTransformBuffer.upload();
        drawBuffer.upload();
    }

    private void resetSSBOs() {
        instanceBuffer.reset();
        uvTransformBuffer.reset();
        drawBuffer.reset();
    }

    public <T extends ObjectInstance> boolean add(@NotNull T instance) {
        if(!instance.isLinked()) {
            instance.link(this);
            instances.add(instance);

            return true;
        }

        return false;
    }

    private void ensureCapacity() {
        var size = instances.size();

        instanceBuffer.ensureCapacity((long) size * InstanceDetails.size);
        uvTransformBuffer.ensureCapacity(
                (long) instances.size()
                        * meshes.length
                        * TRANSFORM_ENTRY_BYTES
        );
        drawBuffer.ensureCapacity((long) meshes.length * size * Integer.BYTES * 4);
    }

    public abstract int targetVertexStride();

    public int numOfInstances() {
        return instances.size();
    }
}
