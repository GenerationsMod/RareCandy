package gg.generations.rarecandy.pokeutils.codec.datafix;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import gg.generations.rarecandy.pokeutils.codec.ModelConfigCodecs;
import gg.generations.rarecandy.pokeutils.codec.References;
import gg.generations.rarecandy.pokeutils.material.MaterialReference;

import static gg.generations.rarecandy.pokeutils.codec.schema.BaseSchema.map;

public class ColorFix extends DataFix {
    public ColorFix(Schema output) {
        super(output, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        var type1 = References.COLOR.in(getInputSchema()).toSimpleType();

        return this.writeFixAndRead(this.getClass().getSimpleName(), type1, type1, dynamic -> {

            var color = ModelConfigCodecs.VECTOR3F_OLD.parse(dynamic).result();

            if(color.isPresent()) {
                 dynamic = dynamic.createString(MaterialReference.colorToString(color.get()));
            }

            return dynamic;
        });
    }
}
