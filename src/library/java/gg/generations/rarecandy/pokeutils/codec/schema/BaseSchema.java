package gg.generations.rarecandy.pokeutils.codec.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.CompoundList;
import com.mojang.datafixers.types.templates.TypeTemplate;
import gg.generations.rarecandy.pokeutils.codec.References;
import gg.generations.rarecandy.pokeutils.codec.TypeTemplates;

import java.util.Collections;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class BaseSchema extends Schema {

    public BaseSchema(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        schema.registerType(false, References.VECTOR3F, () -> DSL.or(
                        TypeTemplates.FLOAT_LIST,
                        DSL.fields(
                                "x", TypeTemplates.FLOAT,
                                "y", TypeTemplates.FLOAT,
                                "z", TypeTemplates.FLOAT)
                )
        );

        schema.registerType(false, References.VECTOR2F, () -> DSL.or(
                TypeTemplates.FLOAT_LIST,
                DSL.fields(
                        "x", TypeTemplates.FLOAT,
                        "y", TypeTemplates.FLOAT
                )
        ));

        schema.registerType(false, References.QUATERNION, () -> DSL.or(
                TypeTemplates.FLOAT_LIST,
                DSL.and(
                        DSL.field("x", TypeTemplates.FLOAT),
                        DSL.field("y", TypeTemplates.FLOAT),
                        DSL.field("z", TypeTemplates.FLOAT),
                        DSL.field("w", TypeTemplates.FLOAT)
                )
        ));

        var VECTOR_2F = References.VECTOR2F.in(schema);
        var VECTOR_3F = References.VECTOR3F.in(schema);

        schema.registerType(false, References.TRANSFORM, () -> DSL.or(
                VECTOR_2F,
                DSL.and(
                        nullableField("offset", VECTOR_2F),
                        nullableField("scale", VECTOR_2F)
                )
        ));

        schema.registerType(false, References.COLOR, () -> DSL.or(
                VECTOR_3F,
                TypeTemplates.STRING)
        );

        var color = References.COLOR.in(schema);

        schema.registerType(false, References.OBJECT_VALUE, () -> or(
                TypeTemplates.FLOAT,
                TypeTemplates.BOOLEAN,
                color,
                DSL.and(
                        DSL.field("type", TypeTemplates.STRING),
                        DSL.field("value", or(TypeTemplates.FLOAT, TypeTemplates.BOOLEAN, color))
                ))
        );

        schema.registerType(false, References.OBJECT_MAP, () -> nullableField("values", map(References.OBJECT_VALUE.in(schema))));

        schema.registerType(false, References.MATERIAL_REFERENCE, () -> DSL.and(
                nullableField("inherits", TypeTemplates.STRING),
                nullableField("parent", TypeTemplates.STRING),
                nullableField("shader", TypeTemplates.STRING),
                nullableField("blend", TypeTemplates.STRING),
                nullableField("cull", TypeTemplates.STRING),
                nullableField("effect", TypeTemplates.STRING),
                nullableField("images", map(TypeTemplates.STRING)),
                References.OBJECT_MAP.in(schema)
                ));

        schema.registerType(false, References.VARIANT_DETAILS, () -> DSL.and(
                nullableField("material", TypeTemplates.STRING),
                nullableField("hide", TypeTemplates.BOOLEAN),
                nullableField("offset", References.TRANSFORM.in(schema))

        ));

//        schema.registerType(false, References.PARENT, () -> DSL.and(
//                nullableField("parent", TypeTemplates.STRING),
//                nullableField("inherits", TypeTemplates.STRING)
//        ));

        schema.registerType(false, References.VARIANT_PARENT, () -> DSL.allWithRemainder(
                nullableField("parent", TypeTemplates.STRING),
                nullableField("inherits", TypeTemplates.STRING)
        ));

//        schema.registerType(false, References.SKELETON_TRANSFORM, () -> DSL.and(
//                nullableField("position", VECTOR_3F),
//                nullableField("rotation", References.QUATERNION.in(schema))
//        ));

//        schema.registerType(false, References.MESH_OPTIONS, () -> or(
//                TypeTemplates.BOOLEAN,
//                TypeTemplates.STRING_LIST,
//                DSL.and(
//                        nullableField("invert", TypeTemplates.BOOLEAN),
//                        nullableField("aliases", TypeTemplates.STRING_LIST)
//                )
//        ));

//        schema.registerType(false, References.HIDE_DURING_ANIMATION, () -> DSL.and(
//            nullableField("blackList", TypeTemplates.BOOLEAN),
//            nullableField("animations", TypeTemplates.STRING_LIST)
//        ));

        schema.registerType(true, References.MODEL_CONFIG, () -> DSL.allWithRemainder(
                nullableField("scale", DSL.constType(DSL.floatType())),
                nullableField("variants", map(or(TypeTemplates.STRING, References.VARIANT_PARENT.in(schema)))),
                nullableField("materials", map(References.MATERIAL_REFERENCE.in(schema)))
//                nullableField("defaultVariant", map(References.VARIANT_DETAILS.in(schema))),
//                nullableField("variants", map(map(References.VARIANT_PARENT.in(schema)))),
//                nullableField("animationFpsOverride", map(DSL.constType(DSL.intType()))),
//                nullableField("hideDuringAnimation", map(References.HIDE_DURING_ANIMATION.in(schema))),
//                nullableField("offsets", map(References.SKELETON_TRANSFORM.in(schema))),
//                nullableField("materialsWithSameMaterialAnimation", map(TypeTemplates.STRING_LIST)),
//                nullableField("ignoreScaleInAnimation", TypeTemplates.STRING_LIST),
//                nullableField("modelOptions", map(References.MESH_OPTIONS.in(schema))),
//                nullableField("meshesToRenderFirst", TypeTemplates.STRING_LIST),
//                nullableField("aliases", map(TypeTemplates.STRING_LIST)),
//                nullableField("excludeMeshNamesFromSkeleton", TypeTemplates.BOOLEAN)
        ));
    }

    public static TypeTemplate map(TypeTemplate valueTemplate) {
        return map(TypeTemplates.STRING, valueTemplate);
    }

    public static TypeTemplate map(TypeTemplate keyTemplate, TypeTemplate valueTemplate) {
        return DSL.compoundList(keyTemplate, valueTemplate);
    }

    static TypeTemplate or(final TypeTemplate first, final TypeTemplate... rest) {
        if (rest.length == 0) {
            return first;
        }
        TypeTemplate result = rest[rest.length - 1];
        for (int i = rest.length - 2; i >= 0; i--) {
            result = DSL.or(rest[i], result);
        }
        return DSL.or(first, result);
    }

    public static TypeTemplate templateOperations(BinaryOperator<TypeTemplate> operator, TypeTemplate... templates) {
        return Stream.of(templates).reduce(operator).orElse(DSL.emptyPart());
    }


    public static TypeTemplate nullableField(String name, TypeTemplate type) {
        return DSL.optional(DSL.field(name, type));
    }

    public static Type<?> nullable(Type<?> type) {
        return DSL.optional(type);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        return Collections.emptyMap();
    }
}
