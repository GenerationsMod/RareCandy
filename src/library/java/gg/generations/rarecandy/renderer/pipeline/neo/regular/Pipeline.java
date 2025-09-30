package gg.generations.rarecandy.renderer.pipeline.neo.regular;

import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.pipeline.Uniform;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import org.lwjgl.opengl.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * A fully constructed and immutable pipeline instance.
 * Holds shader program, uniform map, and binding declarations.
 * Provides methods to bind resources by scope for rendering.
 */
public final class Pipeline {

    private final int program;
    private final Map<String, Uniform> uniforms;
    private final Map<Scope, List<UBOBinding>> ubos;
    private final Map<Scope, List<SSBOBinding>> ssbos;
    private final Map<Scope, List<UniformBinding>> uniformBindings;
    private final Consumer<Material> preDraw;
    private final Consumer<Material> postDraw;

    public Pipeline(
            int program,
            Map<String, Uniform> uniforms,
            Map<Scope, List<UBOBinding>> ubos,
            Map<Scope, List<SSBOBinding>> ssbos,
            Map<Scope, List<UniformBinding>> uniformBindings,
            Consumer<Material> preDraw,
            Consumer<Material> postDraw
    ) {
        this.program = program;
        this.uniforms = Map.copyOf(uniforms);
        this.ubos = deepCopy(ubos);
        this.ssbos = deepCopy(ssbos);
        this.uniformBindings = deepCopy(uniformBindings);
        this.preDraw = preDraw;
        this.postDraw = postDraw;
    }

    private static <T> Map<Scope, List<T>> deepCopy(Map<Scope, List<T>> src) {
        Map<Scope, List<T>> result = new EnumMap<>(Scope.class);
        for (var e : src.entrySet()) {
            result.put(e.getKey(), List.copyOf(e.getValue()));
        }
        return result;
    }

    public void preDraw(Material material) {
        preDraw.accept(material);
    }

    public void postDraw(Material material) {
        postDraw.accept(material);
    }

    // ============================================================
    // Shader program activation
    // ============================================================

    public void useProgram() {
        GL20C.glUseProgram(program);
    }



    // ============================================================
    // Scope binding
    // ============================================================

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

    // ============================================================
    // UBO binding
    // ============================================================

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

    public int getProgram() {
        return program;
    }

    public Uniform getUniform(String name) {
        return uniforms.get(name);
    }
}
