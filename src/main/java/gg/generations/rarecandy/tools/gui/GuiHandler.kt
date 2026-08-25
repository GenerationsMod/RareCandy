package gg.generations.rarecandy.tools.gui

import gg.generations.rarecandy.codec.toJson
import gg.generations.rarecandy.pokeutils.IModelConfig
import gg.generations.rarecandy.pokeutils.resource.FolderAsset
import gg.generations.rarecandy.pokeutils.resource.ResourceLocator
import gg.generations.rarecandy.pokeutils.util.ExceptionThrowingRunnable
import gg.generations.rarecandy.tools.gui.RareCandyCanvas.BaseMultiRenderObject
import gg.generations.rarecandy.tools.gui.RareCandyCanvas.CycleVariants
import gg.generations.rarecandy.tools.pkcreator.PixelmonArchiveBuilder
import imgui.ImGui
import org.apache.commons.io.FilenameUtils
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.io.path.isDirectory
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

class GuiHandler(private val gui: PokeUtilsGui) : KeyListener {
    private val pressedKeys: MutableSet<Int?> = HashSet<Int?>()
    private val arcBall: ArcballOrbit

    var assetPath: Path? = null
    var index: Int = 0
    var amount: Int = 0
    private var dirty = false
    var filesToOpen: MutableList<Path?> = ArrayList<Path?>()

    init {
        arcBall = ArcballOrbit(Supplier { this.canvas }, 3f, 0.125f, 0f)
    }

    fun attach(window: Long) {
        MouseMotionListener.attach(window, arcBall)
        MouseWheelListener.attach(window, arcBall)
        MouseListener.attach(window, arcBall)

        KeyListener.attach(window, this)
    }

    val canvas: RareCandyCanvas
        get() = gui.canvas

    val currentAssetName: String
        get() = if (assetPath != null) assetPath!!.fileName.toString() else "<none>"

    fun initializeAsset(path: Path?) {
        this.assetPath = path
    }

    fun save() {
        if (assetPath == null) return
        save(assetPath)
    }

