package gg.generations.rarecandy.renderer.loading;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.pokeutils.CullType;
import gg.generations.rarecandy.pokeutils.Pair;
import gg.generations.rarecandy.renderer.components.MeshObject;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.model.GLModel;
import gg.generations.rarecandy.renderer.model.Variant;
import gg.generations.rarecandy.renderer.model.material.Material;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gg.generations.rarecandy.renderer.loading.ModelLoader.calculateVertexSize;

public class PlaneGenerator {
    public static Pair<List<Runnable>, MultiRenderObject<MeshObject>> generatePlane(float width, float length) {

        var attributes = List.of(Attribute.POSITION, Attribute.TEXCOORD);

        var vertexlength = calculateVertexSize(attributes);
        var amount = 4;

        var vertexBuffer = MemoryUtil.memAlloc(vertexlength * amount)
                .putFloat(-width / 2).putFloat(0.0f).putFloat(-length / 2).putFloat(0.0f).putFloat(0.0f)
                .putFloat(width / 2).putFloat(0.0f).putFloat(-length / 2).putFloat(1.0f).putFloat(0.0f)
                .putFloat(-width / 2).putFloat(0.0f).putFloat(length / 2).putFloat(0.0f).putFloat(1.0f)
                .putFloat(width / 2).putFloat( 0.0f).putFloat(length / 2).putFloat(1.0f).putFloat(1.0f)
                .flip();


        var indexBuffer = MemoryUtil.memAlloc(6 * 2)
                .putShort((short) 0).putShort((short) 1).putShort((short) 2)
                .putShort((short) 1).putShort((short) 3).putShort((short) 2)
                .flip();


        var glCalls = new ArrayList<Runnable>();
        var model = new GLModel(vertexBuffer, indexBuffer, glCalls, 6, GL11.GL_UNSIGNED_SHORT, attributes);

        var obj = new MultiRenderObject<MeshObject>();

        var mesh = new MeshObject();
        mesh.setup(Map.of("plane", new Variant(new Material("plane", new HashMap<>(), new HashMap<>(), CullType.None, BlendType.None, "plane"))), model, "plane");
        obj.add(mesh);

        return new Pair<>(glCalls, obj);
    }


    public static Pair<GLModel, List<Runnable>> generatePlaneRunnable(float width, float length) {
        var attributes = List.of(Attribute.POSITION, Attribute.TEXCOORD);

        var vertexlength = calculateVertexSize(attributes);
        var amount = 4;

        var vertexBuffer = MemoryUtil.memAlloc(vertexlength * amount)
                .putFloat(-width / 2).putFloat(0.0f).putFloat(-length / 2).putFloat(0.0f).putFloat(0.0f)
                .putFloat(width / 2).putFloat(0.0f).putFloat(-length / 2).putFloat(1.0f).putFloat(0.0f)
                .putFloat(-width / 2).putFloat(0.0f).putFloat(length / 2).putFloat(0.0f).putFloat(1.0f)
                .putFloat(width / 2).putFloat( 0.0f).putFloat(length / 2).putFloat(1.0f).putFloat(1.0f)
                .flip();


        var indexBuffer = MemoryUtil.memAlloc(6 * 2)
                .putShort((short) 0).putShort((short) 1).putShort((short) 2)
                .putShort((short) 1).putShort((short) 3).putShort((short) 2)
                .flip();


        List<Runnable> glCalls = new ArrayList<>();
        var model = new GLModel(vertexBuffer, indexBuffer, glCalls, 6, GL11.GL_UNSIGNED_SHORT, attributes);

        return new Pair<>(model, glCalls);
    }

