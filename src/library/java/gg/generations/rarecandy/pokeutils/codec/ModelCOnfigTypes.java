package gg.generations.rarecandy.pokeutils.codec;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.templates.TypeTemplate;

public class ModelCOnfigTypes {
    public static final DSL.TypeReference MODEL_CONFIG = () -> "model_config";
    public static final DSL.TypeReference OBJECT_VALUE = () -> "object_value";
    public static final DSL.TypeReference MATERIAL_REFERENCE = () -> "material_reference";

    public static final DSL.TypeReference COLOR = () -> "vector3f";

    public static final DSL.TypeReference NEW_VECTOR3F = () -> "new_vector3f";

    public static class TypeTemplates {
        public static TypeTemplate VECTOR3F_OBJECT = DSL.fields(
                "x", DSL.constType(DSL.floatType()),
                "y", DSL.constType(DSL.floatType()),
                "z", DSL.constType(DSL.floatType())
        );
    }
}
