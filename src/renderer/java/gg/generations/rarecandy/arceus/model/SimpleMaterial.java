package gg.generations.rarecandy.arceus.model;

import gg.generations.rarecandy.legacy.pipeline.ShaderProgram;

import java.io.IOException;

public record SimpleMaterial(ShaderProgram program) implements Material {
    @Override
    public ShaderProgram getProgram() {
        return program;
    }

    @Override
    public String name() {
        return "simple";
    }

    @Override
    public void close() throws IOException {

    }
}
