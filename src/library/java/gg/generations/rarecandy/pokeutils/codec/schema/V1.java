package gg.generations.rarecandy.pokeutils.codec.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TypeTemplate;
import gg.generations.rarecandy.pokeutils.codec.ModelCOnfigTypes;

import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class V1 extends Schema {

    public V1(int versionKey, Schema parent) {
        super(versionKey, parent);

        registerType(false, ModelCOnfigTypes.OBJECT_VALUE, () -> or(
                DSL.constType(DSL.floatType()),
                DSL.constType(DSL.bool()),
                ModelCOnfigTypes.COLOR.in(this),
                DSL.taggedChoice("type", DSL.string(), Map.of(
                        "boolean", DSL.constType(DSL.bool()),
                        "float", DSL.constType(DSL.floatType()),
                        "color", ModelCOnfigTypes.COLOR.in(this))
                ))
        );

        registerType(false, ModelCOnfigTypes.COLOR, () -> or(
                DSL.list(DSL.constType(DSL.floatType())),
                DSL.constType(DSL.string()),
                DSL.fields(
                        "x", DSL.constType(DSL.floatType()),
                        "y", DSL.constType(DSL.floatType()),
                        "z", DSL.constType(DSL.floatType())
                )
        ));

        registerType(false, ModelCOnfigTypes.MATERIAL_REFERENCE, () -> DSL.allWithRemainder(
                DSL.or(
                        DSL.field("parent", nullable("parent", DSL.string())),
                        DSL.field("inherits", nullable("inherits", DSL.string()))
                ),
                DSL.field("shader", nullable("shader", DSL.string())),
                DSL.field("effect", nullable("effect", DSL.string())),
                DSL.field("effect", nullable("effect", DSL.string())),
                DSL.field("effect", nullable("effect", DSL.string())),
                DSL.field("images", nullable("images", DSL.compoundList(
                        DSL.constType(DSL.string()),
                        DSL.constType(DSL.string())
                ))),
                DSL.field("values", nullable("values", DSL.compoundList(
                        DSL.constType(DSL.string()),
                        ModelCOnfigTypes.OBJECT_VALUE.in(this))
                ))));
    }

    private TypeTemplate or(TypeTemplate... templates) {
        return Stream.of(templates).reduce(DSL::or).orElse(DSL.emptyPart());
    }


    public static TypeTemplate nullable(String name, TypeTemplate template) {
        return DSL.optionalFields(name, template);
    }

    public static TypeTemplate nullable(String name, Type<?> type) {
        return nullable(name, DSL.constType(type));
    }
}
