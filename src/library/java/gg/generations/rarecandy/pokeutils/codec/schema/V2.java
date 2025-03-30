package gg.generations.rarecandy.pokeutils.codec.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import gg.generations.rarecandy.pokeutils.codec.References;
import gg.generations.rarecandy.pokeutils.codec.TypeTemplates;

import java.util.Map;
import java.util.function.Supplier;

import static gg.generations.rarecandy.pokeutils.codec.schema.BaseSchema.nullableField;

public class V2 extends Schema {
    public V2(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        super.registerTypes(schema, entityTypes, blockEntityTypes);
//
//        schema.registerType(false, References.VARIANT_DETAILS, () -> DSL.and(
//                nullableField("material", TypeTemplates.STRING),
//                nullableField("hide", TypeTemplates.BOOLEAN),
//                nullableField("transform", References.TRANSFORM.in(schema))
//        ));
    }
}
