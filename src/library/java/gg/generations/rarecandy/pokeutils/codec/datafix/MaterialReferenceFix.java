package gg.generations.rarecandy.pokeutils.codec.datafix;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import gg.generations.rarecandy.pokeutils.codec.References;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;

import static gg.generations.rarecandy.pokeutils.codec.schema.BaseSchema.map;

public class MaterialReferenceFix extends DataFix {
    public MaterialReferenceFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        var type1 = map(References.MATERIAL_REFERENCE.in(getInputSchema())).toSimpleType();
        var type2 = map(References.MATERIAL_REFERENCE.in(getOutputSchema())).toSimpleType();

        return this.writeFixAndRead(this.getClass().getSimpleName(), type1, type1, new Function<Dynamic<?>, Dynamic<?>>() {
            @Override
            public Dynamic<?> apply(Dynamic<?> dynamic) {

                dynamic = applyChanges(dynamic);

                dynamic = proccessValues(dynamic);

                System.out.println("Blep: " + dynamic);

                return dynamic;
            }
        });
    }

    private Dynamic<?> proccessValues(Dynamic<?> dynamic) {
        var values = dynamic.get("values").result().map(a -> a.getMapValues()).map(a -> a.result()).filter(a -> a.isPresent()).map(a -> a.get()).orElse(Collections.emptyMap());

        if(!values.isEmpty()) {
            var map = dynamic.emptyMap();

            for(var entry : values.entrySet()) {
                map.set(entry.getKey().asString().result().get(), processValue(dynamic));
            }

            dynamic = dynamic.set("values", map);
        }

        return dynamic;
    }

    private Dynamic<?> processValue(Dynamic<?> dynamic) {
        var type = dynamic.get("type").asString().result();

        if(type.isPresent()) {
            return dynamic.get("value").get().getOrThrow(false, System.out::println);
        } else return dynamic;
    }

    private static <T> Dynamic<T> applyChanges(Dynamic<T> dynamic) {
        var modifer = applyTypeModifier(dynamic);
        if (modifer.isPresent()) return modifer.get();

        var inherits = dynamic.remove("inherits");

        if (inherits.asString().result().isPresent()) {
            dynamic.set("parent", inherits);
        }

        return dynamic;
    }

    private static <T> Optional<Dynamic<T>> applyTypeModifier(Dynamic<T> dynamic) {
        var type = dynamic.get("type").asString().result();

        if (type.isPresent()) {
            var material = new Dynamic<>(dynamic.getOps());

            var texturesToAdd = new HashMap<Dynamic<?>, Dynamic<?>>();
            var valuesToAdd = new HashMap<Dynamic<?>, Dynamic<?>>();

            dynamic.get("texture").asString().result().ifPresent(a -> texturesToAdd.put(dynamic.createString("diffuse"), dynamic.createString(a)));

            switch (type.get()) {
                case "masked" -> {
                    var color = dynamic.get("color").result();

                    color.ifPresent(colorDynamic -> {
                        valuesToAdd.put(dynamic.createString("color"), colorDynamic);
                    });

                    dynamic.get("mask").asString().result().ifPresent(texture -> {
                        texturesToAdd.put(dynamic.createString("mask"), dynamic.createString(texture));
                    });

                    material = material.set("shader", dynamic.createString("masked"));
                }
                case "transparent" -> material = material.set("blend", dynamic.createString("regular"));
                case "cull" -> material = material.set("cull", dynamic.createString("forward"));
                case "unlit_cull" -> {
                    material = material.set("cull", dynamic.createString("forward"));
                    valuesToAdd.put(dynamic.createString("useLight"), dynamic.createBoolean(false));
                }
                case "unlit" -> valuesToAdd.put(dynamic.createString("useLight"), dynamic.createBoolean(false));
            }

            if (!valuesToAdd.isEmpty()) material = material.set("images", dynamic.createMap(texturesToAdd));

            if (!valuesToAdd.isEmpty()) material = material.set("values", dynamic.createMap(valuesToAdd));

            return Optional.of(material);
        }

        return Optional.empty();
    }
}
