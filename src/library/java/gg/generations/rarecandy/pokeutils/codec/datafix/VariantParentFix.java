package gg.generations.rarecandy.pokeutils.codec.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import gg.generations.rarecandy.pokeutils.codec.References;
import gg.generations.rarecandy.pokeutils.codec.TypeTemplates;

import java.util.function.Function;
import java.util.stream.Stream;

import static com.mojang.datafixers.DSL.or;
import static gg.generations.rarecandy.pokeutils.codec.schema.BaseSchema.map;
import static gg.generations.rarecandy.pokeutils.codec.schema.BaseSchema.nullableField;

public class VariantParentFix extends DataFix {
    public VariantParentFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        var type1 = getInputSchema().getType(References.VARIANT_PARENT);
//        var type2 = getOutputSchema().getType(References.VARIANT_PARENT);

        return this.writeFixAndRead(this.getClass().getSimpleName(), type1, type1, new Function<Dynamic<?>, Dynamic<?>>() {
            @Override
            public Dynamic<?> apply(Dynamic<?> dynamic) {
                var map = dynamic.emptyMap();

                var parent = dynamic.get("parent").asString().result();

                if(parent.isEmpty()) parent = dynamic.get("inherits").asString().result();

                if (parent.isPresent()) {
                    dynamic = dynamic.remove("parent").remove("inherits");
                    map = map.set("parent", dynamic.createString(parent.get()));
                }

                map = map.set("details", dynamic);

//                for (var pair : dynamic.asMapOpt().result().orElse(Stream.empty()).toList()) {
//                    var key = pair.getFirst().asString().result();
//
//                    if (key.isPresent()) {
//                        var details = pair.getSecond();
//
//                        var value = dynamic.emptyMap();
//
//                        var parent = details.get("parent").asString().result();
//
//                        if(parent.isEmpty()) parent = details.get("inherits").asString().result();
//
//                        if (parent.isPresent()) {
//                            details = details.remove("parent").remove("inherits");
//                            value = value.set("parent", dynamic.createString(parent.get()));
//                        }
//
//                        map = map.set(key.get(), value.set("details", details));
//                    }
//                }

//                System.out.println(map);

                return map;
            }
        });
    }
}

