package gg.generations.rarecandy.renderer.pipeline.neo.regular;

import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.pipeline.Uniform;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import org.joml.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Builder for creating fully configured Pipeline instances.
 * Handles shader compilation, reflection, uniform population,
 * and binding registration for UBOs, SSBOs, and uniforms.
 */
public class PipelineBuilder {

    private int program;

    private final Map<String, Uniform> uniformMap = new HashMap<>();
    private final Map<Scope, List<UBOBinding>> ubos = new EnumMap<>(Scope.class);
    private final Map<Scope, List<SSBOBinding>> ssbos = new EnumMap<>(Scope.class);
    private final Map<Scope, List<UniformBinding>> uniforms = new EnumMap<>(Scope.class);
    private Consumer<Material> preDraw = material -> {};
    private Consumer<Material> postDraw = material -> {};

    private PipelineBuilder() {
        for (Scope s : Scope.values()) {
            ubos.put(s, new ArrayList<>());
            ssbos.put(s, new ArrayList<>());
            uniforms.put(s, new ArrayList<>());
        }
    }

    // ============================================================
    // Shader compilation & reflection
    // ============================================================

    public PipelineBuilder(String vertexSource, String fragmentSource) {
        super();

        int programId = GL20C.glCreateProgram();
        attachShader(programId, vertexSource, GL20C.GL_VERTEX_SHADER);
        attachShader(programId, fragmentSource, GL20C.GL_FRAGMENT_SHADER);
        linkProgram(programId);
        reflectProgram(programId);
        this.program = programId;
    }

    public PipelineBuilder(String vertexSource, String geometrySource, String fragmentSource) {
        super();
        int programId = GL20C.glCreateProgram();
        attachShader(programId, vertexSource, GL20C.GL_VERTEX_SHADER);
        attachShader(programId, geometrySource, GL32C.GL_GEOMETRY_SHADER);
        attachShader(programId, fragmentSource, GL20C.GL_FRAGMENT_SHADER);
        linkProgram(programId);
        reflectProgram(programId);
        this.program = programId;
    }

    private static void attachShader(int programId, String source, int type) {
        int shader = GL20C.glCreateShader(type);
        GL20C.glShaderSource(shader, source);
        GL20C.glCompileShader(shader);
        if (GL20C.glGetShaderi(shader, GL20C.GL_COMPILE_STATUS) == GL11C.GL_FALSE) {
            String log = GL20C.glGetShaderInfoLog(shader, 8192);
            throw new IllegalStateException("Shader compilation failed for type " + type + ":\n" + log);
        }
        GL20C.glAttachShader(programId, shader);
    }

    private static void linkProgram(int programId) {
        GL20C.glLinkProgram(programId);
        if (GL20C.glGetProgrami(programId, GL20C.GL_LINK_STATUS) == GL11C.GL_FALSE) {
            String log = GL20C.glGetProgramInfoLog(programId, 8192);
            throw new IllegalStateException("Program linking failed:\n" + log);
        }
    }

    /**
     * Reflect active uniforms and populate uniformMap with Uniform objects.
     */
    private void reflectProgram(int programId) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer size = stack.mallocInt(1);
            IntBuffer type = stack.mallocInt(1);

            int uniformCount = GL20C.glGetProgrami(programId, GL20C.GL_ACTIVE_UNIFORMS);
            int maxNameLength = GL20C.glGetProgrami(programId, GL20C.GL_ACTIVE_UNIFORM_MAX_LENGTH);

