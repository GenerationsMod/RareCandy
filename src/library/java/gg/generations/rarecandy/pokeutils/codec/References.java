package gg.generations.rarecandy.pokeutils.codec;

import com.google.common.base.Predicates;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TypeTemplate;

import java.util.HashSet;
import java.util.Set;

public class References {
    private static Set<DSL.TypeReference> types = new HashSet<>();
    public static final DSL.TypeReference MODEL_CONFIG = register("model_config");
    public static final DSL.TypeReference OBJECT_MAP = register("object_map");
    public static final DSL.TypeReference OBJECT_VALUE = register("object_value");
    public static final DSL.TypeReference MATERIAL_REFERENCE = register("material_reference");
    public static final DSL.TypeReference COLOR = register("color");
    public static final DSL.TypeReference VECTOR2F = register("vector_2f");
    public static final DSL.TypeReference VECTOR3F = register("vector_3f");
    public static final DSL.TypeReference QUATERNION = register("quaternion");
    public static final DSL.TypeReference TRANSFORM = register("transform");
    public static final DSL.TypeReference VARIANT_DETAILS = register("variant_details");
    public static final DSL.TypeReference PARENT = register("parent");
    public static final DSL.TypeReference VARIANT_PARENT = register("variant_parent");
    public static final DSL.TypeReference VARIANT_DETAILS_TRANSFORM = register("variant_details_transform");
    public static final DSL.TypeReference SKELETON_TRANSFORM = register("skeleton_transform");
    public static final DSL.TypeReference MESH_OPTIONS = register("mesh_options");
    public static final DSL.TypeReference HIDE_DURING_ANIMATION = register("hide_during_animation");
    public static final DSL.TypeReference DUMMY = register("dummy");

    private static DSL.TypeReference register(String name) {
        DSL.TypeReference reference = () -> name;
        types.add(reference);
        return reference;
    }

    public static Set<DSL.TypeReference> types() {
        return types;
    }
}