    fun save(savePath: Path?): Boolean {
        var savePath = savePath?.normalized() ?: return false

        try {
            writeConfigToTemp()
            PixelmonArchiveBuilder.convertToPk(TEMP, ResourceLocator.of(savePath), this.canvas.scaleModifier)
            initializeAsset(savePath)
            setCleanTitle(savePath)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    @Throws(IOException::class)
    private fun writeConfigToTemp() {
        val json = IModelConfig.GSON.toJson(IModelConfig.CODEC, ModelTracker.config!!)
        TEMP.putFile("config.json", json.toByteArray(StandardCharsets.UTF_8))
    }

    private fun Path.normalized(): Path = this.takeUnless { it.isDirectory() }?.fileName.toString().takeIf { !it.contains(".") }.let { this.resolveSibling("$it.pk") } ?: this

    private fun setCleanTitle(path: Path) {
        dirty = false
        gui.setTitle(BASE_TITLE + " - " + path.getFileName())
    }

    fun markDirty() {
        if (!dirty) {
            this.dirty = true

            gui.setTitle(gui.title + "*")
        }
    }

    fun openAsset(filePath: Path?) {
        try {
            if (filePath == null) return

            gui.clearLoadIssues()

            move(filePath)

            initializeAsset(filePath)
            val title: String = BASE_TITLE + " - " + filePath.getFileName().toString()
            gui.setTitle(title)
            this.canvas.openFile(
                TEMP,
                FilenameUtils.getBaseName(filePath.getFileName().toString()),
                {
//                    gui.fileViewer.initializeAsset(
//                        TEMP, assetPath!!, this.canvas.loadedModel!!
//                    )
                },
                true
            )
        } catch (e: Exception) {
            gui.reportLoadIssue("Failed to open " + (if (filePath != null) filePath.getFileName() else "<null>") + ": " + e.message)
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    fun reloadCurrent() {
        initializeAsset(assetPath)
        this.canvas.openFile(TEMP, FilenameUtils.getBaseName(assetPath!!.getFileName().toString()))
    }

    override fun keyTyped(keyChar: Char, keyCode: Int, scancode: Int, mods: Int) {
    }

    override fun keyPressed(key: Int, scancode: Int, mods: Int) {
        handleKey(key, scancode, mods, false)
    }

    private fun handleKey(key: Int, scancode: Int, mods: Int, heldDown: Boolean) {
        pressedKeys.add(key)

        val isCtrlPressed = (mods and GLFW.GLFW_MOD_CONTROL) != 0
        val isShiftPressed = (mods and GLFW.GLFW_MOD_SHIFT) != 0
        val isAltPressed = (mods and GLFW.GLFW_MOD_ALT) != 0

        if (heldDown) {
            arcBall.keyPressed(key)
        } else if (isCtrlPressed) {
            when (key) {
                GLFW.GLFW_KEY_S -> save()
                GLFW.GLFW_KEY_SPACE -> arcBall.reset()
            }
        } else if (isAltPressed) {
//            switch (code) {
//                case KeyEvent.VK_A -> new RareCandyCanvas.CycleVariants(getCanvas(), true);
//                case KeyEvent.VK_Z -> new RareCandyCanvas.CycleVariants(getCanvas(), false);
//            }
        } else if (isShiftPressed) {
            when (key) {
                GLFW.GLFW_KEY_P -> {
                    CycleVariants(this.canvas, true)
                }

                GLFW.GLFW_KEY_O -> {
                    DialogueUtils.chooseMultipleFiles(
                        "Choose multiple files",
                        gui.settings!!.urls.sequenceUrl,
                        "PK;pk", this::openAsset)
                }

                GLFW.GLFW_KEY_SPACE -> {
                    try {
                        reloadCurrent()
                    } catch (ex: Exception) {
                        throw RuntimeException(ex)
                    }
                }
            }
        } else {
            when (key) {
                GLFW.GLFW_KEY_P -> {
                    CycleVariants(this.canvas, false)
                }

                GLFW.GLFW_KEY_O -> {
                    if (filesToOpen.isEmpty()) {
                        DialogueUtils.chooseFile(
                            this.gui.settings!!.urls.openArchiveUrl,
                            "PK;pk",
                            Consumer { filePath: Path? -> this.openAsset(filePath) })
                    } else {
                        index++
                        val chosenFile = filesToOpen.removeAt(0)
                        println("Selecting: " + (index + 1) + "/" + amount + " - " + chosenFile)
                        if (chosenFile != null) openAsset(chosenFile)
                    }
                }

                GLFW.GLFW_KEY_LEFT_BRACKET -> {
                    val light = gui.settings!!.light.minecraft.sky
                    val value = light.getValue() - 1
                    light.setValue(value)
                }

                GLFW.GLFW_KEY_RIGHT_BRACKET -> {
                    val light = gui.settings!!.light.minecraft.sky
                    val value = light.getValue() + 1
                    light.setValue(value)
                }

                GLFW.GLFW_KEY_SPACE -> AnimationState.togglePause()
                else -> arcBall.keyPressed(key)
            }
        }
    }

    override fun keyReleased(keyCode: Int, scancode: Int, mods: Int) {
//        System.out.println("Before: " + pressedKeys);
//        pressedKeys.remove((Integer) e.getKeyCode());
//        System.out.println("After: " + pressedKeys);
    }

    override fun keyHeld(key: Int, scancode: Int, mods: Int) {
        handleKey(key, scancode, mods, true)
    }

    fun openAsset(chosenFiles: MutableList<Path?>?) {
        if (chosenFiles.isNullOrEmpty()) {
            gui.reportLoadIssue("No files selected.")
            return
        }

        println("Loading " + chosenFiles.size + " into queue.")
        index = 0
        amount = chosenFiles.size

        filesToOpen.clear()
        filesToOpen.addAll(chosenFiles)

        println("Selecting: " + (index + 1) + "/" + amount)
        openAsset(filesToOpen.removeAt(0))
    }

    inner class ArcballOrbit(
        private val canvas: Supplier<RareCandyCanvas?>,
        private var radius: Float,
        private var angleX: Float,
        private var angleY: Float
    ) : MouseMotionListener, MouseWheelListener, MouseListener {
        private var lastX = 0f
        private var lastY = 0f
        private var offsetX = 0f
        private var offsetY = 0f

        private val centerOffset = Vector3f()
        private val forward = Vector3f(0f, 0f, -1f)
        private val right = Vector3f(1f, 0f, 0f)

        init {
            update()
        }

        fun update() {
            val yaw = (angleX + offsetX) * Math.PI.toFloat() * 2f
            val cos = cos(yaw.toDouble()).toFloat()
            val sin = sin(yaw.toDouble()).toFloat()

            forward.set(sin, 0f, -cos)
            right.set(cos, 0f, sin)

            val can: RareCandyCanvas? = canvas.get()

            if (can != null) can.camera.setViewMatrix(Consumer { matrix: Matrix4f? ->
                matrix!!.identity().arcball(
                    radius,
                    centerOffset.x,
                    centerOffset.y,
                    centerOffset.z,
                    (angleY + offsetY) * Math.PI.toFloat() * 2f,
                    (angleX + offsetX) * Math.PI.toFloat() * 2f
                )
            })
        }

        override fun mouseDragged(window: Long, x: Double, y: Double) {
            var dx = ((x - lastX) * 0.001f).toFloat()
            val dy = ((y - lastY) * 0.001f).toFloat()

            // Determine inversion from current orientation (no prediction)
            var currentPitch = (angleY + offsetY) * Math.PI.toFloat() * 2f
            currentPitch = currentPitch - floor(currentPitch.toDouble()).toFloat() // normalize to [0,1)
            if (cos(currentPitch.toDouble()) < 0f) {
                dx = -dx
            }

            offsetX = dx
            offsetY = dy
            update()
        }


        override fun mouseMoved(window: Long, x: Double, y: Double) {}

        override fun mouseWheelMoved(window: Long, xoffset: Double, yoffset: Double) {
            if (ImGui.getIO().getWantCaptureMouse()) return
            val scrollAmount = yoffset.toFloat()
            radius += scrollAmount * 0.1f
            update()
        }

        override fun mouseClicked(window: Long, button: Int, mods: Int, x: Double, y: Double) {}

        override fun mousePressed(window: Long, button: Int, mods: Int, x: Double, y: Double) {
            offsetX = 0f
            offsetY = 0f

            lastX = x.toFloat()
            lastY = y.toFloat()
        }

        override fun mouseReleased(window: Long, button: Int, mods: Int, x: Double, y: Double) {
            angleX += offsetX
            angleY += offsetY
            offsetX = 0f
            offsetY = 0f
            lastX = 0f
            lastY = 0f

            update()
        }

        fun keyPressed(code: Int) {
            val lateralStep = 0.01f // Adjust the step size as needed

            if (!RareCandyCanvas.cycling) {
                when (code) {
                    GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_A -> centerOffset.fma(-lateralStep, right)
                    GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_D -> centerOffset.fma(lateralStep, right)
                    GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_W -> centerOffset.fma(lateralStep, forward)
                    GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_S -> centerOffset.fma(-lateralStep, forward)
                    GLFW.GLFW_KEY_PAGE_UP, GLFW.GLFW_KEY_Q -> centerOffset.y += lateralStep
                    GLFW.GLFW_KEY_PAGE_DOWN, GLFW.GLFW_KEY_E -> centerOffset.y -= lateralStep
                }

                update()
            }
        }

        fun reset() {
            val loadedModel: BaseMultiRenderObject? = canvas.get()!!.loadedModel

            if (loadedModel == null) {
                radius = 2f
                centerOffset.set(0f, 0f, 0f)
            } else {
                radius = ((loadedModel.dimensions.get(loadedModel.dimensions.maxComponent())) * loadedModel.scale) / 2f
                centerOffset.set(0f, radius, 0f)
            }

            lastY = 0f
            lastX = lastY
            angleX = -0.125f
            angleY = 0.125f
        }
    }

    companion object {
        val TEMP: FolderAsset = FolderAsset(Path.of("temp"))
        const val BASE_TITLE: String = "Pk Explorer"

        @Throws(IOException::class)
        fun move(path: Path?) {
            TEMP.getFileNames().forEach(Consumer { key: String? -> TEMP.deleteFile(key) })

            val seven = ResourceLocator.of(path)

            for (file in seven.getFileNames()) {
                if (file.isEmpty()) continue

                TEMP.putFile(file, seven.getFile(file))
            }
        }
    }
}
