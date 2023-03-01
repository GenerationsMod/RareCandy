package com.pokemod.rarecandy.shader;

import java.util.ArrayList;
import java.util.List;

public class RcMethod {

    public final Type type;
    public final List<RcParameter> params = new ArrayList<>();
    public String methodBody;

    public RcMethod(Type type) {
        this.type = type;
    }

    public static Type getType(String methodName) {
        return switch (methodName) {
            case "vertMain" -> Type.VERTEX;
            case "fragMain" -> Type.FRAGMENT;
            default -> Type.OTHER;
        };
    }

    public enum Type {
        VERTEX, FRAGMENT, OTHER
    }
}