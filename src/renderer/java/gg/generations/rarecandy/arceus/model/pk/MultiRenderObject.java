package gg.generations.rarecandy.arceus.model.pk;

import gg.generations.rarecandy.arceus.model.Model;
import gg.generations.rarecandy.arceus.model.RenderingInstance;
import gg.generations.rarecandy.arceus.model.lowlevel.*;
import gg.generationsmod.rarecandy.Pair;
import gg.generationsmod.rarecandy.model.Mesh;
import gg.generationsmod.rarecandy.model.RawModel;
import gg.generationsmod.rarecandy.model.animation.Animation;
import gg.generationsmod.rarecandy.model.animation.Bone;
import gg.generationsmod.rarecandy.model.animation.Skeleton;
import gg.generationsmod.rarecandy.model.config.pk.ModelConfig;
import gg.generationsmod.rarecandy.model.config.pk.VariantDetails;
import gg.generationsmod.rarecandy.model.config.pk.VariantParent;
import org.lwjgl.system.MemoryUtil;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MultiRenderObject<T extends RenderingInstance> implements Closeable {
    public final Map<String, Model> meshes;

    private final Map<String, Map<String, PkMaterial>> materials;

    private final Map<String, List<String>> hidden;
    private final Map<String, PkMaterial> defaultMaterials;
    private final List<String> defaultHidden;

    public final Map<String, Animation<?>> animations;

    private final float scale;

    public MultiRenderObject(RawModel rawModel) {
        var config = rawModel.config();
        scale = config.scale;

        Map<String, ModelConfig.HideDuringAnimation> hideDuringAnimation = new HashMap<>();

        animations = rawModel.animations();

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

        if (config.hideDuringAnimation != null) {
            hideDuringAnimation = config.hideDuringAnimation;
        }

        defaultMaterials = new HashMap<String, PkMaterial>();
        defaultHidden = new ArrayList<String>();
        defaultVariant.forEach((s1, variant) -> {
            defaultMaterials.put(s1, variant.material());
            if (variant.hide()) defaultHidden.add(s1);
        });

        if (config.variants != null) {
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

        this.materials = variantMaterialMap;
        this.hidden = variantHideMap;

        meshes = Stream.of(rawModel.meshes()).collect(Collectors.toMap(Mesh::name, a -> fromMesh(a, rawModel.skeleton())));
    }

    public Map<String, PkMaterial> getMapForVariants(String variant) {
        return materials.getOrDefault(variant, defaultMaterials);
    }

    public List<String> shouldHide(String variant) {
        return hidden.getOrDefault(variant, Collections.emptyList());
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
                if (variantDetails == null) return v;
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

        var length = VertexData.calculateVertexSize(ATTRIBUTES);
        var amount = mesh.positions().size();

        var vertexBuffer = MemoryUtil.memAlloc(length * amount);

        var bones = IntStream.range(0, amount).mapToObj(a -> new VertexBoneData()).toList();


        mesh.bones().forEach(bone -> {
            var boneId = skeleton.getId(bone);

            for(var weight : bone.weights) {
                if(weight.weight == 0.0) return;
                else {
                    bones.get(weight.vertexId).addBoneData(boneId, weight.weight);
                }
            }
        });

        for (int i = 0; i < amount; i++) {

            var position = mesh.positions().get(i);
            var uv = mesh.uvs().get(i);
            var normal = mesh.normals().get(i);
            var bone = bones.get(i);

            vertexBuffer.putFloat(position.x);
            vertexBuffer.putFloat(position.y);
            vertexBuffer.putFloat(position.z);
            vertexBuffer.putFloat(uv.x);
            vertexBuffer.putFloat(uv.y);
            vertexBuffer.putFloat(normal.x);
            vertexBuffer.putFloat(normal.y);
            vertexBuffer.putFloat(normal.z);

            vertexBuffer.put((byte) bone.ids()[0]);
            vertexBuffer.put((byte) bone.ids()[1]);
            vertexBuffer.put((byte) bone.ids()[2]);
            vertexBuffer.put((byte) bone.ids()[3]);

            vertexBuffer.putFloat(bone.weights()[0]);
            vertexBuffer.putFloat(bone.weights()[1]);
            vertexBuffer.putFloat(bone.weights()[2]);
            vertexBuffer.putFloat(bone.weights()[3]);
        }

        vertexBuffer.flip();

        var indexBuffer = MemoryUtil.memAlloc(mesh.indices().size() * 4);
        indexBuffer.asIntBuffer().put(mesh.indices().stream().mapToInt(a -> a).toArray()).flip();

        var vertexData = new VertexData(vertexBuffer, ATTRIBUTES);

        return new Model(mesh.name(), new RenderData(DrawMode.TRIANGLES, vertexData, indexBuffer, IndexType.UNSIGNED_INT, mesh.indices().size()));
    }

    private static List<Attribute> ATTRIBUTES = List.of(
            Attribute.POSITION,
            Attribute.TEXCOORD,
            Attribute.NORMAL,
            Attribute.BONE_IDS,
            Attribute.BONE_WEIGHTS
    );

    public float getScale() {
        return scale;
    }

    @Override
    public void close() throws IOException {
        Set<PkMaterial> closedMaterials = new HashSet<>();
        for (var map : materials.values()) {
            for (var material : map.values()) {
                if (closedMaterials.contains(material)) continue;
                material.close();
                closedMaterials.add(material);
            }
        }

        for (var material : defaultMaterials.values()) {
            if (closedMaterials.contains(material)) continue;
            material.close();
            closedMaterials.add(material);
        }

        for (Model value : meshes.values()) {
            value.data().close();
        }
    }


    public static List<Set<Pair<Bone, Float>>> organizeBones(List<Bone> bones) {
        // Determine the maximum vertex ID
        int maxVertexId = bones.stream()
                .flatMap(bone -> Arrays.stream(bone.weights))
                .mapToInt(weight -> weight.vertexId)
                .max()
                .orElse(-1);

        // Create a list of sets with the required size
        List<Set<Pair<Bone, Float>>> boneList = new ArrayList<>(Collections.nCopies(maxVertexId + 1, new HashSet<>()));

        // Populate the list of sets with distinct bone-weight pairs
        bones.forEach(bone ->
                Arrays.stream(bone.weights)
                        .forEach(vertexWeight -> {
                            if(vertexWeight.weight != 0.0) {
                                Pair<Bone, Float> pair = new Pair<>(bone, vertexWeight.weight);
                                boneList.get(vertexWeight.vertexId).add(pair);
                            }
                        })
        );

        return boneList;
    }
}