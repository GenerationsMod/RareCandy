package gg.generations.rarecandy.arceus.model;

import gg.generations.rarecandy.legacy.pipeline.ShaderProgram;

import java.io.Closeable;

public interface Material extends Closeable {
    ShaderProgram getProgram();
}
