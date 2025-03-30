package gg.generations.rarecandy.pokeutils.codec;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.templates.TypeTemplate;

public class TypeTemplates {
    public static final TypeTemplate FLOAT = DSL.constType(DSL.floatType());
    public static final TypeTemplate BOOLEAN = DSL.constType(DSL.bool());
    public static final TypeTemplate STRING = DSL.constType(DSL.string());
    public static final TypeTemplate STRING_LIST = DSL.list(STRING);
    public static final TypeTemplate FLOAT_LIST = DSL.list(FLOAT);
}
