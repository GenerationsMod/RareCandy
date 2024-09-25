package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.MaterialReference;
import gg.generations.rarecandy.pokeutils.PixelAsset;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.components.MeshObject;
import gg.generations.rarecandy.renderer.loading.ModelLoader;
import gg.generations.rarecandy.renderer.loading.PlaneGenerator;
import gg.generations.rarecandy.renderer.model.GLModel;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.rendering.FrameBuffer;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import gg.generations.rarecandy.tools.TextureLoader;
import gg.generations.rarecandy.tools.gui.imgui.GuiPipelines;
import gg.generations.rarecandy.tools.pkcreator.PixelmonArchiveBuilder;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

import static gg.generations.rarecandy.tools.gui.GuiHandler.TEMP;
import static gg.generations.rarecandy.tools.gui.GuiHandler.move;
import static java.sql.Types.NULL;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_TRUE;

public class MinimalQuad {

    private long window;
    private Map<String, Material> materials = new HashMap<>();

    private List<String> materialNames = new ArrayList<>();

    public static Map<Integer, FrameBuffer> framebuffer;

    private int index = 0;

    private ObjectInstance instance = new ObjectInstance(new Matrix4f(), new Matrix4f(), "");

    public MeshObject object = new MeshObject() {
        @Override
        public Material getMaterial(@Nullable String materialId) {
            if (!materials.isEmpty() && !materialNames.isEmpty()) {
                return materials.get(materialNames.get(index));
            }

            return null;
        }
    };
    private GLModel model;
    private int timer = 0;
    private Path file;
    private HashMap<String, MaterialReference> newMaterial;

    public void run() throws IOException {
        NativeFileDialog.NFD_Init();

        System.out.println("Starting LWJGL...");

        init();

        file = DialogueUtils.chooseFile("PK;pk");

        var ps = new PixelAsset(move(file), file.getFileName().toString());

        var imagesToDelete = new ArrayList<String>();

//        ps.getConfig().materials.forEach(new BiConsumer<String, MaterialReference>() {
//            @Override
//            public void accept(String name, MaterialReference materialReference) {
//                materialReference.shader = "solid";
//                var map = materialReference.images;
//
////                var diffuse = map.get();
////                imagesToDelete.add();
//            }
//        });

        load(() -> ps);

        loop();

//        save(file);

        // Free resources
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
//        glfwSetErrorCallback(null).free();
    }

    private void init() {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE); // Mac compatibility

        // Create the window
        window = glfwCreateWindow(1024, 1024, "Minimal Quad", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);

        GL.createCapabilities();

        GuiPipelines.init();
        ITextureLoader.setInstance(new TextureLoader());

        var pair = PlaneGenerator.generatePlaneRunnable(2, 2);

        pair.b().forEach(Runnable::run);
        model = pair.a();

        framebuffer = new HashMap<>();
        framebuffer.computeIfAbsent(32, res -> new FrameBuffer(res, res));
        framebuffer.computeIfAbsent(128, res -> new FrameBuffer(res, res));
        framebuffer.computeIfAbsent(256, res -> new FrameBuffer(res, res));
        framebuffer.computeIfAbsent(512, res -> new FrameBuffer(res, res));
        framebuffer.computeIfAbsent(1024, res -> new FrameBuffer(res, res));
    }

    public void render() {
        var material = object.getMaterial(null);

        if (material == null) return;

        var pipeline = material.getPipeline();

        pipeline.bind(material);
        pipeline.updateOtherUniforms(instance, object);
        pipeline.updateTexUniforms(instance, object);
        model.runDrawCalls();
        pipeline.unbind(material);
    }

    protected void load(Supplier<PixelAsset> asset) {
        var as = asset.get();

        var config = as.getConfig();

        var images = ModelLoader.readImages(as);

        var replacments = mapDuplicatesWithStreams(config.materials);
        materialNames.clear();
        materialNames.addAll(replacments.values().stream().distinct().toList());

        for(var name : materialNames) {
            var material = MaterialReference.process(name, config.materials, images);
            materials.put(name, material);
        }

        newMaterial = new HashMap<String, MaterialReference>();

        for(var name : materialNames) {
            var reference = config.materials.get(name);

            var images1 = new HashMap<String, String>();

            images1.put("diffuse", name + ".png");
            if(reference.images.containsKey("emission")) images1.put("emission", reference.images.get("emission"));

            var values = new HashMap<String, Object>();

            if(reference.values.containsKey("useLight")) values.put("useLight", reference.values.get("useLight"));

            newMaterial.put(name, new MaterialReference(null, "solid", reference.cull, reference.blend, images1, values));
        }

        System.out.println();
    }

    public void next() {
        index++;
        index %= materialNames.size();
    }

    private void loop() throws IOException {
        for (int i = 0; i < materialNames.size(); i++) {
            index = i;

            var string = materialNames.get(i);

            var texture = materials.get(materialNames.get(i)).maxTextureSize();

            System.out.println(i + " " + texture);

            var buffer = framebuffer.get(1024);

            buffer.bindFramebuffer();

            render();

            buffer.unbindFramebuffer();

            buffer.regularScreenshot(Path.of("images_output", materialNames.get(i) + ".png"), texture);
        }
    }

    public static void main(String[] args) throws IOException {
        System.loadLibrary("renderdoc");


        new MinimalQuad().run();
    }

    public static void save(Path savePath) {
        try {
            PixelmonArchiveBuilder.convertToPk(TEMP, Files.walk(TEMP).toList(), savePath, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public record Key(String key) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key key1)) return false;
            return Arrays.equals(key.getBytes(), key1.key.getBytes());
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(key.getBytes());
        }
    }

    public static Map<String, String> mapDuplicatesWithStreams(Map<String, MaterialReference> inputMap) {
        var alreadyMapped = new HashMap<String, MaterialReference>();

        var mapList = new HashMap<String, String>();

        for(var pair : inputMap.entrySet()) {
            var existing = alreadyMapped.entrySet().stream().filter(a -> a.getValue().equals(pair.getValue())).findFirst().map(a -> a.getKey());

            String entry;

            if(existing.isPresent()) {
                entry = existing.get();
            } else {
                alreadyMapped.put(pair.getKey(), pair.getValue());
                entry = pair.getKey();
            }

            mapList.put(pair.getKey(), entry);
        }

        return mapList;
    }
}
