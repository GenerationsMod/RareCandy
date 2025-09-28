package gg.generations.rarecandy.tools.gui;

import com.bedrockk.molang.MoLang;
import com.bedrockk.molang.runtime.value.DoubleValue;
import com.github.weisj.darklaf.LafManager;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.launch.OpenGL;
import gg.generations.rarecandy.tools.AppBase;
import gg.generations.rarecandy.tools.TextureLoader;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWWindowCloseCallback;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public class PokeUtilsGui extends AppBase {


    private final AdvancedMenuBar menu;
    public GuiHandler handler;
    public PixelAssetTree fileViewer;
    public RareCandyCanvas canvas;

    public PokeUtilsGui(String title, int width, int height) {
        super(title, width, height, new OpenGL());
        handler = new GuiHandler(this);

        ITextureLoader.setInstance(new TextureLoader());

        this.canvas = new RareCandyCanvas(this);
        this.fileViewer = new PixelAssetTree(this);


        menu = configureMenu();
    }

    public static void main(String[] args) {
        try {
            System.loadLibrary("renderdoc");
        } catch (Exception e) {
            System.out.println("Renderdoc not loaded. Continuing without.");
        }

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
        TerasalizationEffect.render();
        canvas.renderGui();
    }

    @Override
    protected void render() {
        canvas.render();
    }

    private AdvancedMenuBar configureMenu() {
        var toolbar = new AdvancedMenuBar();
        var file = toolbar.addMenu("File");
        file.addItem("Open Archive (.pk)", () -> {
            var chosenFile = DialogueUtils.chooseFile("PK;pk");
            if (chosenFile != null) handler.openAsset(chosenFile);
        });
        file.addItem("Create Archive (.glb)", () -> {
            var chosenFile = DialogueUtils.chooseFile("GLB;glb");
            if (chosenFile != null) handler.convertGlb(chosenFile);
        });
        file.addItem("Open Multiple Archives in sequence (.pk)", () -> {
            var chosenFiles = DialogueUtils.chooseMultipleFiles("PK;pk");
            if (chosenFiles != null) handler.openAsset(chosenFiles);
        });
        file.addItem("Save", () -> handler.save());
        file.addItem("Save As", () -> {
            var chosenFile = DialogueUtils.saveFile("PK;pk");
            if (chosenFile != null) {
                handler.markDirty();
                handler.save(chosenFile);
            }
        });

        return toolbar;
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
}