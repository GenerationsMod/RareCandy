package com.pokemod.rarecandy.shader;

import java.util.ArrayList;
import java.util.List;

public class RCSLShader {

    public final List<RCSLMethod> methods;
    public final RCSLMethod vertexMethod;
    public final RCSLMethod fragmentMethod;

    public RCSLShader(RCSLMethod vertexMethod, RCSLMethod fragmentMethod) {
        this.vertexMethod = vertexMethod;
        this.fragmentMethod = fragmentMethod;
        this.methods = new ArrayList<>();
    }
}