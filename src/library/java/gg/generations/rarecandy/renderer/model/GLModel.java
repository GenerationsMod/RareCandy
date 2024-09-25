package gg.generations.rarecandy.renderer.model;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.loading.Attribute;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.model.material.PipelineRegistry;
import gg.generations.rarecandy.renderer.pipeline.Pipeline;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import gg.generations.rarecandy.renderer.rendering.RenderStage;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Consumer;

import static gg.generations.rarecandy.renderer.loading.ModelLoader.generateVao;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;

public class GLModel implements RenderModel {
    public List<MeshDrawCommand> meshDrawCommands = new ArrayList<>();

    private Vector3f dimensions = new Vector3f();

    public int vao = -1;
    public int ebo = -1;
    public int vbo = -1;

    public void runDrawCalls() {
        for (var drawCommand : meshDrawCommands) {
            drawCommand.run();
        }
    }

    public GLModel(ByteBuffer vertexBuffer, ByteBuffer indexBuffer, List<Runnable> glCalls, int indexSize, int gltType, List<Attribute> attributes) {
        var model = this;

        glCalls.add(() -> {
            generateVao(model, vertexBuffer, attributes);
            GL30.glBindVertexArray(model.vao);
            model.ebo = GL15.glGenBuffers();
            glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, model.ebo);
            glBufferData(GL15C.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
            model.meshDrawCommands.add(new MeshDrawCommand(model.vao, GL11.GL_TRIANGLES, gltType, model.ebo, indexSize));
            MemoryUtil.memFree(vertexBuffer);
            MemoryUtil.memFree(indexBuffer);
        });
    }

    @Override
    public int hashCode() {
        return meshDrawCommands != null ? meshDrawCommands.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        var glModel = (GLModel) o;
        return Objects.equals(meshDrawCommands, glModel.meshDrawCommands);
    }

    @Override
    public void close() {
        if(vao > -1) {
            GL30.glDeleteVertexArrays(vao);
            vao = -1;
        }
        if(ebo > -1) {
            GL30.glDeleteBuffers(ebo);
            ebo = -1;
        }
        if(vbo > -1) {
            GL30.glDeleteBuffers(vbo);
            vbo = -1;
        }

        meshDrawCommands.clear();
    }

    public Vector3f getDimensions() {
        return dimensions;
    }

    private Map<Material, List<Consumer<Pipeline>>> EMPTY = Collections.emptyMap();

    public <T extends RenderObject> void render(List<ObjectInstance> instances, T object) {


        Map<RenderStage, Map<Material, List<Consumer<Pipeline>>>> map = new HashMap<>();

        for (var instance : instances) {
            if (object.shouldRender(instance)) {
                continue;
            }

            var material = object.getMaterial(instance.variant());

            var stage = RenderStage.SOLID;

            if(material.blendType() != BlendType.None) stage = RenderStage.TRANSPARENT;

            var stages = map.computeIfAbsent(stage, s -> new HashMap<>());

            stages.computeIfAbsent(material, a -> new ArrayList<>()).add(pipeline -> {
                pipeline.updateOtherUniforms(instance, object);
                pipeline.updateTexUniforms(instance, object);
                runDrawCalls();
            });
        }



        map.getOrDefault(RenderStage.SOLID, EMPTY).forEach(GLModel::render);
        map.getOrDefault(RenderStage.TRANSPARENT, EMPTY).forEach(GLModel::render);
    }

    public <T extends RenderObject> void render(ObjectInstance instance, T object) {
        Map<Material, List<Consumer<Pipeline>>> solidMap = new HashMap<>();
        Map<Material, List<Consumer<Pipeline>>> transparentMap = new HashMap<>();

        if (object.shouldRender(instance)) return;

        var material = object.getMaterial(instance.variant());

        var stage = RenderStage.SOLID;

        if(material.blendType() != BlendType.None) stage = RenderStage.TRANSPARENT;
        var stages = stage == RenderStage.SOLID ? solidMap : transparentMap;

        stages.computeIfAbsent(material, a -> new ArrayList<>()).add(pipeline -> {
            pipeline.updateOtherUniforms(instance, object);
            pipeline.updateTexUniforms(instance, object);
            runDrawCalls();
        });

        solidMap.forEach(GLModel::render);
        transparentMap.forEach(GLModel::render);
    }

    private static void render(Material k, List<Consumer<Pipeline>> v) {
        var pl = PipelineRegistry.get(k.getPipeline());

        if(pl == null) return;

        pl.bind(k);
        v.forEach(a -> a.accept(pl));
        pl.unbind(k);
    }

}
