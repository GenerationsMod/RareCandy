package gg.generations.rarecandy.tools.gui;

import com.bedrockk.molang.MoLang;
import com.bedrockk.molang.runtime.value.DoubleValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.launch.OpenGL;
import gg.generations.rarecandy.tools.AppBase;
import gg.generations.rarecandy.tools.ImGuiImageViewer;
import gg.generations.rarecandy.tools.TextureLoader;
import gg.generations.rarecandy.tools.gui.imgui.ImBoolean;
import gg.generations.rarecandy.tools.gui.imgui.ImVector3f;
import gg.generations.rarecandy.tools.gui.imgui.ImVector4f;
import gg.generations.rarecandy.tools.gui.imgui.Serializers;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import org.joml.Vector4f;
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
    public ViewportGizmos gizmos;
    protected Settings settings;
    private final List<String> loadIssues = new ArrayList<>();
    private boolean closed;
    private static Path settingsPath = Paths.get("settings.json");

    public PokeUtilsGui(String title, int width, int height) throws IOException {
        super(title, width, height, new OpenGL());

        setupSettings();

        handler = new GuiHandler(this);

        ITextureLoader.setInstance(new TextureLoader());

        this.canvas = new RareCandyCanvas(this);
        this.fileViewer = new PixelAssetTree(this);
        this.gizmos = new ViewportGizmos(this);

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
        menu.render();
        fileViewer.render();
        settings.render();
        renderLoadIssues();
        gizmos.render(settings.features.gizmos.getValue());
    }

    public boolean gizmoWantsMouse() {
        return gizmos != null && gizmos.wantsMouseInput();
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

    @Override
    protected Vector4f clearColor() {
        return settings.fog.color.getValue();
    }

    public static class Settings {
        public Urls urls = new Urls();
        public Terastalization terastalization = new Terastalization();
        public Features features = new Features();
        public Fog fog = new Fog();
        public Light light = new Light();

        public void render() {
            if(features.terastalization.getValue()) terastalization.render();
            if(features.fog.getValue()) fog.render();
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

        public static class Light {
            public ImInt lightLevel = new ImInt(15);

            public void render() {
                ImGui.begin("Light");

                if(ImGui.sliderInt("Start", lightLevel.getData(), 0, 15)) {
                }

                ImGui.end();

            }
        }

        public static class Features {
            ImBoolean terastalization = new ImBoolean(true);
            ImBoolean fog = new ImBoolean(true);
            ImBoolean light = new ImBoolean(true);
            ImBoolean grid = new ImBoolean(true);
            ImBoolean gizmos = new ImBoolean(true);

            public void render() {
                ImGui.begin("Features");
                terastalization.render("Terastalization");
                fog.render("Fog");
                light.render("Light");
                grid.render("Grid");
                gizmos.render("Gizmos");
                ImGui.end();
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

                var dirty = false;

                if(color.render("Color")) {
                    dirty = true;
                }

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
