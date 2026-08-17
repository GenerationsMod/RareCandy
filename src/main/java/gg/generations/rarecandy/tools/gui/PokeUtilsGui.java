package gg.generations.rarecandy.tools.gui;

import com.bedrockk.molang.MoLang;
import com.bedrockk.molang.runtime.value.DoubleValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gg.generations.rarecandy.pokeutils.IMaterialReference;
import gg.generations.rarecandy.pokeutils.IModelConfig;
import gg.generations.rarecandy.pokeutils.ImModelConfig;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.launch.OpenGL;
import gg.generations.rarecandy.renderer.loading.ModelObjectCompiler;
import gg.generations.rarecandy.tools.AppBase;
import gg.generations.rarecandy.tools.TextureLoader;
import gg.generations.rarecandy.tools.gui.imgui.*;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImFloat;
import imgui.type.ImString;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public class PokeUtilsGui extends AppBase {
    public static Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ImBoolean.class, new ImBoolean.Serializer())
            .registerTypeAdapter(ImVector3f.class, new ImVector3f.Serializer())
            .registerTypeAdapter(ImVector4f.class, new ImVector4f.Serializer())
            .registerTypeAdapter(ImFloat.class, new Serializers.ImFloatSerializer())
            .setPrettyPrinting()
            .create();


    private final AdvancedMenuBar menu;
    public GuiHandler handler;
    public PixelAssetTree fileViewer;
    public RareCandyCanvas canvas;
    private final AnimationPlaybackPanel animationPlayback;
    protected Settings settings;
    private final List<String> loadIssues = new ArrayList<>();
    private static Path settingsPath = Paths.get("settings.json");
    private VariantView variantMenu;

    public PokeUtilsGui(String title, int width, int height) throws IOException {
        super(title, width, height, new OpenGL());
        IModelConfig.Factory.ACTIVE_FACTORY = ImModelConfig.FACTORY;
        setupSettings();

        handler = new GuiHandler(this);

        ITextureLoader.setInstance(new TextureLoader());

        this.canvas = new RareCandyCanvas(this);
        this.animationPlayback = new AnimationPlaybackPanel(canvas);
        this.fileViewer = new PixelAssetTree(this);
        this.variantMenu = new VariantView(this);

        menu = configureMenu();
    }

    @Override
    protected void initWindow() {
        super.initWindow();

        GLFW.glfwSetWindowCloseCallback(window, window -> close());
    }

    private void setupSettings() throws IOException {
        if(Files.exists(settingsPath)) {
            settings = GSON.fromJson(Files.readString(settingsPath), Settings.class);
        } else {
            settings = new Settings();
            Files.createFile(settingsPath);

            Files.writeString(settingsPath, GSON.toJson(settings));
        }
    }

    @Override
    protected void onResize(int width, int height) {
        canvas.resize(width, height);
    }

    public static void main(String[] args) throws IOException {
        try {
            System.loadLibrary("renderdoc");
        } catch (Exception e) {
            System.out.println("Renderdoc not loaded. Continuing without.");
        }

        DialogueUtils.init();

        new PokeUtilsGui(GuiHandler.BASE_TITLE, 250+512 + (512 - 482), 512).run();
    }

    @Override
    protected void initGL() {
        handler.attach(window);
        canvas.initGL();
    }



    @Override
    protected void renderGui() {
        setUiScale(settings.features.largeUi.getValue() ? 1.75f : 1.0f);
        menu.render();
        fileViewer.render();
        animationPlayback.render();
        settings.render();
        renderLoadIssues();

        variantMenu.render();

        var config = canvas.config;

        if(config instanceof ImModelConfig imConfig) {
            var flags = imConfig.render();
            if (flags != 0) {
                if ((flags & 1) != 0) {
                    canvas.setScaleModifier(imConfig.scale());
                }

                if((flags & 2) != 0) {
                    canvas.loadedModel.onUpdate(model -> {
                        ModelObjectCompiler.rebuildMaterials(model, canvas.config, IMaterialReference::process);
                    });
                }

                if((flags & 4) != 0) {
                    canvas.loadedModel.onUpdate(model -> {
                        ModelObjectCompiler.rebuildVariants(model, canvas.config);
                    });
                }

                if((flags & 8) != 0) {
                    canvas.loadedModel.onUpdate(model -> {
                        ModelObjectCompiler.rebuildAnimationVisibility(model, canvas.config);
                    });
                }

                handler.markDirty();
            }
        }
    }

    @Override
    protected void render() {
        try {
            canvas.render();
        } catch (RuntimeException e) {
            reportLoadIssue("Render failed for " + handler.getCurrentAssetName() + ": " + e.getMessage());
            canvas.stopRenderingAfterFailure();
            e.printStackTrace();
        }
    }

    public void clearLoadIssues() {
        loadIssues.clear();
    }

    public void reportLoadIssue(String issue) {
        if (issue != null && !issue.isBlank() && !loadIssues.contains(issue)) {
            loadIssues.add(issue);
        }
    }

    private void renderLoadIssues() {
        if (loadIssues.isEmpty()) return;

        ImGui.begin("Load Issues");
        for (String issue : loadIssues) {
            ImGui.textWrapped(issue);
        }
        if (ImGui.button("Clear")) {
            loadIssues.clear();
        }
        ImGui.end();
    }

    private void open(Path path) {
        if(path == null) return;
        addRunnable(() -> handler.openAsset(path));
        settings.urls.openArchiveUrl = path.toString();
    }

    private void save(Path path) {
        if (path == null) return;
        if (handler.save(path)) settings.urls.saveAsUrl = handler.assetPath.toString();
    }

    private AdvancedMenuBar configureMenu() {
        var toolbar = new AdvancedMenuBar();
        var file = toolbar.addMenu("File");

        var open = file.addMenu("Open");

        open.addItem("PK (*.pk)", () -> DialogueUtils.chooseFile(settings.urls.openArchiveUrl, "PK;pk", this::open));
        open.addItem("Folder", () -> DialogueUtils.chooseFolder(settings.urls.openArchiveUrl, this::open));

        file.addItem("Open Multiple Archives in sequence (.pk)", () -> DialogueUtils.chooseMultipleFiles( "Open Multiple Archives", settings.urls.sequenceUrl, "PK;pk", files -> {
            addRunnable(() -> handler.openAsset(files));
            settings.urls.sequenceUrl = files.get(0).toString();
        }));

        file.addItem("Open Multiple Folders in sequence", () -> DialogueUtils.chooseFolders(settings.urls.sequenceUrl, files -> {
            addRunnable(() -> handler.openAsset(files));
            settings.urls.sequenceUrl = files.get(0).toString();
        }));

        var saveAs = file.addMenu("Save As");

        saveAs.addItem("PK (*.pk)", () -> DialogueUtils.saveFile(settings.urls.saveAsUrl, "PK;pk", this::save));
        saveAs.addItem("Folder", () -> DialogueUtils.chooseFolder(settings.urls.saveAsUrl, this::save));

        file.addItem("Save", () -> handler.save());


        return toolbar;
    }

    private void close() {
        try {
            Files.writeString(settingsPath, GSON.toJson(settings));
        } catch (IOException e) {

        }

        DialogueUtils.quit();

        if (window != 0) {
            GLFW.glfwSetWindowShouldClose(window, true);
        }
    }

    @Override
    protected void cleanupGL() {
        close();
        canvas.close();
    }

    public void setTitle(String title) {
        GLFW.glfwSetWindowTitle(window, title);
    }

    public static class FloatInputComponent {

        private final String title;
        private final DoubleSupplier originalValue;
        private final DoubleConsumer consumer;
        private final DecimalFormat decimalFormat = new DecimalFormat("#.####");

        // State
        private float[] value;
        private ImString textBuffer = new ImString("base"); // user input string
        private boolean inputError = false;

        public FloatInputComponent(String title, DoubleSupplier originalValue, DoubleConsumer consumer) {
            this.title = title;
            this.originalValue = originalValue;
            this.consumer = consumer;
            this.value = new float[]{(float) originalValue.getAsDouble()};
        }

        public void render() {
            ImGui.text(title + ": " + formatScaleValue(value[0]));

            // Input field as text, not just float, so MoLang expressions are possible
            ImGui.inputText("##scaleExpr", textBuffer, ImGuiInputTextFlags.EnterReturnsTrue);

            if (ImGui.button("Enter")) {
                apply();
            }
            ImGui.sameLine();
            if (ImGui.button("Reset")) {
                reset();
            }

            // Visual error indicator
            if (inputError) {
                ImGui.textColored(1f, 0f, 0f, 1f, "Invalid expression!");
            }
        }

        private void apply() {
            try {
                String inputText = textBuffer.get();

                var runtime = MoLang.createRuntime();
                double result = runtime.execute(
                        MoLang.parse(inputText.replace("base", "context.base")),
                        Map.of("base", new DoubleValue(originalValue.getAsDouble()))
                ).asDouble();

                if (result > 0) {
                    value[0] = (float) result;
                    consumer.accept(value[0]);
                    inputError = false;
                } else {
                    inputError = true;
                }
            } catch (Exception ex) {
                inputError = true;
            }
        }

        protected void reset() {
            value[0] = (float) originalValue.getAsDouble();
            consumer.accept(value[0]);
            textBuffer.set("base");
            inputError = false;
        }

        private String formatScaleValue(float value) {
            return decimalFormat.format(value);
        }
    }

