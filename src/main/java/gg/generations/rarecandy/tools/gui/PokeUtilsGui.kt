package gg.generations.rarecandy.tools.gui

import com.bedrockk.molang.MoLang
import com.bedrockk.molang.runtime.value.DoubleValue
import com.bedrockk.molang.runtime.value.MoValue
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import gg.generations.rarecandy.codec.codec
import gg.generations.rarecandy.codec.fromJson
import gg.generations.rarecandy.codec.obj
import gg.generations.rarecandy.pokeutils.IMaterialReference
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader
import gg.generations.rarecandy.renderer.launch.OpenGL
import gg.generations.rarecandy.renderer.loading.ModelObjectCompiler
import gg.generations.rarecandy.tools.AppBase
import gg.generations.rarecandy.tools.TextureLoader
import gg.generations.rarecandy.tools.gui.imgui.ImInt
import imgui.ImGui
import imgui.flag.ImGuiInputTextFlags
import imgui.type.ImBoolean
import imgui.type.ImFloat
import imgui.type.ImString
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.DecimalFormat
import java.util.function.Consumer

class PokeUtilsGui(title: String, width: Int, height: Int) : AppBase(title, width, height, OpenGL()) {
    private val menu: AdvancedMenuBar
    var handler: GuiHandler
//    @JvmField
//    var fileViewer: PixelAssetTree
    @JvmField
    var canvas: RareCandyCanvas
    @JvmField
    var settings: Settings? = null
    private val loadIssues: MutableList<String> = mutableListOf()
//    private val variantMenu: VariantView

    init {
        setupSettings()

        handler = GuiHandler(this)

        ITextureLoader.setInstance(TextureLoader())

        this.canvas = RareCandyCanvas(this)
//        this.fileViewer = PixelAssetTree(this)
//        this.variantMenu = VariantView(this)

        menu = configureMenu()
    }

    override fun initWindow() {
        super.initWindow()

        GLFW.glfwSetWindowCloseCallback(window) { _: Long -> close() }
    }

    @Throws(IOException::class)
    private fun setupSettings() {
        if (Files.exists(settingsPath)) {
            settings = Files.readString(settingsPath).fromJson(Settings.CODEC)
        } else {
            settings = Settings()
            Files.createFile(settingsPath)

            Files.writeString(settingsPath, GSON.toJson(settings))
        }
    }

    override fun onResize(width: Int, height: Int) {
        canvas.resize(width, height)
    }

    override fun initGL() {
        handler.attach(window)
        canvas.initGL()
    }


    override fun renderGui() {
        setUiScale(if (settings!!.features.largeUi) 1.75f else 1.0f)
        menu.render()
//        fileViewer.render()
        AnimationState.render()
        settings!!.render()
        renderLoadIssues()

//        variantMenu.render()

        val flags = ModelTracker.render()

        if(flags.isNotEmpty()) {
            if (flags.contains(ModelTracker.UpdateType.Scale)) {
                canvas.scaleModifier = ModelTracker.config?.scale ?: 1f
            }

            if (flags.contains(ModelTracker.UpdateType.Material)) {
//                canvas.loadedModel?.onUpdate { model ->
//                        ModelObjectCompiler.rebuildMaterials(
//                            model,
//                            canvas.config!!
//                        ) { base, references, images ->
//                            IMaterialReference.process(
//                                base,
//                                references, images
//                            )
//                        }
//                    }
            }

//                if ((flags and 2) != 0) {
//                    canvas.loadedModel?.onUpdate { model ->
//                        ModelObjectCompiler.rebuildMaterials(
//                            model,
//                            canvas.config
//                        ) { reference: IMaterialReference?, imageNames: MutableList<String?>? ->
//                            IMaterialReference.process(
//                                reference,
//                                imageNames
//                            )
//                        }
//                    }
//                }
//
//                if ((flags and 4) != 0) {
//                    canvas.loadedModel?.onUpdate { model: MultiRenderObject? ->
//                        ModelObjectCompiler.rebuildVariants(model, canvas.config)
//                    }
//                }
//
//                if ((flags and 8) != 0) {
//                    canvas.loadedModel?.onUpdate({ model -> ModelObjectCompiler.rebuildAnimationVisibility(model, canvas.config)
//                    })
//                }

                handler.markDirty()
//            }
        }
    }

    override fun render() {
        try {
            canvas.render()
        } catch (e: RuntimeException) {
            reportLoadIssue("Render failed for ${handler.currentAssetName}: ${e.message}")
            canvas.stopRenderingAfterFailure()
            e.printStackTrace()
        }
    }

    fun clearLoadIssues() {
        loadIssues.clear()
    }

    fun reportLoadIssue(issue: String?) {
        if (!issue.isNullOrBlank() && !loadIssues.contains(issue)) loadIssues.add(issue)
    }

    private fun renderLoadIssues() {
        if (loadIssues.isEmpty()) return

        ImGui.begin("Load Issues")
        for (issue in loadIssues) {
            ImGui.textWrapped(issue)
        }
        if (ImGui.button("Clear")) {
            loadIssues.clear()
        }
        ImGui.end()
    }

