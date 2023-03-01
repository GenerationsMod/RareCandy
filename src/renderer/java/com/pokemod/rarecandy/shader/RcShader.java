package com.pokemod.rarecandy.shader;

import java.util.ArrayList;
import java.util.List;

public class RcShader {

    public final List<RcMethod> methods;
    public final RcMethod vertexMethod;
    public final RcMethod fragmentMethod;

    public RcShader(RcMethod vertexMethod, RcMethod fragmentMethod) {
        this.vertexMethod = vertexMethod;
        this.fragmentMethod = fragmentMethod;
        this.methods = new ArrayList<>();
    }
}