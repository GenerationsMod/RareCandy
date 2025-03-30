package gg.generations.rarecandy.pokeutils.codec.datafix;

import com.google.gson.JsonElement;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;

import java.util.Optional;
import java.util.function.Function;

public class CodecRewriteFix<T> extends DataFix {
    private final String name;
    private final Codec<T> oldCodec;
    private final Codec<T> newCodec;
    private final DSL.TypeReference reference;

    public CodecRewriteFix(Schema outputSchema, DSL.TypeReference reference, Codec<T> oldCodec, Codec<T> newCodec) {
        super(outputSchema, true);
        this.reference = reference;
        this.oldCodec = oldCodec;
        this.newCodec = newCodec;
        this.name = "CodecRewriteFix_" + reference.typeName();
    }

    @Override
    protected TypeRewriteRule makeRule() {
        var inputType = getInputSchema().getType(reference);
        var outputType = getOutputSchema().getType(reference);

        return writeFixAndRead(name, inputType, inputType, this::rewrite);
    }

    private Dynamic<?> rewrite(Dynamic<?> input) {
        System.out.println("Converting: " + reference.typeName());

        System.out.println("    Before:"  + input);

        var output = oldCodec.parse(input)
            .result()
            .map(new Function<T, DataResult<JsonElement>>() {
                @Override
                public DataResult<JsonElement> apply(T t) {
                    return newCodec.encodeStart(JsonOps.INSTANCE, t);
                }
            })
            .flatMap(DataResult::result).map((Function<JsonElement, Dynamic<?>>) jsonElement -> new Dynamic<>(JsonOps.INSTANCE, jsonElement))
            .orElse(input);
        System.out.println("    After:"  + output);

        return input;
    }
}