            for (int i = 0; i < uniformCount; i++) {
                String name = GL20C.glGetActiveUniform(programId, i, maxNameLength, size, type);
                // Remove [0] suffix for array uniforms
                if (name.endsWith("[0]")) {
                    name = name.substring(0, name.length() - 3);
                }
                Uniform u = new Uniform(programId, name, type.get(0), size.get(0));
                uniformMap.put(name, u);
            }
        }
    }

    // ============================================================
    // Binding registration
    // ============================================================

    public PipelineBuilder addUBO(Scope scope, String name, int bindingPoint, BufferSupplier supplier) {
        ubos.computeIfAbsent(scope, s -> new ArrayList<>()).add(new UBOBinding(name, bindingPoint, supplier));
        return this;
    }

    public PipelineBuilder addSSBO(Scope scope, String name, int bindingPoint, BufferSupplier supplier) {
        ssbos.computeIfAbsent(scope, s -> new ArrayList<>()).add(new SSBOBinding(name, bindingPoint, supplier, null, null));
        return this;
    }

    public PipelineBuilder addSSBORange(
            Scope scope,
            String name,
            int bindingPoint,
            BufferSupplier bufferSupplier,
            RangeSupplier offsetSupplier,
            RangeSupplier sizeSupplier) {
        ssbos.get(scope).add(new SSBOBinding(name, bindingPoint, bufferSupplier, offsetSupplier, sizeSupplier));
        return this;
    }

    public PipelineBuilder addUniform(Scope scope, String uniformName, UniformCallback callback) {
        Uniform uniform = uniformMap.get(uniformName);
        if (uniform == null) {
            throw new IllegalStateException("Uniform not found: " + uniformName);
        }
        uniforms.computeIfAbsent(scope, s -> new ArrayList<>()).add(new UniformBinding(uniform, callback));
        return this;
    }

    // ============================================================
    // Automatic uniform binding helpers (autoXXX)
    // ============================================================

    public PipelineBuilder autoSampler2D(Scope scope, String uniformName, int textureUnit, TextureSupplier tex) {
        Uniform u = getAndCheckUniform(uniformName, GL20C.GL_SAMPLER_2D);
        return addUniform(scope, uniformName, (uniform, ctx) -> {
            int textureId = tex.get(ctx.instance(), ctx.object());
            GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + textureUnit);
            GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, textureId);
            uniform.uploadInt(textureUnit);
        });
    }

    public PipelineBuilder autoSampler2DArray(Scope scope, String uniformName, int textureUnit, TextureSupplier tex) {
        Uniform u = getAndCheckUniform(uniformName, GL30C.GL_SAMPLER_2D_ARRAY);
        return addUniform(scope, uniformName, (uniform, ctx) -> {
            int textureId = tex.get(ctx.instance(), ctx.object());
            GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + textureUnit);
            GL11C.glBindTexture(GL30C.GL_TEXTURE_2D_ARRAY, textureId);
            uniform.uploadInt(textureUnit);
        });
    }

    public PipelineBuilder autoMat4(Scope scope, String uniformName, BiFunction<ObjectInstance, RenderObject, Matrix4f> supplier) {
        getAndCheckUniform(uniformName, GL20C.GL_FLOAT_MAT4);
        return addUniform(scope, uniformName, (uniform, ctx) -> uniform.uploadMat4f(supplier.apply(ctx.instance(), ctx.object())));
    }

    public PipelineBuilder autoMat4Array(Scope scope, String uniformName, BiFunction<ObjectInstance, RenderObject, Matrix4f[]> supplier
    ) {
        getAndCheckUniform(uniformName, GL20C.GL_FLOAT_MAT4);
        return addUniform(scope, uniformName, (uniform, ctx) -> {
            Matrix4f[] arr = supplier.apply(ctx.instance(), ctx.object());
            if (arr != null) {
                uniform.uploadMat4fs(arr);
            }
        });
    }

    PipelineBuilder autoMat3(Scope scope, String uniformName, BiFunction<ObjectInstance, RenderObject, Matrix3f> supplier) {
        getAndCheckUniform(uniformName, GL20C.GL_FLOAT_MAT3);
        return addUniform(scope, uniformName, (uniform, ctx) -> uniform.uploadMat3f(supplier.apply(ctx.instance(), ctx.object())));
    }

    public PipelineBuilder autoVec4(Scope scope, String uniformName, BiFunction<ObjectInstance, RenderObject, Vector4f> supplier) {
        getAndCheckUniform(uniformName, GL20C.GL_FLOAT_VEC4);
        return addUniform(scope, uniformName, (uniform, ctx) -> uniform.uploadVec4f(supplier.apply(ctx.instance(), ctx.object())));
    }

    public PipelineBuilder autoVec3(Scope scope, String uniformName, BiFunction<ObjectInstance, RenderObject, Vector3f> supplier) {
        getAndCheckUniform(uniformName, GL20C.GL_FLOAT_VEC3);
        return addUniform(scope, uniformName, (uniform, ctx) -> uniform.uploadVec3f(supplier.apply(ctx.instance(), ctx.object())));
    }

    public PipelineBuilder autoVec2(Scope scope, String uniformName, BiFunction<ObjectInstance, RenderObject, Vector2f> supplier) {
        getAndCheckUniform(uniformName, GL20C.GL_FLOAT_VEC2);
        return addUniform(scope, uniformName, (uniform, ctx) -> uniform.uploadVec2f(supplier.apply(ctx.instance(), ctx.object())));
    }

    public PipelineBuilder autoFloat(Scope scope, String uniformName, BiFunction<ObjectInstance, RenderObject, Float> supplier) {
        getAndCheckUniform(uniformName, GL20C.GL_FLOAT);
        return addUniform(scope, uniformName, (uniform, ctx) -> uniform.uploadFloat(supplier.apply(ctx.instance(), ctx.object())));
    }

    public PipelineBuilder autoInt(Scope scope, String uniformName, BiFunction<ObjectInstance, RenderObject, Integer> supplier) {
        getAndCheckUniform(uniformName, GL20C.GL_INT);
        return addUniform(scope, uniformName, (uniform, ctx) -> uniform.uploadInt(supplier.apply(ctx.instance(), ctx.object())));
    }

    public PipelineBuilder autoBool(Scope scope, String uniformName, BiFunction<ObjectInstance, RenderObject, Boolean> supplier) {
        getAndCheckUniform(uniformName, GL20C.GL_BOOL);
        return addUniform(scope, uniformName, (uniform, ctx) -> uniform.uploadBoolean(supplier.apply(ctx.instance(), ctx.object())));
    }

    private Uniform getAndCheckUniform(String name, int expectedType) {
        Uniform u = uniformMap.get(name);
        if (u == null) throw new IllegalStateException("Uniform not found: " + name);
        if (u.type != expectedType) {
            throw new IllegalStateException(
                "Uniform " + name + " has type " + u.type + " but expected " + expectedType
            );
        }
        return u;
    }

    // ============================================================
    // Build
    // ============================================================

    public Pipeline build() {
        if (program == 0) {
            throw new IllegalStateException("Shader program not created.");
        }
        return new Pipeline(program, uniformMap, ubos, ssbos, uniforms, preDraw, postDraw);
    }

    public PipelineBuilder prePostDraw(Consumer<Material> preDraw, Consumer<Material> postDraw) {
        this.preDraw = preDraw;
        this.postDraw = postDraw;
        return this;
    }
}
