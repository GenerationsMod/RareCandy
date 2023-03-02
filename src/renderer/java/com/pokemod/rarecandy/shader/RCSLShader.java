package com.pokemod.rarecandy.shader;

import java.util.List;

public class RCSLShader {

    public final List<RCSLMethod> methods;
    public final RCSLMethod vertexMethod;
    public final RCSLMethod fragmentMethod;
    private final String name;

    public RCSLShader(String shaderName, RCSLMethod vertexMethod, RCSLMethod fragmentMethod, List<RCSLMethod> methods) {
        this.name = shaderName;
        this.vertexMethod = vertexMethod;
        this.fragmentMethod = fragmentMethod;
        this.methods = methods;
    }

    @Override
    public String toString() {
        return "\"" + name + "\" RCSLShader{" +
                "methods=" + methods +
                ", vertexMethod=" + vertexMethod +
                ", fragmentMethod=" + fragmentMethod +
                '}';
    }
}