    private fun open(path: Path?) {
        if (path == null) return
        addRunnable { handler.openAsset(path) }
        settings!!.urls.openArchiveUrl = path.toString()
    }

    private fun save(path: Path?) {
        if (path == null) return
        if (handler.save(path)) settings!!.urls.saveAsUrl = handler.assetPath.toString()
    }

    private fun configureMenu(): AdvancedMenuBar {
        val toolbar = AdvancedMenuBar()
        val file = toolbar.addMenu("File")

        val open = file.addMenu("Open")

        open.addItem(
            "PK (*.pk)",
            Runnable {
                DialogueUtils.chooseFile(
                    settings!!.urls.openArchiveUrl,
                    "PK;pk",
                    Consumer { path: Path? -> this.open(path) })
            })
        open.addItem(
            "Folder",
             {
                DialogueUtils.chooseFolder(settings!!.urls.openArchiveUrl, this::open)
            })

        file.addItem("Open Multiple Archives in sequence (.pk)", Runnable {
            DialogueUtils.chooseMultipleFiles(
                "Open Multiple Archives",
                settings!!.urls.sequenceUrl,
                "PK;pk",
                Consumer { files: MutableList<Path?>? ->
                    addRunnable({ handler.openAsset(files) })
                    settings!!.urls.sequenceUrl = files!!.get(0).toString()
                })
        })

        file.addItem("Open Multiple Folders in sequence", Runnable {
            DialogueUtils.chooseFolders(settings!!.urls.sequenceUrl, Consumer { files: MutableList<Path?>? ->
                addRunnable({ handler.openAsset(files) })
                settings!!.urls.sequenceUrl = files!!.get(0).toString()
            })
        })

        val saveAs = file.addMenu("Save As")

        saveAs.addItem(
            "PK (*.pk)",
            Runnable {
                DialogueUtils.saveFile(
                    settings!!.urls.saveAsUrl,
                    "PK;pk",
                    Consumer { path: Path? -> this.save(path) })
            })
        saveAs.addItem(
            "Folder",
            Runnable {
                DialogueUtils.chooseFolder(
                    settings!!.urls.saveAsUrl,
                    Consumer { path: Path? -> this.save(path) })
            })

        file.addItem("Save", Runnable { handler.save() })


        return toolbar
    }

    private fun close() {
        try {
            Files.writeString(settingsPath, GSON.toJson(settings))
        } catch (e: IOException) {
        }

        DialogueUtils.quit()

        if (window != 0L) {
            GLFW.glfwSetWindowShouldClose(window, true)
        }
    }

    override fun cleanupGL() {
        close()
        canvas.close()
    }

    fun setTitle(title: String) {
        GLFW.glfwSetWindowTitle(window, title)
    }

    class FloatInputComponent(
        private val title: String?,
        private val originalValue: () -> Double,
        private val consumer: (Double) -> Unit
    ) {
        private val decimalFormat = DecimalFormat("#.####")

        // State
        private val value: FloatArray
        private val textBuffer = ImString("base") // user input string
        private var inputError = false

        init {
            this.value = floatArrayOf(originalValue.invoke().toFloat())
        }

        fun render() {
            ImGui.text(title + ": " + formatScaleValue(value[0]))

            // Input field as text, not just float, so MoLang expressions are possible
            ImGui.inputText("##scaleExpr", textBuffer, ImGuiInputTextFlags.EnterReturnsTrue)

            if (ImGui.button("Enter")) {
                apply()
            }
            ImGui.sameLine()
            if (ImGui.button("Reset")) {
                reset()
            }

            // Visual error indicator
            if (inputError) {
                ImGui.textColored(1f, 0f, 0f, 1f, "Invalid expression!")
            }
        }

        private fun apply() {
            try {
                val inputText = textBuffer.get()

                val runtime = MoLang.createRuntime()
                val result = runtime.execute(
                    MoLang.parse(inputText.replace("base", "context.base")),
                    mapOf<String, MoValue>("base" to  DoubleValue(originalValue.invoke()))
                ).asDouble()

                when {
                    result > 0 -> {
                        value[0] = result.toFloat()
                        consumer.invoke(value[0].toDouble())
                        inputError = false
                    }
                    else -> {
                        inputError = true
                    }
                }
            } catch (ex: Exception) {
                inputError = true
            }
        }

        fun reset() {
            value[0] = originalValue.invoke().toFloat()
            consumer.invoke(value[0].toDouble())
            textBuffer.set("base")
            inputError = false
        }

        private fun formatScaleValue(value: Float): String? {
            return decimalFormat.format(value.toDouble())
        }
    }

    public class Settings {
        var urls: Urls = Urls()
        var terastalization: Terastalization = Terastalization()
        var features: Features = Features()
        var fog: Fog = Fog()
        var values: Values = Values()
        var light: Light = Light()

        fun render() {
            features.terastalization.get().runIfTrue(terastalization::render)
            features.fog.runIfTrue(fog::render)
            features.values.runIfTrue(values::render)
            features.light.runIfTrue(light::render)
            features.render()
        }

