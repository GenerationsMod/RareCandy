package gg.generations.rarecandy.renderer.components;

import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.animation.Transform;
import gg.generations.rarecandy.renderer.loading.ModelLoader;
import gg.generations.rarecandy.renderer.loading.SbboOffset;
import gg.generations.rarecandy.renderer.model.Variant;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import gg.generations.rarecandy.renderer.rendering.RenderStage;
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance;
import gg.generations.rarecandy.renderer.storage.SSBOBuffer;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL43C;

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
    public Map<String, Integer> meshNameToId;
    public Map<String, Integer> materialNameToId;
    public Map<String, Integer> variantNameToId;
    public Map<String, Integer> imageNameToId;
    public DrawRecord[] meshes;
    public Material[] materials;
    public Variant[] variants;
    public String[] images;
    public int[][] variantRelationships;

    public SbboOffset vertex;
    public SbboOffset index;
    public SbboOffset draw;
    public SbboOffset target;

    public SSBOBuffer instanceBuffer;
    public SSBOBuffer uvTransformBuffer;

    public int modelBuffer;
    public int destBuffer;

    public int maxVertex;

    public Animation[] animations = new Animation[0];
    public String[] animationNames = new String[0];
    public Map<String, Integer> animationNameToId = Map.of();
    public boolean[][] hideDuringAnimation;

    private final List<Consumer<MultiRenderObject>> queue = new ArrayList<>();

    public final Vector3f dimensions = new Vector3f();
    public float scale = 1.0f;

    private Matrix4f rootTransformation = new Matrix4f();

    private final List<ObjectInstance> instances = new ArrayList<>();

    public MultiRenderObject(ModelLoader.Names names) {
        meshes = new DrawRecord[names.meshes().size()];
        meshNameToId = listToMap(names.meshes());
        materials = new Material[names.materials().size()];
        materialNameToId = listToMap(names.materials());
        variants = new Variant[names.variants().size()];
        variantNameToId = listToMap(names.variants());
        variantRelationships = new int[meshes.length][variants.length];
        images = names.images().toArray(String[]::new);
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
        state.transformationMatrix().mul(rootTransformation, state.transformationMatrix());
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

    abstract public void render(RenderStage stage, List<ObjectInstance> instances);

    public void render(RenderStage stage) {
        render(stage, instances);
    }

    public boolean shouldRender(int mesh, ObjectInstance instance) {
        if(instance instanceof AnimatedObjectInstance animationInstance) {
            var animation = animationInstance.currentAnimation;

            if(animation != null) {
                var animId = animationInstance.currentAnimation.getAnimation().id;
                if(hideDuringAnimation[mesh][animId]) {
                    return true;
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
        GL43.glDeleteBuffers(destBuffer);
        this.uvTransformBuffer.delete();
        this.instanceBuffer.delete();
    }

    public boolean isEmpty() {
        return instances.isEmpty();
    }

    public void updateSSBOs() {
        ensureCapacity();
        for (int i = 0; i < instances.size(); i++) {
            var instance = instances.get(i);

            instance.update(i * InstanceDetails.size, instanceBuffer);

            for (int meshId = 0; meshId < meshes.length; meshId++) {

                var variant = getVariant(meshId, instance.variant());

                Transform transform = variant.offset();

                if (instance instanceof AnimatedObjectInstance animatedInstance) {

                    var material = variant.material();

                    var t = animatedInstance.getTransform(material);

                    if (t != null && !t.isUnit()) {
                        transform = t;
                    }
                }

                if(transform == null) {
                    transform = Transform.DEFAULT;
                }

                int stride = Float.BYTES * 4; // 16
                int index = i * meshes.length + meshId;
                transform.upload(index * stride, uvTransformBuffer);            }
        }

        instanceBuffer.upload();
        uvTransformBuffer.upload();
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
                        * Float.BYTES * 4
        );
    }

    public abstract int targetVertexStride();

    public int numOfInstances() {
        return instances.size();
    }
}
