package gg.generations.rarecandy.pokeutils.codec.schema;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import gg.generations.rarecandy.pokeutils.codec.References;

import java.util.function.Function;

import static gg.generations.rarecandy.pokeutils.codec.schema.BaseSchema.map;

public class Vector3fFix extends DataFix {
    public Vector3fFix(Schema schema1) {
        super(schema1, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        var type1 = getInputSchema().getType(References.VECTOR3F);
        var type2 = getOutputSchema().getType(References.VECTOR3F);

        return this.writeFixAndRead(this.getClass().getSimpleName(), type1, type2, new Function<Dynamic<?>, Dynamic<?>>() {
            @Override
            public Dynamic<?> apply(Dynamic<?> dynamic) {
                return dynamic;
            }
        });
    }
}
