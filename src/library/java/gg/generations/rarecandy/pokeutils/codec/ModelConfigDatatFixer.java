package gg.generations.rarecandy.pokeutils.codec;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
//import gg.generations.rarecandy.pokeutils.codec.datafix.VariantParentFix;
import gg.generations.rarecandy.pokeutils.codec.datafix.*;
import gg.generations.rarecandy.pokeutils.codec.schema.BaseSchema;
import gg.generations.rarecandy.pokeutils.codec.schema.V1;
import gg.generations.rarecandy.pokeutils.codec.schema.V2;
import gg.generations.rarecandy.pokeutils.material.MaterialReference;

import java.util.concurrent.Executors;

public class ModelConfigDatatFixer {
    public static DataFixer createDataFixer() {
        var executor = Executors.newSingleThreadExecutor(Thread::new);


        var builder = new DataFixerBuilder(2);
        var base = builder.addSchema(0, BaseSchema::new);

        var schema1 = builder.addSchema(1, V1::new);
//        builder.addFixer(new ParentFix(schema1));
        builder.addFixer(new VariantParentFix(schema1));
        builder.addFixer(new MaterialReferenceFix(schema1));
        builder.addFixer(new CodecRewriteFix<>(schema1, References.VECTOR2F, ModelConfigCodecs.VECTOR2F_OLD, JomlCodecs.VECTOR2F));
//        builder.addFixer(new CodecRewriteFix<>(schema1, References.TRANSFORM, JomlCodecs.TRANSFORM_OLD, JomlCodecs.TRANSFORM));
        builder.addFixer(new CodecRewriteFix<>(schema1, References.COLOR, ModelConfigCodecs.OLD_COLOR_CODEC, MaterialReference.COLOR_CODEC));
        var schema2 =builder.addSchema(2, V2::new);
//        builder.addFixer(new CodecRewriteFix<>(schema2, References.VARIANT_DETAILS, ModelConfigCodecs.VARIANT_DETAILS_OLD, ModelConfigCodecs.VARIANT_DETAILS));


        return builder.buildUnoptimized();
    }
}