        class Urls {
            @JvmField
            var openArchiveUrl: String = ""
            var saveAsUrl: String = ""
            var createArchiveUrl: String = ""
            @JvmField
            var sequenceUrl: String = ""
        }

        class Terastalization {
            var enabled = ImBoolean()
            var tint = Vector3f(1f, 1f, 1f)

            fun render() {
                begin("Terastalization") {
                    enabled.render("Enabled")
                    tint.render("Tint")
                }
            }
        }

        class Values {
            var gBufferDebug: ImInt = ImInt("G-Buffer Debug", 0, 0, 3)

            fun render() {
                ImGui.begin("Values")

                gBufferDebug.render()

                ImGui.end()
            }
        }

        class Features {
            var terastalization: ImBoolean = ImBoolean(true)
            var fog = true
            var values = true
            var light = true
            var grid = true
            var gizmos = true
            var largeUi = false

            fun render() {
                ImGui.begin("Features")
                terastalization.render("Terastalization")
                fog.render("Fog") { fog = it }
                values.render("Values") { values = it }
                grid.render("Grid") { grid = it }
                gizmos.render("Gizmos") { gizmos = it }
                largeUi.render("Large UI") { largeUi = it }
                light.render("Light") { light = it }
                ImGui.end()
            }
        }

        class Light {
            @JvmField
            var selected: imgui.type.ImInt = imgui.type.ImInt(0)

            @JvmField
            var standard: Standard = Standard()
            @JvmField
            var minecraft: Minecraft = Minecraft()

            class Standard {
                @JvmField
                var lightColor: Vector3f = Vector3f(1f, 1f, 1f)
                @JvmField
                var lightRange: ImFloat = ImFloat(20f)
                @JvmField
                var ambientColor: Vector3f = Vector3f(0.15f, 0.15f, 0.15f)
                @JvmField
                var shininess: ImFloat = ImFloat(32f)

                fun render() {
                    lightColor.render("Light Color")
                    ambientColor.render("Ambient Color")
                    lightRange.render("Light Range", 1f, 100f)
                    ImGui.sliderFloat("Shininess", shininess.getData(), 1f, 128f)
                }
            }

            class Minecraft {
                @JvmField
                var sky: ImInt = ImInt("Sky", 15, 0, 15)
                @JvmField
                var block: ImInt = ImInt("block", 15, 0, 15)

                fun render() {
                    sky.render()
                    block.render()
                }
            }

            fun render() = begin("Light", {
                ImGui.combo("Mode", selected, modes)

                when (selected.get()) {
                    1, 3 -> standard.render()
                    2 -> minecraft.render()
                    else -> {}
                }
            })

            companion object {
                private val modes: Array<String> = arrayOf("None", "Ambient", "Minecraft", "Diffuse")
            }
        }

        class Fog {
            var color = Vector4f(1f, 1f, 1f, 1f)
            var start = 0f
            var end = 5f

            @Transient
            private var consumer: Consumer<Fog>? = null

            fun setListener(consumer: Consumer<Fog>?) {
                this.consumer = consumer
            }

            fun render() {
                ImGui.begin("Fog")

                var dirty = false

                dirty = dirty or color.render("Color")

                dirty = dirty or start.render("Start", 0f, end) {
                    start = it
                }

                dirty = dirty or end.render("End", start, 10f) {
                    end = it
                }

                consumer?.accept(this)

                ImGui.end()

            }
        }

        companion object {
            val CODEC = codec({
                obj()
            }, {
                it.obj().let {
                    Settings()
                }
            })
        }
    }

    companion object {
        var GSON: Gson = GsonBuilder().setPrettyPrinting().create()


        private val settingsPath: Path = Paths.get("settings.json")

        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            try {
                System.loadLibrary("renderdoc")
            } catch (e: Exception) {
                println("Renderdoc not loaded. Continuing without.")
            }

            DialogueUtils.init()

            PokeUtilsGui(GuiHandler.BASE_TITLE, 250 + 512 + (512 - 482), 512).run()
        }
    }
}

private fun ImBoolean.render(name: String, builder: (Boolean) -> Unit = {}): Boolean {
    val succeeded = ImGui.checkbox(name, this)

    if(succeeded) builder.invoke(this.get())

    return succeeded
}

val vec3 = FloatArray(3)

public fun Vector3f.render(name: String, builder: (Vector3f) -> Unit = {}): Boolean {
    vec3[0] = this.x
    vec3[1] = this.y
    vec3[2] = this.z
    return ImGui.colorEdit3(name, vec3).runIfTrue {
        this.set(vec3)
        builder.invoke(this);
    }
}

fun ImFloat.render(name: String, min: Float, max: Float, builder: (Float) -> Unit = {}): Boolean {
    return ImGui.sliderFloat("Light Range", data, min, max, "%.2fx").runIfTrue { builder.invoke(this.get()) }
}

private fun Boolean.runIfTrue(run: () -> Unit): Boolean {
    if(this) run.invoke()
    return this
}

fun begin(name: String, builder: () -> Unit) {
    ImGui.begin(name)
    builder.invoke()
    ImGui.end()
}
