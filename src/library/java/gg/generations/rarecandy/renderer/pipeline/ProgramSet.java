package gg.generations.rarecandy.renderer.pipeline;

import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL41C;

public record ProgramSet(String vertex, String geometry, String fragment, String compute) {
    public void attachTraditional(int programId) {
        if(vertex != null) Pipeline.attachShader(programId, vertex, GL20C.GL_VERTEX_SHADER);
        if(geometry != null) Pipeline.attachShader(programId, geometry, GL41C.GL_GEOMETRY_SHADER);
        if(fragment != null) Pipeline.attachShader(programId, fragment, GL20C.GL_FRAGMENT_SHADER);
    }
}