//    @Override
//    protected Vector4f clearColor() {
//        return settings.fog.color.getValue();
//    }

    public static class Settings {
        public Urls urls = new Urls();
        public Terastalization terastalization = new Terastalization();
        public Features features = new Features();
        public Fog fog = new Fog();
        public Values values = new Values();
        public Light light = new Light();

        public void render() {
            if(features.terastalization.getValue()) terastalization.render();
            if(features.fog.getValue()) fog.render();
            if(features.values.getValue()) values.render();
            if(features.light.getValue()) light.render();
            features.render();
        }

        public static class Urls {
            public String openArchiveUrl = "";
            public String saveAsUrl = "";
            public String createArchiveUrl = "";
            public String sequenceUrl = "";
        }

        public static class Terastalization {
            public ImBoolean enabled = new ImBoolean(false);
            public ImVector3f tint = new ImVector3f(1, 1, 1);

            public void render() {
                ImGui.begin("Terastalization");
                enabled.render("Enabled");
                tint.render("Tint");
                ImGui.end();
            }
        }

        public static class Values {
            public ImInt gBufferDebug = new ImInt("G-Buffer Debug", 0, 0, 3);

            public void render() {

                ImGui.begin("Values");

                gBufferDebug.render();

                ImGui.end();

            }
        }

        public static class Features {
            ImBoolean terastalization = new ImBoolean(true);
            ImBoolean fog = new ImBoolean(true);
            ImBoolean values = new ImBoolean(true);
            ImBoolean light = new ImBoolean(true);
            ImBoolean grid = new ImBoolean(true);
            ImBoolean gizmos = new ImBoolean(true);
            ImBoolean largeUi = new ImBoolean(false);

            public void render() {
                ImGui.begin("Features");
                terastalization.render("Terastalization");
                fog.render("Fog");
                values.render("Values");
                grid.render("Grid");
                gizmos.render("Gizmos");
                largeUi.render("Large UI");
                light.render("Light");
                ImGui.end();
            }
        }

        public static class Light {
            public imgui.type.ImInt selected = new imgui.type.ImInt(0);

            public Standard standard = new Standard();
            public Minecraft minecraft = new Minecraft();

            public static class Standard {
                public ImVector3f lightColor = new ImVector3f(1,1,1);
                public ImFloat lightRange = new ImFloat(20);
                public ImVector3f ambientColor = new ImVector3f(0.15f, 0.15f, 0.15f);
                public ImFloat shininess = new ImFloat(32);

                public void render() {
                    lightColor.render("Light Color");
                    ambientColor.render("Ambient Color");
                    ImGui.sliderFloat("Light Range", lightRange.getData(), 1, 100);
                    ImGui.sliderFloat("Shininess", shininess.getData(), 1, 128);
                }
            }

            public static class Minecraft {
                public ImInt sky = new ImInt("Sky", 15, 0, 15);
                public ImInt block = new ImInt("block", 15, 0, 15);

                public void render() {
                    sky.render();
                    block.render();

                }
            }

            private static String[] modes = new String[] { "None", "Ambient", "Minecraft", "Diffuse"};

            public void render() {
                ImGui.begin("Light");

                ImGui.combo("Mode", selected, modes);

                switch (selected.get()) {
                    case 1,3 -> standard.render();
                    case 2 -> minecraft.render();
                    default -> {
                    }
                }

                ImGui.end();;

            }
        }

        public static class Fog {
            public ImVector4f color = new ImVector4f(1, 1, 1, 1);
            public ImFloat start = new ImFloat(0f);
            public ImFloat end = new ImFloat(5f);
            transient private Consumer<Fog> consumer;

            public void setListener(Consumer<Fog> consumer) {
                this.consumer = consumer;
            }

            public void render() {
                ImGui.begin("Fog");

                var dirty = color.render("Color");

                if(ImGui.sliderFloat("Start", start.getData(), 0, end.floatValue())) {
                    dirty = true;
                }

                if(ImGui.sliderFloat("End", end.getData(), start.floatValue(), 10f)) {
                    dirty = true;
                }

                if(dirty && consumer != null) consumer.accept(this);

                ImGui.end();
            }
        }
    }
}
