package gg.generations.rarecandy.renderer.loading;

import gg.generations.rarecandy.pokeutils.BlendType;
import gg.generations.rarecandy.pokeutils.CullType;
import gg.generations.rarecandy.pokeutils.Pair;
import gg.generations.rarecandy.renderer.animation.Skeleton;
import gg.generations.rarecandy.renderer.components.MeshObject;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.model.GLModel;
import gg.generations.rarecandy.renderer.model.MeshDrawCommand;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.rendering.RareCandy;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static gg.generations.rarecandy.renderer.loading.ModelLoader.calculateVertexSize;
import static gg.generations.rarecandy.renderer.loading.ModelLoader.generateVao;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_INT;

public class PlaneGenerator {
    public static Pair<List<Runnable>, MultiRenderObject<MeshObject>> generatePlane(float width, float length) {
        var model = new GLModel();

        var attributes = List.of(Attribute.POSITION, Attribute.TEXCOORD);

        var vertexlength = calculateVertexSize(attributes);
        var amount = 4;

        var vertexBuffer = MemoryUtil.memAlloc(vertexlength * amount)
                .putFloat(-width / 2).putFloat(0.0f).putFloat(-length / 2).putFloat(0.0f).putFloat(0.0f)
                .putFloat(width / 2).putFloat(0.0f).putFloat(-length / 2).putFloat(1.0f).putFloat(0.0f)
                .putFloat(-width / 2).putFloat(0.0f).putFloat(length / 2).putFloat(0.0f).putFloat(1.0f)
                .putFloat(width / 2).putFloat( 0.0f).putFloat(length / 2).putFloat(1.0f).putFloat(1.0f)
                .flip();


        var indexBuffer = MemoryUtil.memAlloc(6 * 2).asShortBuffer()
                .put((short) 0).put((short) 1).put((short) 2)
                .put((short) 1).put((short) 3).put((short) 2)
                .flip();



        List<Runnable> glCalls = List.of(() -> {
            model.vao = generateVao(vertexBuffer, attributes);
            GL30.glBindVertexArray(model.vao);

            model.ebo = GL15.glGenBuffers();
            GL15.glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, model.ebo);
            GL15.glBufferData(GL15C.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);

            model.meshDrawCommands.add(new MeshDrawCommand(model.vao, GL11.GL_TRIANGLES, GL11.GL_UNSIGNED_SHORT, model.ebo, 6));
        });

        var obj = new MultiRenderObject<MeshObject>();

        var mesh = new MeshObject();
        mesh.setup(Map.of("plane", new Material("plane", new HashMap<>(), new HashMap<>(), CullType.None, BlendType.None, "plane")), new ArrayList<>(), new HashMap<>(), model, "plane");
        obj.add(mesh);

        return new Pair<>(glCalls, obj);
    }

    public static Mesh mesh(String name, int material, float width, float length) {
        System.out.println(name + " " + material);

        return new Mesh(name, material, List.of(0,1,2,1,3,2),
                List.of(
                        new Vector3f(-width / 2, 0.0f, -length / 2).add(0, material, 0),
                        new Vector3f(width / 2, 0.0f, -length / 2).add(0, material, 0),
                        new Vector3f(-width / 2, 0.0f, length / 2).add(0, material, 0),
                        new Vector3f(width / 2, 0.0f, length / 2).add(0, material, 0)
                ),
                List.of(
                        new Vector2f(0.0f, 0.0f),
                        new Vector2f(1.0f, 0.0f),
                        new Vector2f(0.0f, 1.0f),
                        new Vector2f(1.0f, 1.0f)
                ),
                List.of(
                        new Vector3f(0, 1, 0),
                        new Vector3f(0, 1, 0),
                        new Vector3f(0, 1, 0),
                        new Vector3f(0, 1, 0)
                ),
                List.of()
        );
    }
}
