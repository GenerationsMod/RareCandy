package com.pixelmongenerations.test;

import com.pixelmongenerations.rarecandy.AnimationUtil;
import com.pixelmongenerations.rarecandy.components.StaticRenderObject;
import com.pixelmongenerations.rarecandy.core.RenderObject;
import com.pixelmongenerations.rarecandy.rendering.GameProvider;
import com.pixelmongenerations.rarecandy.rendering.RenderingEngine;
import com.pixelmongenerations.rarecandy.rendering.shader.ShaderProgram;
import com.pixelmongenerations.pixelmonassetutils.PixelAsset;
import com.pixelmongenerations.pixelmonassetutils.reader.GlbReader;
import com.pixelmongenerations.pixelmonassetutils.scene.Scene;
import com.pixelmongenerations.pixelmonassetutils.scene.material.Texture;
import com.pixelmongenerations.pixelmonassetutils.scene.objects.Mesh;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AITexture;
import org.lwjgl.opengl.GL11C;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main implements GameProvider {

    public final Window window;
    public final Matrix4f projectionMatrix;
    public final Matrix4f viewMatrix;
    private final RenderingEngine renderer;
    public final RenderObject root;
    private final RenderObject displayPokemon;

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        //System.loadLibrary("renderdoc");

        this.window = new Window("Pixelmon Renderer Test", 960, 540);
        this.projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(90), (float) window.width / window.height, 0.1f, 1000.0f);
        this.viewMatrix = new Matrix4f().lookAt(0.1f, 0.01f, -2, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        this.renderer = new RenderingEngine(this);
        this.root = new RenderObject();
        setupGl();

        //addStaticObject("world", new Vector3f(-25, -14, 2), 1, 1); my computer cant handle this :(
        this.displayPokemon = addPokemon("mudkip", new Vector3f(0f, -2f, 1f), 0.03f);

        while (!this.window.shouldClose()) {
            this.window.pollEvents();
            update();
            render();
            this.window.swapBuffers();
        }

        this.window.destroy();
    }

    private void update() {
        this.root.update();
        this.displayPokemon.getTransformation().rotate(0.0005f, 0, 1, 0);
    }

    private void render() {
        GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
        this.renderer.render(this.root);
    }

    private void setupGl() {
        GL11C.glClearColor(63 / 255f, 191 / 255f, 217 / 255f, 1.0f);
        GL11C.glFrontFace(GL11C.GL_CW);
        GL11C.glCullFace(GL11C.GL_FRONT);
        GL11C.glEnable(GL11C.GL_CULL_FACE);
        GL11C.glEnable(GL11C.GL_DEPTH_TEST);
    }

    private RenderObject addStaticObject(String name, Vector3f pos, float scale, int textureIndex) {
        PixelAsset model = new PixelAsset(Objects.requireNonNull(Main.class.getResourceAsStream("/" + name + ".pk"), "Failed to read /" + name + ".pk"));
        RenderObject object = new RenderObject();
        loadStaticFile(model.scene, ((GlbReader) model.reader).rawScene, object, textureIndex);
        object.getTransformation().translate(pos).rotate((float) Math.toRadians(90), new Vector3f(1, 0, 0)).scale(new Vector3f(scale, scale, scale));

        root.addChild(object);
        return object;
    }

    private RenderObject addPokemon(String name, Vector3f pos, float scale) {
        PixelAsset model = new PixelAsset(Objects.requireNonNull(Main.class.getResourceAsStream("/" + name + ".pk"), "Failed to read /" + name + ".pk"));
        RenderObject pokemon = new RenderObject().addComponent(AnimationUtil.loadAnimatedFile(model.scene, ((GlbReader) model.reader).rawScene));
        pokemon.getTransformation().translate(pos).scale(new Vector3f(scale, scale, scale));

        root.addChild(pokemon);
        return pokemon;
    }

    public static void loadStaticFile(Scene scene, AIScene aiScene, RenderObject object, int textureIndex) {
        int sizeOfVertex = Float.BYTES * 3 + Float.BYTES * 2 + Float.BYTES * 3;

        for (Mesh mesh : scene.meshes) {
            float[] rawMeshData = new float[mesh.getVertices().length * sizeOfVertex];
            int index = 0;

            for (int v = 0; v < mesh.getVertices().length; v++) {
                Vector3f position = mesh.getVertices()[v];
                Vector3f normal = mesh.getNormals()[v];
                Vector2f texCoord = mesh.getTexCoords()[v];

                rawMeshData[index++] = position.x();
                rawMeshData[index++] = position.y();
                rawMeshData[index++] = position.z();

                rawMeshData[index++] = texCoord.x();
                rawMeshData[index++] = texCoord.y();

                rawMeshData[index++] = normal.x();
                rawMeshData[index++] = normal.y();
                rawMeshData[index++] = normal.z();
            }

            StaticRenderObject component = new StaticRenderObject();
            FloatBuffer vertBuffer = BufferUtils.createFloatBuffer(rawMeshData.length);

            IntBuffer indices = BufferUtils.createIntBuffer(mesh.getIndices().length);
            for (int i : mesh.getIndices()) {
                indices.put(i);
            }
            indices.flip();

            for (float v : rawMeshData) vertBuffer.put(v);
            vertBuffer.flip();

            List<AITexture> rawTextures = new ArrayList<>();

            // Retrieve Textures
            PointerBuffer pTextures = aiScene.mTextures();
            if (pTextures != null) {
                for (int i = 0; i < aiScene.mNumTextures(); i++) {
                    rawTextures.add(AITexture.create(pTextures.get(i)));
                }
            } else {
                throw new RuntimeException("How do you expect us to render without textures? Use colours? we don't support that yet!");
            }

            // Try to load the textures into rosella
            List<Texture> textures = new ArrayList<>();
            for (AITexture rawTexture : rawTextures) {
                if (rawTexture.mHeight() > 0) {
                    throw new RuntimeException(".glb file had texture with height of 0");
                } else {
                    textures.add(new Texture(rawTexture.pcDataCompressed(), rawTexture.mFilename().dataString()));
                }
            }

            component.addVertices(ShaderProgram.STATIC_SHADER, vertBuffer, indices, textures.get(textureIndex));
            object.addComponent(component);
        }
    }

    @Override
    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }

    @Override
    public Matrix4f getViewMatrix() {
        return this.viewMatrix;
    }
}
