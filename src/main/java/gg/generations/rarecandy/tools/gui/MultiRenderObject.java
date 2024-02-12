package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.arceus.core.RareCandyScene;
import gg.generations.rarecandy.arceus.model.Material;
import gg.generations.rarecandy.arceus.model.Model;
import gg.generations.rarecandy.arceus.model.RenderingInstance;
import gg.generations.rarecandy.arceus.model.lowlevel.*;
import gg.generations.rarecandy.arceus.model.pk.PkMaterial;
import gg.generations.rarecandy.arceus.model.pk.Variant;
import gg.generationsmod.rarecandy.model.Mesh;
import gg.generationsmod.rarecandy.model.RawModel;
import gg.generationsmod.rarecandy.model.animation.Skeleton;
import gg.generationsmod.rarecandy.model.config.pk.ModelConfig;
import gg.generationsmod.rarecandy.model.config.pk.VariantDetails;
import gg.generationsmod.rarecandy.model.config.pk.VariantParent;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultiRenderObject<T extends RenderingInstance> {
    private final Map<String, Model> meshes;

    private final Map<String, Map<String, PkMaterial>> materials;

    private final Map<String, List<String>> hidden;
    private final Map<String, PkMaterial> defaultMaterials;
    private final List<String> defaultHidden;

    public MultiRenderObject(RawModel rawModel) {
        var config = rawModel.config();


        Map<String, ModelConfig.HideDuringAnimation> hideDuringAnimation = new HashMap<>();

        var materials = new HashMap<String, PkMaterial>();

        config.materials.forEach((k, v) -> {
            var material = PkMaterial.process(k, config.materials, rawModel.images());

            materials.put(k, material);
        });

        var defaultVariant = new HashMap<String, Variant>();
        config.defaultVariant.forEach((k, v) -> {
            var variant = new Variant(materials.get(v.material()), v.hide());
            defaultVariant.put(k, variant);
        });

        var variantMaterialMap = new HashMap<String, Map<String, PkMaterial>>();
        var variantHideMap = new HashMap<String, List<String>>();

        if(config.hideDuringAnimation != null) {
            hideDuringAnimation = config.hideDuringAnimation;
        }

        defaultMaterials = new HashMap<String, PkMaterial>();
        defaultHidden = new ArrayList<String>();
        defaultVariant.forEach((s1, variant) -> {
            defaultMaterials.put(s1, variant.material());
            if (variant.hide()) defaultHidden.add(s1);
        });

        if(config.variants != null) {
            for (Map.Entry<String, VariantParent> entry : config.variants.entrySet()) {
                String variantKey = entry.getKey();
                VariantParent variantParent = entry.getValue();

                VariantParent child = config.variants.get(variantParent.inherits());

                var map = variantParent.details();

                while (child != null) {
                    var details = child.details();

                    applyVariantDetails(details, map);

                    child = config.variants.get(child.inherits());
                }

                applyVariantDetails(config.defaultVariant, map);

                var matMap = variantMaterialMap.computeIfAbsent(variantKey, s3 -> new HashMap<>());
                var hideMap = variantHideMap.computeIfAbsent(variantKey, s3 -> new ArrayList<>());

                applyVariant(materials, matMap, hideMap, map);
            }
        } else {
            variantMaterialMap.put("regular", defaultMaterials);
            variantHideMap.put("regular", defaultHidden);
        }

        this.materials = reverseMap(variantMaterialMap);
        this.hidden = reverseListMap(variantHideMap);

        meshes = Stream.of(rawModel.meshes()).collect(Collectors.toMap(Mesh::name, a -> fromMesh(a, rawModel.skeleton())));
    }

    public Map<String, PkMaterial> getMapForVariants(String variant) {
        return materials.getOrDefault(variant, defaultMaterials);
    }

    public static <T> Map<String, Map<String, T>> reverseMap(Map<String, Map<String, T>> inputMap) {
        Map<String, Map<String, T>> reversedMap = new HashMap<>();

        for (Map.Entry<String, Map<String, T>> outerEntry : inputMap.entrySet()) {
            String outerKey = outerEntry.getKey();
            Map<String, T> innerMap = outerEntry.getValue();

            for (Map.Entry<String, T> innerEntry : innerMap.entrySet()) {
                String innerKey = innerEntry.getKey();
                T value = innerEntry.getValue();

                reversedMap.computeIfAbsent(innerKey, k -> new HashMap<>()).put(outerKey, value);
            }
        }

        return reversedMap;
    }

    public static Map<String, List<String>> reverseListMap(Map<String, List<String>> inputMap) {
        Map<String, List<String>> reversedMap = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : inputMap.entrySet()) {
            String outerKey = entry.getKey();
            List<String> innerList = entry.getValue();

            for (String innerKey : innerList) {
                reversedMap.computeIfAbsent(innerKey, k -> new ArrayList<>()).add(outerKey);
            }
        }

        return reversedMap;
    }

    private static void applyVariantDetails(Map<String, VariantDetails> applied, Map<String, VariantDetails> appliee) {
        for (Map.Entry<String, VariantDetails> entry : applied.entrySet()) {
            String k = entry.getKey();
            VariantDetails v = entry.getValue();
            appliee.compute(k, (s, variantDetails) -> {
                if(variantDetails == null) return v;
                return variantDetails.fillIn(v);
            });
        }
    }

    private static void applyVariant(Map<String, PkMaterial> materials, Map<String, PkMaterial> matMap, List<String> hideMap, Map<String, VariantDetails> variantMap) {
        variantMap.forEach((k, v) -> {
            matMap.put(k, materials.get(v.material()));
            if (v.hide() != null && v.hide()) hideMap.add(k);
        });
    }

    private static Model fromMesh(Mesh mesh, Skeleton skeleton) {
        var length = (Float.BYTES * 3) + (Float.BYTES * 2) + (Float.BYTES * 3);
        var amount = mesh.positions().size();

        var vertices = new float[length * amount];

        for (int i = 0; i < amount; i += length) {
            var position = mesh.positions().get(i);
            var uv = mesh.uvs().get(i);
            var normal = mesh.normals().get(i);

            vertices[i] = position.x;
            vertices[i+1] = position.y;
            vertices[i+2] = position.z;
            vertices[i+3] = uv.x;
            vertices[i+4] = uv.y;
            vertices[i+5] = normal.x;
            vertices[i+6] = normal.y;
            vertices[i+7] = normal.z;
        }

        var vertexBuffer = MemoryUtil.memAlloc(vertices.length * 4);
        vertexBuffer.asFloatBuffer().put(vertices).flip();

        var indexBuffer = MemoryUtil.memAlloc(mesh.indices().size() * 4);
        indexBuffer.asIntBuffer().put(mesh.indices().stream().mapToInt(a -> a).toArray()).flip();

        var vertexData = new VertexData(vertexBuffer, ATTRIBUTES);

        return new Model(mesh.name(), new RenderData(DrawMode.TRIANGLES, vertexData, indexBuffer, IndexType.UNSIGNED_INT, mesh.indices().size()));
    }

    private static List<Attribute> ATTRIBUTES = List.of(
            Attribute.POSITION,
            Attribute.TEXCOORD,
            Attribute.NORMAL/*,
            Attribute.BONE_IDS,
            Attribute.BONE_WEIGHTS*/
    );

    public static class MultiRenderObjectInstance {
        private final MultiRenderObject<?> object;
        private Matrix4f transform;

        private Map<String, Supplier<PkMaterial>> materialSuppliers;
        private String variant;

        public MultiRenderObjectInstance(MultiRenderObject<?> object, Matrix4f transform, String varaint) {
            this.object = object;
            this.transform = transform;
            this.variant = varaint;

            materialSuppliers = new HashMap<>();

            for (var pair : object.meshes.keySet()) {

                materialSuppliers.put(pair, () -> object.getMapForVariants(this.getVariant()).get(pair));
            }
        }

        public void addToScene(RareCandyScene scene){
            for(var pair : object.meshes.entrySet()) {
                var key = pair.getKey();
                var value = pair.getValue();

                scene.addInstance(new MultiRenderingInstance(value, materialSuppliers.get(key), transform));

            }
        }
            public String getVariant () {
                return variant;
            }

            public void setVariant (String variant){
                this.variant = variant;
            }
        }

        public record MultiRenderingInstance(Model model, Supplier<PkMaterial> materialSupplier, Matrix4f transform) implements RenderingInstance {
        @Override
        public Model getModel() {
            return model;
        }

        @Override
        public PkMaterial getMaterial() {
            return materialSupplier.get();
        }

        @Override
        public Matrix4f getTransform() {
            return transform;
        }
    }
}
