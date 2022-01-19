package cf.hydos.engine.rendering.resources;

import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL20.glCreateProgram;

public class ShaderResource {
    private final int program;
    private final HashMap<String, Integer> uniforms;
    private final ArrayList<String> uniformNames;
    private final ArrayList<String> uniformTypes;
    private int refCount;

    public ShaderResource() {
        this.program = glCreateProgram();
        this.refCount = 1;

        if (program == 0) {
            System.err.println("Shader creation failed: Could not find valid memory location in constructor");
            System.exit(1);
        }

        uniforms = new HashMap<>();
        uniformNames = new ArrayList<>();
        uniformTypes = new ArrayList<>();
    }

    @Override
    protected void finalize() {
        glDeleteBuffers(program);
    }

    public void AddReference() {
        refCount++;
    }

    public boolean RemoveReference() {
        refCount--;
        return refCount == 0;
    }

    public int GetProgram() {
        return program;
    }

    public HashMap<String, Integer> GetUniforms() {
        return uniforms;
    }

    public ArrayList<String> GetUniformNames() {
        return uniformNames;
    }

    public ArrayList<String> GetUniformTypes() {
        return uniformTypes;
    }
}
