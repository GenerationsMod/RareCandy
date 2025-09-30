package gg.generations.rarecandy.renderer.pipeline;

import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.pipeline.util.*;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import org.joml.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.*;
import java.util.function.BiFunction;

public class Pipeline {
    private final int program;
    private final Map<String, Uniform> uniforms;
    private final Map<Scope, List<UBOBinding>> ubos;
    private final Map<Scope, List<SSBOBinding>> ssbos;
    private final Map<Scope, List<UniformBinding>> uniformBindings;

    public Pipeline(int program, Map<String, Uniform> uniforms, Map<Scope, List<UBOBinding>> ubos,
                               Map<Scope, List<SSBOBinding>> ssbos, Map<Scope, List<UniformBinding>> uniformBindings) {
        this.program = program;
        this.uniforms = uniforms;
        this.ubos = ubos;
        this.ssbos = ssbos;
        this.uniformBindings = uniformBindings;
    }

    public void useProgram() {
        GL20C.glUseProgram(program);
    }

    public void bindGlobal(ObjectInstance instance, RenderObject object) {
        bindScope(Scope.GLOBAL, instance, object);
    }

    public void bindInstance(ObjectInstance instance, RenderObject object) {
        bindScope(Scope.INSTANCE, instance, object);
    }

    public void bindModel(ObjectInstance instance, RenderObject object) {
        bindScope(Scope.MODEL, instance, object);
    }

    private void bindScope(Scope scope, ObjectInstance instance, RenderObject object) {
        bindUBOs(scope, instance, object);
        bindSSBOs(scope, instance, object);
        bindUniforms(scope, instance, object);
    }

    private void bindUBOs(Scope scope, ObjectInstance instance, RenderObject object) {
        List<UBOBinding> list = ubos.get(scope);
        if (list == null) return;
        for (UBOBinding b : list) {
            int id = b.bufferSupplier().get(instance, object);
            if (id != 0) {
                GL30C.glBindBufferBase(GL31C.GL_UNIFORM_BUFFER, b.bindingPoint(), id);
            }
        }
    }

    // ============================================================
    // SSBO binding
    // ============================================================

