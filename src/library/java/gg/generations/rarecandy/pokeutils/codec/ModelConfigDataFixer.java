package gg.generations.rarecandy.pokeutils.codec;

import com.mojang.datafixers.*;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;

import java.util.Optional;

public class ModelConfigDataFixer {


    public static void register() {
        var builder = new DataFixerBuilder(2);
        builder.addFixer(new DataFix() {
            @Override
            protected TypeRewriteRule makeRule() {
                return new TypeRewriteRule() {
                    @Override
                    public <A> Optional<RewriteResult<A, ?>> rewrite(Type<A> type) {
                        return Optional.empty();
                    }
                };
            }
        });
        builder.addSchema(new Schema());

        builder.addFixer(new DataFix() {
            @Override
            protected TypeRewriteRule makeRule() {
                return null;
            }
        });
    }

}