    public static Pair<List<Runnable>, MultiRenderObject<MeshObject>> generateCube(float width, float height, float length, String image) {

        var attributes = List.of(Attribute.POSITION, Attribute.TEXCOORD);

        var vertexLength = calculateVertexSize(attributes);
        var amount = 24; // 6 faces * 4 vertices per face

        var vertexBuffer = MemoryUtil.memAlloc(vertexLength * amount)
                .putFloat(-width / 2).putFloat(-height / 2).putFloat(-length / 2).putFloat(0.0f).putFloat(0.0f) // Bottom-left-back
                .putFloat(width / 2).putFloat(-height / 2).putFloat(-length / 2).putFloat(1.0f).putFloat(0.0f) // Bottom-right-back
                .putFloat(width / 2).putFloat(height / 2).putFloat(-length / 2).putFloat(1.0f).putFloat(1.0f) // Top-right-back
                .putFloat(-width / 2).putFloat(height / 2).putFloat(-length / 2).putFloat(0.0f).putFloat(1.0f) // Top-left-back

                .putFloat(-width / 2).putFloat(-height / 2).putFloat(length / 2).putFloat(0.0f).putFloat(0.0f) // Bottom-left-front
                .putFloat(width / 2).putFloat(-height / 2).putFloat(length / 2).putFloat(1.0f).putFloat(0.0f) // Bottom-right-front
                .putFloat(width / 2).putFloat(height / 2).putFloat(length / 2).putFloat(1.0f).putFloat(1.0f) // Top-right-front
                .putFloat(-width / 2).putFloat(height / 2).putFloat(length / 2).putFloat(0.0f).putFloat(1.0f) // Top-left-front

                .putFloat(-width / 2).putFloat(-height / 2).putFloat(-length / 2).putFloat(0.0f).putFloat(0.0f) // Reuse Bottom-left-back
                .putFloat(width / 2).putFloat(-height / 2).putFloat(-length / 2).putFloat(1.0f).putFloat(0.0f) // Reuse Bottom-right-back
                .putFloat(width / 2).putFloat(-height / 2).putFloat(length / 2).putFloat(1.0f).putFloat(1.0f) // Bottom-right-front
                .putFloat(-width / 2).putFloat(-height / 2).putFloat(length / 2).putFloat(0.0f).putFloat(1.0f) // Bottom-left-front

                .putFloat(-width / 2).putFloat(height / 2).putFloat(-length / 2).putFloat(0.0f).putFloat(0.0f) // Reuse Top-left-back
                .putFloat(width / 2).putFloat(height / 2).putFloat(-length / 2).putFloat(1.0f).putFloat(0.0f) // Reuse Top-right-back
                .putFloat(width / 2).putFloat(height / 2).putFloat(length / 2).putFloat(1.0f).putFloat(1.0f) // Reuse Top-right-front
                .putFloat(-width / 2).putFloat(height / 2).putFloat(length / 2).putFloat(0.0f).putFloat(1.0f) // Reuse Top-left-front

                .putFloat(-width / 2).putFloat(-height / 2).putFloat(-length / 2).putFloat(0.0f).putFloat(0.0f) // Reuse Bottom-left-back
                .putFloat(-width / 2).putFloat(height / 2).putFloat(-length / 2).putFloat(1.0f).putFloat(0.0f) // Reuse Top-left-back
                .putFloat(-width / 2).putFloat(height / 2).putFloat(length / 2).putFloat(1.0f).putFloat(1.0f) // Reuse Top-left-front
                .putFloat(-width / 2).putFloat(-height / 2).putFloat(length / 2).putFloat(0.0f).putFloat(1.0f) // Reuse Bottom-left-front

                .putFloat(width / 2).putFloat(-height / 2).putFloat(-length / 2).putFloat(0.0f).putFloat(0.0f) // Reuse Bottom-right-back
                .putFloat(width / 2).putFloat(height / 2).putFloat(-length / 2).putFloat(1.0f).putFloat(0.0f) // Reuse Top-right-back
                .putFloat(width / 2).putFloat(height / 2).putFloat(length / 2).putFloat(1.0f).putFloat(1.0f) // Reuse Top-right-front
                .putFloat(width / 2).putFloat(-height / 2).putFloat(length / 2).putFloat(0.0f).putFloat(1.0f) // Reuse Bottom-right-front
                .flip();

        var indexBuffer = MemoryUtil.memAlloc(36 * 2)
                .putShort((short) 0).putShort((short) 1).putShort((short) 2) // Back face
                .putShort((short) 2).putShort((short) 3).putShort((short) 0)

                .putShort((short) 4).putShort((short) 5).putShort((short) 6) // Front face
                .putShort((short) 6).putShort((short) 7).putShort((short) 4)

                .putShort((short) 8).putShort((short) 9).putShort((short) 10) // Bottom face
                .putShort((short) 10).putShort((short) 11).putShort((short) 8)

                .putShort((short) 12).putShort((short) 13).putShort((short) 14) // Top face
                .putShort((short) 14).putShort((short) 15).putShort((short) 12)

                .putShort((short) 16).putShort((short) 17).putShort((short) 18) // Left face
                .putShort((short) 18).putShort((short) 19).putShort((short) 16)

                .putShort((short) 20).putShort((short) 21).putShort((short) 22) // Right face
                .putShort((short) 22).putShort((short) 23).putShort((short) 20)
                .flip();

        List<Runnable> glCalls = new ArrayList<>();
        var model = new GLModel(vertexBuffer, indexBuffer, glCalls, 36, GL11.GL_UNSIGNED_SHORT, attributes);

        var obj = new MultiRenderObject<MeshObject>();

        var mesh = new MeshObject();
        mesh.setup(Map.of("cube", new Variant(new Material("cube", Map.of("diffuse", image), new HashMap<>(), CullType.None, BlendType.None, "solid"))), model, "cube");
        obj.add(mesh);

        return new Pair<>(glCalls, obj);
    }
}
