package gg.generations.rarecandy.pokeutils.codec.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import gg.generations.rarecandy.pokeutils.codec.References;
import gg.generations.rarecandy.pokeutils.codec.TypeTemplates;

import java.util.Map;
import java.util.function.Supplier;

import static gg.generations.rarecandy.pokeutils.codec.schema.BaseSchema.*;

public class V1 extends Schema {
    public V1(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        super.registerTypes(schema, entityTypes, blockEntityTypes);

//        schema.registerType(false, References.PARENT, () -> nullableField("parent", TypeTemplates.STRING));

        schema.registerType(false, References.VARIANT_PARENT, () -> DSL.allWithRemainder(
                nullableField("parent", TypeTemplates.STRING),
                nullableField("details", map(References.VARIANT_DETAILS.in(schema)))
        ));

        schema.registerType(false, References.VECTOR2F, () -> TypeTemplates.FLOAT_LIST);

        schema.registerType(false, References.COLOR, () -> TypeTemplates.STRING);

        schema.registerType(false, References.MATERIAL_REFERENCE, () -> DSL.and(
                nullableField("parent", TypeTemplates.STRING),
                nullableField("shader", TypeTemplates.STRING),
                nullableField("blend", TypeTemplates.STRING),
                nullableField("cull", TypeTemplates.STRING),
                nullableField("effect", TypeTemplates.STRING),
                nullableField("images", map(TypeTemplates.STRING)),
                nullableField("values", map(References.OBJECT_VALUE.in(schema)))
        ));
    }
}