    private void bindSSBOs(Scope scope, ObjectInstance instance, RenderObject object) {
        List<SSBOBinding> list = ssbos.get(scope);
        if (list == null) return;
        for (SSBOBinding b : list) {
            int id = b.bufferSupplier().get(instance, object);
            if (id != 0) {
                if (b.offsetSupplier() != null && b.sizeSupplier() != null) {
                    long offset = b.offsetSupplier().get(instance, object);
                    long size = b.sizeSupplier().get(instance, object);
                    GL30C.glBindBufferRange(GL43C.GL_SHADER_STORAGE_BUFFER, b.bindingPoint(), id, offset, size);
                } else {
                    GL30C.glBindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, b.bindingPoint(), id);
                }
            }
        }
    }

    // ============================================================
    // Uniform binding
    // ============================================================

    private void bindUniforms(Scope scope, ObjectInstance instance, RenderObject object) {
        List<UniformBinding> list = uniformBindings.get(scope);
        if (list == null) return;
        UniformUploadContext ctx = new UniformUploadContext(program, instance, object);
        for (UniformBinding b : list) {
            b.callback().apply(b.uniform(), ctx);
        }
    }

    // ============================================================
    // Accessors
    // ============================================================

    public Uniform getUniform(String name) {
        return uniforms.get(name);
    }

    public static abstract class Builder<T extends Pipeline, V extends Builder<T, V>> {

        protected final int program;

        protected final Map<String, Uniform> uniformMap = new HashMap<>();
        protected final Map<Scope, List<UBOBinding>> ubos = new EnumMap<>(Scope.class);
        protected final Map<Scope, List<SSBOBinding>> ssbos = new EnumMap<>(Scope.class);
        protected final Map<Scope, List<UniformBinding>> uniforms = new EnumMap<>(Scope.class);

        protected Builder(int program) {
            for (Scope s : Scope.values()) {
                ubos.put(s, new ArrayList<>());
                ssbos.put(s, new ArrayList<>());
                uniforms.put(s, new ArrayList<>());
            }

            reflectProgram(program);
            this.program = program;
        }

        public static void attachShader(int programId, String source, int type) {
            int shader = GL20C.glCreateShader(type);
            GL20C.glShaderSource(shader, source);
            GL20C.glCompileShader(shader);
            if (GL20C.glGetShaderi(shader, GL20C.GL_COMPILE_STATUS) == GL11C.GL_FALSE) {
                String log = GL20C.glGetShaderInfoLog(shader, 8192);
                throw new IllegalStateException("Shader compilation failed for type " + type + ":\n" + log);
            }
            GL20C.glAttachShader(programId, shader);
        }

        public static void linkProgram(int programId) {
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

        public V addUBO(Scope scope, String name, int bindingPoint, BufferSupplier supplier) {
            ubos.computeIfAbsent(scope, s -> new ArrayList<>()).add(new UBOBinding(name, bindingPoint, supplier));
            return (V) this;
        }

        public V addSSBO(Scope scope, String name, int bindingPoint, BufferSupplier supplier) {
            ssbos.computeIfAbsent(scope, s -> new ArrayList<>()).add(new SSBOBinding(name, bindingPoint, supplier, null, null));
            return (V) this;
        }

        public V addSSBORange(
                Scope scope,
                String name,
                int bindingPoint,
                BufferSupplier bufferSupplier,
                RangeSupplier offsetSupplier,
                RangeSupplier sizeSupplier) {
            ssbos.get(scope).add(new SSBOBinding(name, bindingPoint, bufferSupplier, offsetSupplier, sizeSupplier));
            return (V) this;
        }

        public V addUniform(Scope scope, String uniformName, UniformCallback callback) {
            Uniform uniform = uniformMap.get(uniformName);
            if (uniform == null) {
                throw new IllegalStateException("Uniform not found: " + uniformName);
            }
            uniforms.computeIfAbsent(scope, s -> new ArrayList<>()).add(new UniformBinding(uniform, callback));
            return (V) this;
        }

        // ============================================================
        // Automatic uniform binding helpers (autoXXX)
        // ============================================================

        public V autoImage2D(Scope scope, String uniformName, int textureUnit, TextureSupplier tex) {
            Uniform u = getAndCheckUniform(uniformName, GL43C.GL_IMAGE_2D);
            return addUniform(scope, uniformName, (uniform, ctx) -> {
                var texture = tex.getTexture(ctx.instance(), ctx.object());
                uniform.uploadImage2D(texture, textureUnit);
            });
        }

        public V autoSampler2D(Scope scope, String uniformName, int textureUnit, TextureIdSupplier tex) {
            Uniform u = getAndCheckUniform(uniformName, GL20C.GL_SAMPLER_2D);
            return addUniform(scope, uniformName, (uniform, ctx) -> {
                int textureId = tex.get(ctx.instance(), ctx.object());
                GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + textureUnit);
                GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, textureId);
                uniform.uploadInt(textureUnit);
            });
        }

        public V autoSampler2DArray(Scope scope, String uniformName, int textureUnit, TextureIdSupplier tex) {
            Uniform u = getAndCheckUniform(uniformName, GL30C.GL_SAMPLER_2D_ARRAY);
            return addUniform(scope, uniformName, (uniform, ctx) -> {
                int textureId = tex.get(ctx.instance(), ctx.object());
                GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + textureUnit);
                GL11C.glBindTexture(GL30C.GL_TEXTURE_2D_ARRAY, textureId);
                uniform.uploadInt(textureUnit);
            });
        }

        public V autoMat4(Scope scope, String uniformName, BiFunction<ObjectInstance, RenderObject, Matrix4f> supplier) {
            getAndCheckUniform(uniformName, GL20C.GL_FLOAT_MAT4);
            return addUniform(scope, uniformName, (uniform, ctx) -> uniform.uploadMat4f(supplier.apply(ctx.instance(), ctx.object())));
        }

        public V autoMat4Array(Scope scope, String uniformName, BiFunction<ObjectInstance, RenderObject, Matrix4f[]> supplier
        ) {
            getAndCheckUniform(uniformName, GL20C.GL_FLOAT_MAT4);
            return addUniform(scope, uniformName, (uniform, ctx) -> {
                Matrix4f[] arr = supplier.apply(ctx.instance(), ctx.object());
                if (arr != null) {
                    uniform.uploadMat4fs(arr);
                }
            });
        }

        V autoMat3(Scope scope, String uniformName, BiFunction<ObjectInstance, RenderObject, Matrix3f> supplier) {
            getAndCheckUniform(uniformName, GL20C.GL_FLOAT_MAT3);
            return addUniform(scope, uniformName, (uniform, ctx) -> uniform.uploadMat3f(supplier.apply(ctx.instance(), ctx.object())));
        }

        public V autoVec4(Scope scope, String uniformName, BiFunction<ObjectInstance, RenderObject, Vector4f> supplier) {
            getAndCheckUniform(uniformName, GL20C.GL_FLOAT_VEC4);
            return addUniform(scope, uniformName, (uniform, ctx) -> uniform.uploadVec4f(supplier.apply(ctx.instance(), ctx.object())));
        }

        public V autoVec3(Scope scope, String uniformName, BiFunction<ObjectInstance, RenderObject, Vector3f> supplier) {
            getAndCheckUniform(uniformName, GL20C.GL_FLOAT_VEC3);
            return addUniform(scope, uniformName, (uniform, ctx) -> uniform.uploadVec3f(supplier.apply(ctx.instance(), ctx.object())));
        }

        public V autoVec2(Scope scope, String uniformName, BiFunction<ObjectInstance, RenderObject, Vector2f> supplier) {
            getAndCheckUniform(uniformName, GL20C.GL_FLOAT_VEC2);
            return addUniform(scope, uniformName, (uniform, ctx) -> uniform.uploadVec2f(supplier.apply(ctx.instance(), ctx.object())));
        }

        public V autoFloat(Scope scope, String uniformName, BiFunction<ObjectInstance, RenderObject, Float> supplier) {
            getAndCheckUniform(uniformName, GL20C.GL_FLOAT);
            return addUniform(scope, uniformName, (uniform, ctx) -> uniform.uploadFloat(supplier.apply(ctx.instance(), ctx.object())));
        }

        public V autoInt(Scope scope, String uniformName, BiFunction<ObjectInstance, RenderObject, Integer> supplier) {
            getAndCheckUniform(uniformName, GL20C.GL_INT);
            return addUniform(scope, uniformName, (uniform, ctx) -> uniform.uploadInt(supplier.apply(ctx.instance(), ctx.object())));
        }

        public V autoBool(Scope scope, String uniformName, BiFunction<ObjectInstance, RenderObject, Boolean> supplier) {
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

        public T build() {
            if (program == 0) {
                throw new IllegalStateException("Shader program not created.");
            }
            return createPipeline();
        }

        public abstract T createPipeline();

        public static <T> Map<Scope, List<T>> deepCopy(Map<Scope, List<T>> src) {
            Map<Scope, List<T>> result = new EnumMap<>(Scope.class);
            for (var e : src.entrySet()) {
                result.put(e.getKey(), List.copyOf(e.getValue()));
            }
            return result;
        }
    }
}
