package gg.generations.rarecandy.renderer.components;

import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.loading.ModelLoader;
import gg.generations.rarecandy.renderer.loading.SbboOffset;
import gg.generations.rarecandy.renderer.model.Variant;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import gg.generations.rarecandy.renderer.rendering.RenderStage;
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance;
import gg.generations.rarecandy.renderer.storage.DrawBuffer;
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

    public final ModelLoader.Names names;

    public Map<String, Integer> meshNameToId;
    public Map<String, Integer> materialNameToId;
    public Map<String, Integer> variantNameToId;
    public Map<String, Integer> imageNameToId;

    public int[] meshes;
    public Material[] materials;
    public Variant[] variants;
    public TextureArray images;
    public int[][] variantRelationships;
    public RenderStage[][] stageRelationships;

    public SbboOffset vertex;
    public SbboOffset index;
    public SbboOffset meshOffsets;

    public SSBOBuffer material;
    public SSBOBuffer variant;

    public SSBOBuffer instance;
    public SSBOBuffer drawInfo;

    public EnumMap<RenderStage, DrawBuffer> drawBuffer = new EnumMap<>(RenderStage.class);

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
        this.names = names;

        meshes = new int[names.meshes().size()];
        meshNameToId = listToMap(names.meshes());
        materials = new Material[names.materials().size()];
        materialNameToId = listToMap(names.materials());
        variants = new Variant[names.variants().size()];
        variantNameToId = listToMap(names.variants());
        variantRelationships = new int[meshes.length][variants.length];
        stageRelationships = new RenderStage[meshes.length][variants.length];
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

        instances.removeIf(a -> !a.isLinked());
        for (ObjectInstance instance : instances) {
            instance.update(absoluteTime);
        }

        updateSSBOs();
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

    public void close() throws IOException {
        for (Material material : this.materials) {
            material.close();
        }

        GL43.glDeleteBuffers(modelBuffer);
        this.drawInfo.delete();
        this.instance.delete();
        this.variant.delete();
        this.material.delete();

        drawBuffer.values().forEach(SSBOBuffer::delete);
    }

    public boolean isEmpty() {
        return instances.isEmpty();
    }

    public void updateSSBOs() {
        ensureCapacity();
        resetSSBOs();

        var drawId = 0;

        for (int instanceId = 0; instanceId < instances.size(); instanceId++) {
            ObjectInstance instance = instances.get(instanceId);
            instance.update(this.instance);

            for (int meshId = 0; meshId < meshes.length; meshId++) {

                var stage = stageRelationships[meshId][instance.variant()];

                if(!shouldRender(meshId, instance)) continue;

                var buffer = drawBuffer.get(stage);

                if(buffer == null) continue;

//TODO: Redo material animation
//                var variant = getVariant(meshId, instance.variant());
//                var material = variant.material();
//                Transform animationTransform = Transform.DEFAULT;



//                if (instance instanceof AnimatedObjectInstance animatedInstance) {
//
//                    var t = animatedInstance.getTransform(material);
//
//                    if (t != null && !t.isUnit()) {
//                        animationTransform = t;
//                    }
//                }

//                Transform.combine(animationTransform).upload(drawInfoBuffer);



                drawInfo.put(variantRelationships[meshId][instance.variant()]);
                drawInfo.put(instanceId);
                drawInfo.put(meshId);
                drawInfo.put(0);

                buffer.putDraw(meshes[meshId], 1, 0, drawId);
                drawId++;
            }
        }

        instance.upload();
        drawInfo.upload();
        drawBuffer.values().forEach(SSBOBuffer::upload);
    }

    private void resetSSBOs() {
        instance.reset();
        drawInfo.reset();

        drawBuffer.values().forEach(DrawBuffer::reset);
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

        instance.ensureCapacity((long) size * InstanceDetails.size);
        drawInfo.ensureCapacity(
                (long) instances.size()
                        * meshes.length
                        * TRANSFORM_ENTRY_BYTES
        );

        var drawSize = (long) meshes.length * size * Integer.BYTES * 4;

        drawBuffer.values().forEach(buffer -> buffer.ensureCapacity(drawSize));
    }

    public abstract int targetVertexStride();

    public int numOfInstances() {
        return instances.size();
    }
}
