package gg.generations.rarecandy.pokeutils.codec.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import gg.generations.rarecandy.pokeutils.codec.References;

import java.util.function.Function;

public class ParentFix extends DataFix {
    public ParentFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        var type1 = getInputSchema().getType(References.PARENT);
//        var type2 = getOutputSchema().getType(References.VARIANT_PARENT);

        return this.writeFixAndRead(this.getClass().getSimpleName(), type1, type1, new Function<Dynamic<?>, Dynamic<?>>() {
            @Override
            public Dynamic<?> apply(Dynamic<?> dynamic) {
                System.out.println(dynamic);

                var inherits = dynamic.get("inherits").asString().result();
                if (inherits.isPresent()) {
                    dynamic = dynamic.remove("inherits").set("parent", dynamic.createString(inherits.get()));
                }

                return dynamic;
            }
        });
    }
}
