package com.pokemod.rarecandy.shader;

import java.util.ArrayList;
import java.util.List;

public class RCSLMethod {

    public final Type methodType;
    public final String returnType;
    public final List<RCSLParam> params = new ArrayList<>();
    public String body;

    public RCSLMethod(Type methodType, String returnType) {
        this.methodType = methodType;
        this.returnType = returnType;
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