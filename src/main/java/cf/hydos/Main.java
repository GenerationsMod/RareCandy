package cf.hydos;

import cf.hydos.engine.AnimationUtil;
import cf.hydos.engine.components.StaticRenderObject;
import cf.hydos.engine.core.LoopManager;
import cf.hydos.engine.core.RenderObject;
import cf.hydos.engine.core.RenderingApplication;
import cf.hydos.engine.rendering.shader.ShaderProgram;
import cf.hydos.pixelmonassetutils.PixelAsset;
import cf.hydos.pixelmonassetutils.reader.GlbReader;
import cf.hydos.pixelmonassetutils.scene.Scene;
import cf.hydos.pixelmonassetutils.scene.material.Texture;
import cf.hydos.pixelmonassetutils.scene.objects.Mesh;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AITexture;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main extends RenderingApplication {

    private RenderObject pokemon;

    public static void main(String[] args) {
        System.loadLibrary("renderdoc");
        new LoopManager(960, 540, "Pixelmon: Generations .pk Renderer", new Main()).start();
    }

    @Override
    public void init() {
        addStaticObject("world", new Vector3f(-25, -14, 2), 1);
        pokemon = addPokemon("mudkip", new Vector3f(0f, -2f, 1f), 0.03f);
    }

    @Override
    public void update() {
        super.update();
        this.pokemon.getTransformation().rotate(0.0005f, 0, 1, 0);
    }

    private RenderObject addStaticObject(String name, Vector3f pos, float scale) {
        PixelAsset model = new PixelAsset(Objects.requireNonNull(Main.class.getResourceAsStream("/" + name + ".pk"), "Failed to read /" + name + ".pk"));
        RenderObject object = new RenderObject();
        loadStaticFile(model.scene, ((GlbReader) model.reader).rawScene, object);
        object.getTransformation().translate(pos).rotate((float) Math.toRadians(90), new Vector3f(1, 0, 0)).scale(new Vector3f(scale, scale, scale));

        add(object);
        return object;
    }

    private RenderObject addPokemon(String name, Vector3f pos, float scale) {
        PixelAsset model = new PixelAsset(Objects.requireNonNull(Main.class.getResourceAsStream("/" + name + ".pk"), "Failed to read /" + name + ".pk"));
        RenderObject pokemon = new RenderObject().addComponent(AnimationUtil.loadAnimatedFile(model.scene, ((GlbReader) model.reader).rawScene));
        pokemon.getTransformation().translate(pos).scale(new Vector3f(scale, scale, scale));

        add(pokemon);
        return pokemon;
    }

    public static void loadStaticFile(Scene scene, AIScene aiScene, RenderObject object) {
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

            component.addVertices(new ShaderProgram("static"), vertBuffer, indices, textures.get(1));
            object.addComponent(component);
        }
    }
}
