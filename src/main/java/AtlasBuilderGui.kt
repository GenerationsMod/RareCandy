import AtlasCompacter.*
import com.google.gson.JsonObject
import gg.generations.rarecandy.pokeutils.IModelConfig
import gg.generations.rarecandy.pokeutils.resource.PkResourceLocator
import gg.generations.rarecandy.pokeutils.resource.ResourceLocator
import gg.generations.rarecandy.renderer.launch.OpenGL
import gg.generations.rarecandy.renderer.textures.ITexture
import gg.generations.rarecandy.renderer.textures.Texture
import gg.generations.rarecandy.tools.AppBase
import gg.generations.rarecandy.tools.gui.AdvancedMenuBar
import gg.generations.rarecandy.tools.gui.DialogueUtils
import imgui.ImColor
import imgui.ImDrawList
import imgui.ImGui
import imgui.ImVec2
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiMouseButton
import imgui.flag.ImGuiMouseCursor
import imgui.flag.ImGuiWindowFlags
import imgui.type.ImInt
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWWindowCloseCallbackI
import java.awt.AlphaComposite
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.Map
import java.util.function.Consumer
import java.util.function.ToIntFunction
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class AtlasBuilderGui : AppBase("Atlas Builder", MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT, OpenGL()) {
    private val menu: AdvancedMenuBar
    private val textures = mutableListOf<TextureEntry>()
    private val placements = mutableListOf<TexturePlacement>()
    private val skippedImages = mutableListOf<String>()
    private val snapPixels = ImInt(1)

    private var locator: ResourceLocator? = null
    private var assetPath: Path? = null
    private var baseConfig: JsonObject? = null
    private var currentConfig: JsonObject? = null
    private var pageCount = 0
    private var selectedPage = 0
    private var selected: TexturePlacement? = null
    private var dragging: TexturePlacement? = null
    private var dragStartX = 0
    private var dragStartY = 0
    private var dragStartMouseX = 0f
    private var dragStartMouseY = 0f
    private var zoom = 0.75f
    private var deleteSourceImages = true
    private var dirty = false
    private var status = "Open a PK or folder to begin."

    init {
        this.menu = configureMenu()
    }

    override fun initWindow() {
        super.initWindow()
        GLFW.glfwSetWindowCloseCallback(
            window,
            GLFWWindowCloseCallbackI { ignored: Long -> GLFW.glfwSetWindowShouldClose(window, true) })
    }

    override fun initGL() {
    }

    override fun renderGui() {
        menu.render()
        renderAtlasWindow()
        renderTextureWindow()
        renderDetailsWindow()
    }

    override fun render() {
    }

    public override fun clearColor(): Vector4f {
        return CLEAR_COLOR
    }

    override fun cleanupGL() {
        closePreviewTextures()
        DialogueUtils.quit()
    }

    private fun configureMenu(): AdvancedMenuBar {
        val toolbar = AdvancedMenuBar()
        val file = toolbar.addMenu("File")
        file.addItem(
            "Open PK (*.pk)",
            Runnable { DialogueUtils.chooseFile(defaultPath(), "PK;pk", Consumer { path: Path? -> this.open(path) }) })
        file.addItem(
            "Open Folder",
            Runnable { DialogueUtils.chooseFolder(defaultPath(), Consumer { path: Path? -> this.open(path) }) })
        file.addItem("Save", Runnable { addRunnable { this.saveInPlace() } })
        file.addItem(
            "Save As PK (*.pk)",
            Runnable {
                DialogueUtils.saveFile(
                    defaultPath(),
                    "PK;pk",
                    Consumer { path: Path? -> addRunnable { saveAs(path) } })
            })

        val layout = toolbar.addMenu("Layout")
        layout.addItem("Auto Pack", Runnable { addRunnable { this.autoPack() } })
        layout.addItem("Add Page", Runnable { addRunnable { this.addPage() } })
        layout.addItem("Remove Empty Page", Runnable { addRunnable { this.removeCurrentPageIfEmpty() } })
        return toolbar
    }

    private fun defaultPath(): String {
        if (assetPath == null) return ""
        val parent = if (Files.isDirectory(assetPath)) assetPath else assetPath!!.getParent()
        return if (parent != null) parent.toString() else assetPath.toString()
    }

    private fun open(path: Path?) {
        if (path == null) return
        addRunnable { openNow(path) }
    }

    private fun openNow(path: Path) {
        try {
            closePreviewTextures()
            textures.clear()
            placements.clear()
            skippedImages.clear()
            selected = null
            dragging = null
            pageCount = 0
            selectedPage = 0

            locator = ResourceLocator.of(path)
            assetPath = path
            baseConfig = locator?.readModelConfig()
            currentConfig = baseConfig?.deepCopy()

            loadTextures(locator)
            autoPack()
            dirty = false
            status = "Loaded " + textures.size + " texture(s) from " + path.getFileName() + "."
            setWindowTitle()
        } catch (exception: Exception) {
            status = "Open failed: " + exception.message
            exception.printStackTrace()
        }
    }

    fun ResourceLocator.readModelConfig() : JsonObject? = this.takeIf { this.hasFile("config.json") }
        ?.getInputStream("config.json")
        ?.let { InputStreamReader(it, StandardCharsets.UTF_8) }
        ?.use { IModelConfig.GSON.fromJson(it, JsonObject::class.java) }

    @Throws(IOException::class)
    private fun loadTextures(source: ResourceLocator?) {
        if(source == null) return

        val names = ArrayList(source.getFileNames())
        names.sortWith { obj: String, str: String -> obj.compareTo(str, ignoreCase = true) }

        for (name in names) {
            if (name.isNullOrBlank()) continue

            val bytes = source.getFile(name)
            val image = ImageIO.read(ByteArrayInputStream(bytes)) ?: continue

            val imageWidth = image.width
            val imageHeight = image.height
            var packWidth = imageWidth
            var packHeight = imageHeight

            if (imageWidth == ATLAS_SIZE && imageHeight == ATLAS_SIZE && isAtlasFileName(name)) {
                skippedImages.add("$name - existing atlas page")
                continue
            }

            if (imageWidth > ATLAS_SIZE || imageHeight > ATLAS_SIZE) {
                if (imageWidth == 2048 && imageHeight == 1024) {
                    packWidth = 1024
                    packHeight = 512
                } else if (imageWidth == 2048 && imageHeight == 2048) {
                    packWidth = 1024
                    packHeight = 1024
                } else {
                    skippedImages.add(name + " - too large (" + imageWidth + "x" + imageHeight + ")")
                    continue
                }
            }

            val previewTexture: ITexture? = Texture.read(bytes, name)

            textures.add(
                TextureEntry(
                    name,
                    image,
                    imageWidth,
                    imageHeight,
                    packWidth,
                    packHeight,
                    previewTexture,
                    colorForName(name)
                )
            )
        }
    }

    private fun autoPack() {
        if (textures.isEmpty()) {
            placements.clear()
            pageCount = 0
            selectedPage = 0
            selected = null
            status = if (locator == null) "Open a PK or folder first." else "No packable textures found."
            return
        }

        placements.clear()
        selected = null
        selectedPage = 0

        val pages: MutableList<PackingPage> = ArrayList<PackingPage>()
        val sorted: ArrayList<TextureEntry> = ArrayList<TextureEntry>(textures)
        sorted.sortWith(Comparator.comparing<TextureEntry, Int> { a -> a.area() }.thenComparing { a -> a.maxDimension() }.thenComparing { a -> a.name })
        (
            Comparator.comparingInt(ToIntFunction { obj: TextureEntry -> obj.area() }).reversed()
                .thenComparing(
                    Comparator.comparingInt(ToIntFunction { obj: TextureEntry -> obj.maxDimension() })
                        .reversed()
                )
                .thenComparing(TextureEntry::name)
        )

        for (texture in sorted) {
            var best: PackingCandidate? = null

            for (page in pages) {
                val candidate = page.preview(texture.packWidth, texture.packHeight)
                if (candidate != null && (best == null || candidate.isBetterThan(best))) {
                    best = candidate
                }
            }

            if (best == null) {
                val page = PackingPage(pages.size)
                pages.add(page)
                best = page.preview(texture.packWidth, texture.packHeight)
            }

            if (best == null) {
                skippedImages.add(texture.name + " - could not fit in a new atlas page")
                continue
            }

            val rect = pages.get(best.pageIndex).insert(texture.packWidth, texture.packHeight)
            placements.add(TexturePlacement(texture, best.pageIndex, rect!!.x, rect.y, rect.width, rect.height))
        }

        pageCount = pages.size
        selected = placements[0]
        selectedPage = selected?.pageIndex ?: 0
        markDirty()
        status = "Auto-packed ${placements.size} texture(s) into $pageCount atlas page(s)."
    }

    private fun addPage() {
        if (locator == null) {
            status = "Open a PK or folder first."
            return
        }

        pageCount++
        selectedPage = max(0, pageCount - 1)
        markDirty()
        status = "Added atlas page $selectedPage."
    }

    private fun removeCurrentPageIfEmpty() {
        if (pageCount <= 0) return

        for (placement in placements) {
            if (placement.pageIndex == selectedPage) {
                status = "Page $selectedPage is not empty."
                return
            }
        }

        val removed = selectedPage
        pageCount--
        for (placement in placements) {
            if (placement.pageIndex > removed) placement.pageIndex--
        }
        selectedPage = clamp(selectedPage, 0, max(0, pageCount - 1))
        markDirty()
        status = "Removed empty atlas page $removed."
    }

    private fun renderAtlasWindow() {
        ImGui.setNextWindowSize(820f, 720f, ImGuiCondOnce.VALUE)
        ImGui.begin("Atlas")

        if (locator == null) {
            ImGui.textWrapped(status)
            ImGui.end()
            return
        }

        renderAtlasControls()

        if (pageCount <= 0) {
            ImGui.textWrapped("No atlas pages to show.")
            ImGui.end()
            return
        }

        val canvasSize: Float = ATLAS_SIZE * zoom
        ImGui.beginChild(
            "AtlasCanvasScroll",
            canvasSize + 24f,
            canvasSize + 24f,
            true,
            ImGuiWindowFlags.HorizontalScrollbar
        )
        drawAtlasCanvas(canvasSize)
        ImGui.endChild()

        ImGui.end()
    }

    private fun renderAtlasControls() {
        if (ImGui.button("Auto Pack")) {
            autoPack()
        }
        ImGui.sameLine()
        if (ImGui.button("Add Page")) {
            addPage()
        }
        ImGui.sameLine()
        if (ImGui.button("Remove Empty Page")) {
            removeCurrentPageIfEmpty()
        }

        val zoomValue = floatArrayOf(zoom)
        if (ImGui.sliderFloat("Zoom", zoomValue, 0.25f, 1.5f, "%.2fx")) {
            zoom = zoomValue[0]
        }

        if (ImGui.inputInt("Snap Pixels", snapPixels, 1, 16)) {
            snapPixels.set(max(1, snapPixels.get()))
        }

        if (ImGui.checkbox("Delete source images on save", deleteSourceImages)) {
            deleteSourceImages = !deleteSourceImages
        }

        if (pageCount > 0) {
            ImGui.text("Page " + (selectedPage + 1) + " / " + pageCount + " - " + pageUsagePercent(selectedPage) + "% used")
            if (ImGui.button("<")) {
                selectedPage = clamp(selectedPage - 1, 0, pageCount - 1)
            }
            ImGui.sameLine()
            if (ImGui.button(">")) {
                selectedPage = clamp(selectedPage + 1, 0, pageCount - 1)
            }
            ImGui.sameLine()
            for (page in 0..<pageCount) {
                if (page > 0) ImGui.sameLine()
                if (ImGui.radioButton((page + 1).toString(), selectedPage == page)) {
                    selectedPage = page
                }
            }
        }

        if (hasOverlaps()) {
            ImGui.textColored(1f, 0.35f, 0.25f, 1f, "Resolve overlapping placements before saving.")
        }

        ImGui.separator()
    }

    private fun drawAtlasCanvas(canvasSize: Float) {
        val canvasMin = ImGui.getCursorScreenPos()
        val canvasMaxX = canvasMin.x + canvasSize
        val canvasMaxY = canvasMin.y + canvasSize
        val scale: Float = canvasSize / ATLAS_SIZE
        val drawList = ImGui.getWindowDrawList()

        drawList.addRectFilled(canvasMin.x, canvasMin.y, canvasMaxX, canvasMaxY, ImGui.getColorU32(ImGuiCol.FrameBg))
        drawCheckerboard(drawList, canvasMin, canvasSize)
        drawGrid(drawList, canvasMin, canvasSize)

        for (placement in placementsForPage(selectedPage)) {
            drawPlacement(drawList, placement, canvasMin, scale)
        }

        ImGui.setCursorScreenPos(canvasMin.x, canvasMin.y)
        ImGui.invisibleButton("##atlasCanvas", canvasSize, canvasSize)
        handleCanvasInput(canvasMin, scale)
    }

    private fun drawCheckerboard(drawList: ImDrawList, canvasMin: ImVec2, canvasSize: Float) {
        val dark = ImColor.rgba(42, 45, 48, 255)
        val light = ImColor.rgba(52, 55, 58, 255)
        val tile = 32f * zoom
        var y = 0f
        while (y < canvasSize) {
            var x = 0f
            while (x < canvasSize) {
                val color = if ((((x / tile).toInt() + (y / tile).toInt()) and 1) == 0) dark else light
                drawList.addRectFilled(
                    canvasMin.x + x,
                    canvasMin.y + y,
                    canvasMin.x + min(canvasSize, x + tile),
                    canvasMin.y + min(canvasSize, y + tile),
                    color
                )
                x += tile
            }
            y += tile
        }
    }

    private fun drawGrid(drawList: ImDrawList, canvasMin: ImVec2, canvasSize: Float) {
        val color = ImColor.rgba(255, 255, 255, 26)
        var pixel = 0
        while (pixel <= ATLAS_SIZE) {
            var pos = canvasMin.x + pixel * zoom
            drawList.addLine(pos, canvasMin.y, pos, canvasMin.y + canvasSize, color)
            pos = canvasMin.y + pixel * zoom
            drawList.addLine(canvasMin.x, pos, canvasMin.x + canvasSize, pos, color)
            pixel += GRID_STEP
        }
    }

    private fun drawPlacement(drawList: ImDrawList, placement: TexturePlacement, canvasMin: ImVec2, scale: Float) {
        val x0 = canvasMin.x + placement.x * scale
        val y0 = canvasMin.y + placement.y * scale
        val x1 = x0 + placement.width * scale
        val y1 = y0 + placement.height * scale
        val selectedPlacement = placement == selected
        val overlapping = overlapsAny(placement)
        val border = if (selectedPlacement)
            ImColor.rgba(255, 207, 84, 255)
        else
            if (overlapping) ImColor.rgba(255, 84, 72, 255) else ImColor.rgba(120, 190, 255, 230)

        if (placement.texture.previewTexture != null) {
            drawList.addImage(placement.texture.previewTexture.id.toLong(), x0, y0, x1, y1, 0f, 0f, 1f, 1f)
        } else {
            drawList.addRectFilled(x0, y0, x1, y1, placement.texture.color)
        }

        drawList.addRectFilled(x0, y0, x1, min(y1, y0 + 22f), ImColor.rgba(0, 0, 0, 135))
        drawList.pushClipRect(x0 + 3f, y0 + 2f, x1 - 3f, min(y1, y0 + 22f), true)
        drawList.addText(x0 + 4f, y0 + 3f, ImColor.rgba(245, 247, 250, 255), placement.texture.name)
        drawList.popClipRect()

        drawList.addRect(x0, y0, x1, y1, border, 0f, 0, if (selectedPlacement) 3f else 2f)
    }

    private fun handleCanvasInput(canvasMin: ImVec2, scale: Float) {
        val hovered = ImGui.isItemHovered()
        val io = ImGui.getIO()
        val mouseX = io.getMousePosX()
        val mouseY = io.getMousePosY()

        val hoveredPlacement = if (hovered) placementAt(mouseX, mouseY, canvasMin, scale) else null
        if (hoveredPlacement != null) {
            ImGui.setMouseCursor(ImGuiMouseCursor.ResizeAll)
        }

        if (hovered && ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
            selected = hoveredPlacement
            dragging = hoveredPlacement


            if (dragging != null) {
                dragStartX = dragging!!.x
                dragStartY = dragging!!.y
                dragStartMouseX = mouseX
                dragStartMouseY = mouseY
            }
        }

        if (dragging != null) {
            if (!ImGui.isMouseDown(ImGuiMouseButton.Left)) {
                dragging = null
                return
            }

            val newX = dragStartX + Math.round((mouseX - dragStartMouseX) / scale)
            val newY = dragStartY + Math.round((mouseY - dragStartMouseY) / scale)
            movePlacement(dragging!!, newX, newY)
        }
    }

    private fun placementAt(mouseX: Float, mouseY: Float, canvasMin: ImVec2, scale: Float): TexturePlacement? {
        val pagePlacements = placementsForPage(selectedPage)
        for (i in pagePlacements.indices.reversed()) {
            val placement = pagePlacements.get(i)
            val x0 = canvasMin.x + placement.x * scale
            val y0 = canvasMin.y + placement.y * scale
            val x1 = x0 + placement.width * scale
            val y1 = y0 + placement.height * scale
            if (mouseX >= x0 && mouseX <= x1 && mouseY >= y0 && mouseY <= y1) {
                return placement
            }
        }
        return null
    }

    private fun movePlacement(placement: TexturePlacement, x: Int, y: Int) {
        var x = x
        var y = y
        val snap = max(1, snapPixels.get())
        x = (x / snap.toFloat()).roundToInt() * snap
        y = (y / snap.toFloat()).roundToInt() * snap
        val clampedX: Int = clamp(x, 0, ATLAS_SIZE - placement.width)
        val clampedY: Int = clamp(y, 0, ATLAS_SIZE - placement.height)
        if (placement.x != clampedX || placement.y != clampedY) {
            placement.x = clampedX
            placement.y = clampedY
            markDirty()
        }
    }

    private fun renderTextureWindow() {
        ImGui.setNextWindowSize(340f, 720f, ImGuiCondOnce.VALUE)
        ImGui.begin("Textures")

        if (locator == null) {
            ImGui.textWrapped("No asset loaded.")
            ImGui.end()
            return
        }

        ImGui.text(textures.size.toString() + " packable texture(s)")
        if (!skippedImages.isEmpty() && ImGui.treeNode("Skipped (" + skippedImages.size + ")")) {
            for (skipped in skippedImages) {
                ImGui.textWrapped(skipped)
            }
            ImGui.treePop()
        }

        ImGui.separator()
        ImGui.beginChild("TextureList", 0f, 360f, true)
        for (placement in placements) {
            ImGui.pushID(placement.texture.name)
            val label = (placement.texture.name
                    + "  [" + placement.texture.packWidth + "x" + placement.texture.packHeight + "]"
                    + "  p" + (placement.pageIndex + 1))
            if (ImGui.selectable(label, placement == selected)) {
                selected = placement
                selectedPage = placement.pageIndex
            }
            ImGui.popID()
        }
        ImGui.endChild()

        selected?.texture?.also {
            ImGui.separator()
            ImGui.textWrapped(it.name)
            drawPreview(it)
        }

        ImGui.end()
    }

    private fun drawPreview(texture: TextureEntry) {
        if (texture.previewTexture == null) {
            ImGui.text("Preview unavailable")
            return
        }

        val max = 256f
        val width = texture.sourceWidth.toFloat()
        val height = texture.sourceHeight.toFloat()
        val scale = min(max / width, max / height)
        ImGui.image(texture.previewTexture.id.toLong(), width * scale, height * scale)
    }

    private fun renderDetailsWindow() {
        ImGui.setNextWindowSize(340f, 250f, ImGuiCondOnce.VALUE)
        ImGui.begin("Atlas Details")

        ImGui.textWrapped(status)
        if (assetPath != null) {
            ImGui.textWrapped("Asset: " + assetPath)
        }
        if (dirty) {
            ImGui.textColored(1f, 0.82f, 0.35f, 1f, "Unsaved layout changes")
        }

        ImGui.separator()
        if (selected == null) {
            ImGui.textWrapped("Select a texture placement to edit its atlas coordinates.")
        } else {
            renderSelectedPlacementControls()
        }

        ImGui.separator()
        if (ImGui.button("Save")) {
            saveInPlace()
        }
        ImGui.sameLine()
        if (ImGui.button("Save As PK")) {
            DialogueUtils.saveFile(defaultPath(), "PK;pk", Consumer { path: Path? -> addRunnable { saveAs(path) } })
        }

        ImGui.end()
    }

    private fun renderSelectedPlacementControls() {
        selected?.also { selected ->

            ImGui.text("Selected")
            ImGui.textWrapped(selected.texture.name)

            val position = intArrayOf(selected.x, selected.y)
            if (ImGui.inputInt2("Position", position)) {
                movePlacement(selected!!, position[0], position[1])
            }

            val size = intArrayOf(selected.width, selected.height)
            if (ImGui.inputInt2("Size", size)) {
                val newWidth: Int = clamp(size[0], 1, ATLAS_SIZE)
                val newHeight: Int = clamp(size[1], 1, ATLAS_SIZE)
                if (newWidth != selected.width || newHeight != selected.height) {
                    selected.width = newWidth
                    selected.height = newHeight
                    movePlacement(selected!!, selected.x, selected.y)
                    markDirty()
                }
            }

            val page = ImInt(selected.pageIndex + 1)
            if (ImGui.inputInt("Page", page, 1, 1)) {
                val targetPage: Int = clamp(page.get() - 1, 0, max(0, pageCount - 1))
                if (targetPage != selected.pageIndex) {
                    selected.pageIndex = targetPage
                    selectedPage = targetPage
                    movePlacement(selected!!, selected.x, selected.y)
                    markDirty()
                }
            }

            if (ImGui.button("Native Size")) {
                selected.width = selected.texture.packWidth
                selected.height = selected.texture.packHeight
                movePlacement(selected, selected.x, selected.y)
                markDirty()
            }
        }
    }

    private fun saveInPlace() {
        if (locator == null || assetPath == null) {
            status = "Nothing to save."
            return
        }

        try {
            writeAtlas(locator!!)
            locator!!.save()
            dirty = false
            status = "Saved " + assetPath!!.getFileName() + "."
            setWindowTitle()
        } catch (exception: Exception) {
            status = "Save failed: " + exception.message
            exception.printStackTrace()
        }
    }

    private fun saveAs(path: Path?) {
        if (path == null || locator == null) return

        try {
            val target = normalizePkPath(path)
            val output = createOutputLocator(target)
            copyLocator(locator!!, output)
            writeAtlas(output)
            output.save(target)
            locator = output
            assetPath = target
            dirty = false
            status = "Saved " + target.getFileName() + "."
            setWindowTitle()
        } catch (exception: Exception) {
            status = "Save As failed: " + exception.message
            exception.printStackTrace()
        }
    }

    private fun normalizePkPath(path: Path): Path {
        val fileName = if (path.getFileName() != null) path.getFileName().toString() else ""
        if (!fileName.contains(".")) {
            return path.resolveSibling(fileName + ".pk")
        }
        return path
    }

    @Throws(IOException::class)
    private fun createOutputLocator(target: Path): ResourceLocator {
        val fileName = target.getFileName().toString().lowercase()
        if (fileName.endsWith(".pk")) return PkResourceLocator()
        return ResourceLocator.of(target)
    }

    @Throws(IOException::class)
    private fun copyLocator(source: ResourceLocator, output: ResourceLocator) {
        for (file in source.getFileNames()) {
            output.putFile(file, source.getFile(file))
        }
    }

    @Throws(IOException::class)
    private fun writeAtlas(target: ResourceLocator) {
        check(!placements.isEmpty()) { "No atlas placements to save." }
        check(!hasOverlaps()) { "Atlas placements overlap." }

        for (page in 0..<pageCount) {
            val atlasImage = BufferedImage(ATLAS_SIZE, ATLAS_SIZE, BufferedImage.TYPE_INT_ARGB)
            val graphics = atlasImage.createGraphics()
            graphics.setComposite(AlphaComposite.Src)

            for (placement in placementsForPage(page)) {
                graphics.drawImage(
                    placement.texture.image,
                    placement.x,
                    placement.y,
                    placement.width,
                    placement.height,
                    null
                )
            }

            graphics.dispose()
            val outputStream = ByteArrayOutputStream()
            ImageIO.write(atlasImage, "PNG", outputStream)
            target.putFile(atlasFileName(page), outputStream.toByteArray())
        }

        val atlasBuild = AtlasBuild(mutableListOf<Atlas?>(), packedTextures())
        if (baseConfig != null) {
            currentConfig = baseConfig!!.deepCopy()
            val transforms = MaterialCompressor.applyAtlasTextures(currentConfig, atlasBuild)
            MaterialCompressor.applyAtlasTransformsToDefaultVariant(currentConfig, transforms)
            MaterialCompressor.applyAtlasTransformsToAllVariants(currentConfig, transforms)
            MaterialCompressor.removeDuplicateMaterialsAfterAtlas(currentConfig, atlasBuild)
            target.putFile("config.json", IModelConfig.GSON.toJson(currentConfig).toByteArray(StandardCharsets.UTF_8))
        }

        if (deleteSourceImages) {
            MaterialCompressor.deleteCompactedImages(target, atlasBuild)
        }
    }

    private fun packedTextures(): MutableMap<String?, PackedTexture?> {
        val packed: MutableMap<String?, PackedTexture?> = LinkedHashMap<String?, PackedTexture?>()
        for (placement in placements) {
            packed.put(
                placement.texture.name,
                PackedTexture(
                    placement.texture.name,
                    atlasFileName(placement.pageIndex),
                    placement.x,
                    placement.y,
                    placement.width,
                    placement.height
                )
            )
        }
        return Map.copyOf<String?, PackedTexture?>(packed)
    }

    private fun atlasFileName(page: Int): String {
        return "atlas_" + String.format(Locale.ROOT, "%02d", page) + ".png"
    }

    private fun placementsForPage(page: Int): MutableList<TexturePlacement> {
        val result: MutableList<TexturePlacement> = ArrayList<TexturePlacement>()
        for (placement in placements) {
            if (placement.pageIndex == page) result.add(placement)
        }
        return result
    }

    private fun hasOverlaps(): Boolean {
        for (i in placements.indices) {
            for (j in i + 1..<placements.size) {
                val first = placements.get(i)
                val second = placements.get(j)
                if (first.pageIndex == second.pageIndex && overlaps(first, second)) return true
            }
        }
        return false
    }

    private fun overlapsAny(placement: TexturePlacement): Boolean {
        for (other in placements) {
            if (placement != other && placement.pageIndex == other.pageIndex && overlaps(placement, other)) {
                return true
            }
        }
        return false
    }

    private fun overlaps(first: TexturePlacement, second: TexturePlacement): Boolean {
        return first.x < second.x + second.width && first.x + first.width > second.x && first.y < second.y + second.height && first.y + first.height > second.y
    }

    private fun pageUsagePercent(page: Int): Int {
        var used = 0
        for (placement in placements) {
            if (placement.pageIndex == page) used += placement.width * placement.height
        }
        return Math.round(used * 100f / (ATLAS_SIZE * ATLAS_SIZE))
    }

    private fun setWindowTitle() {
        val name = if (assetPath != null) assetPath!!.getFileName().toString() else "Atlas Builder"
        GLFW.glfwSetWindowTitle(window, "Atlas Builder - " + name + (if (dirty) "*" else ""))
    }

    private fun markDirty() {
        if (!dirty) {
            dirty = true
            setWindowTitle()
        }
    }

    private fun closePreviewTextures() {
        for (texture in textures) {
            if (texture.previewTexture == null) continue
            try {
                texture.previewTexture.delete()
            } catch (ignored: IOException) {
            }
        }
    }

    @JvmRecord
    private data class TextureEntry(
        val name: String,
        val image: BufferedImage,
        val sourceWidth: Int,
        val sourceHeight: Int,
        val packWidth: Int,
        val packHeight: Int,
        val previewTexture: ITexture?,
        val color: Int
    ) {
        fun area(): Int {
            return packWidth * packHeight
        }

        fun maxDimension(): Int {
            return max(packWidth, packHeight)
        }
    }

    private class TexturePlacement(
        val texture: TextureEntry,
        var pageIndex: Int,
        var x: Int,
        var y: Int,
        var width: Int,
        var height: Int
    )

    @JvmRecord
    private data class Rect(val x: Int, val y: Int, val width: Int, val height: Int) {
        fun contains(other: Rect): Boolean {
            return other.x >= x && other.y >= y && other.x + other.width <= x + width && other.y + other.height <= y + height
        }
    }

    @JvmRecord
    private data class PackingCandidate(
        val pageIndex: Int,
        val rect: Rect?,
        val shortSideFit: Int,
        val longSideFit: Int
    ) {
        fun isBetterThan(other: PackingCandidate): Boolean {
            if (shortSideFit != other.shortSideFit) return shortSideFit < other.shortSideFit
            if (longSideFit != other.longSideFit) return longSideFit < other.longSideFit
            if (rect!!.y != other.rect!!.y) return rect.y < other.rect.y
            if (rect.x != other.rect.x) return rect.x < other.rect.x
            return pageIndex < other.pageIndex
        }
    }

    private class PackingPage(private val index: Int) {
        private val freeRectangles: MutableList<Rect> = ArrayList<Rect>()

        init {
            freeRectangles.add(Rect(0, 0, ATLAS_SIZE, ATLAS_SIZE))
        }

        fun preview(width: Int, height: Int): PackingCandidate? {
            var best: PackingCandidate? = null
            for (freeRectangle in freeRectangles) {
                if (freeRectangle.width < width || freeRectangle.height < height) continue

                val leftoverHoriz = freeRectangle.width - width
                val leftoverVert = freeRectangle.height - height
                val rect = Rect(freeRectangle.x, freeRectangle.y, width, height)
                val candidate = PackingCandidate(
                    index,
                    rect,
                    min(leftoverHoriz, leftoverVert),
                    max(leftoverHoriz, leftoverVert)
                )
                if (best == null || candidate.isBetterThan(best)) best = candidate
            }
            return best
        }

        fun insert(width: Int, height: Int): Rect? {
            val candidate = preview(width, height)
            if (candidate == null) return null
            placeRect(candidate.rect!!)
            return candidate.rect
        }

        fun placeRect(usedRect: Rect) {
            var i = 0
            while (i < freeRectangles.size) {
                val freeRect = freeRectangles.get(i)
                if (!splitFreeRect(freeRect, usedRect)) {
                    i++
                    continue
                }

                freeRectangles.removeAt(i)
                i--
                i++
            }
            pruneFreeList()
        }

        fun splitFreeRect(freeRect: Rect, usedRect: Rect): Boolean {
            if (!intersects(freeRect, usedRect)) return false

            if (usedRect.x < freeRect.x + freeRect.width
                && usedRect.x + usedRect.width > freeRect.x
            ) {
                if (usedRect.y > freeRect.y && usedRect.y < freeRect.y + freeRect.height) {
                    freeRectangles.add(
                        Rect(
                            freeRect.x,
                            freeRect.y,
                            freeRect.width,
                            usedRect.y - freeRect.y
                        )
                    )
                }

                val usedBottom = usedRect.y + usedRect.height
                val freeBottom = freeRect.y + freeRect.height
                if (usedBottom < freeBottom) {
                    freeRectangles.add(
                        Rect(
                            freeRect.x,
                            usedBottom,
                            freeRect.width,
                            freeBottom - usedBottom
                        )
                    )
                }
            }

            if (usedRect.y < freeRect.y + freeRect.height
                && usedRect.y + usedRect.height > freeRect.y
            ) {
                if (usedRect.x > freeRect.x && usedRect.x < freeRect.x + freeRect.width) {
                    freeRectangles.add(
                        Rect(
                            freeRect.x,
                            freeRect.y,
                            usedRect.x - freeRect.x,
                            freeRect.height
                        )
                    )
                }

                val usedRight = usedRect.x + usedRect.width
                val freeRight = freeRect.x + freeRect.width
                if (usedRight < freeRight) {
                    freeRectangles.add(
                        Rect(
                            usedRight,
                            freeRect.y,
                            freeRight - usedRight,
                            freeRect.height
                        )
                    )
                }
            }

            return true
        }

        fun pruneFreeList() {
            var i = 0
            while (i < freeRectangles.size) {
                val current = freeRectangles.get(i)
                var removedCurrent = false

                var j = i + 1
                while (j < freeRectangles.size) {
                    val other = freeRectangles.get(j)
                    if (current.contains(other)) {
                        freeRectangles.removeAt(j)
                        j--
                    } else if (other.contains(current)) {
                        freeRectangles.removeAt(i)
                        i--
                        removedCurrent = true
                        break
                    }
                    j++
                }

                if (removedCurrent) {
                    i++
                    continue
                }
                i++
            }
        }

        fun intersects(first: Rect, second: Rect): Boolean {
            return first.x < second.x + second.width && first.x + first.width > second.x && first.y < second.y + second.height && first.y + first.height > second.y
        }
    }

    private object ImGuiCondOnce {
        val VALUE = 1 shl 1
    }

    companion object {
        private const val ATLAS_SIZE = 1024
        private const val MIN_WINDOW_WIDTH = 1180
        private const val MIN_WINDOW_HEIGHT = 760
        private const val GRID_STEP = 128
        private val CLEAR_COLOR = Vector4f(0.08f, 0.09f, 0.10f, 1.0f)

        @JvmStatic
        fun main(args: Array<String>) {
            DialogueUtils.init()
            AtlasBuilderGui().run()
        }

        private fun colorForName(name: String): Int {
            val hash = abs(name.hashCode())
            val r = 90 + (hash and 0x7F)
            val g = 90 + ((hash shr 8) and 0x7F)
            val b = 90 + ((hash shr 16) and 0x7F)
            return ImColor.rgba(r, g, b, 255)
        }

        private fun isAtlasFileName(name: String): Boolean {
            val lower = name.lowercase()
            return lower.matches("atlas_\\d+\\.png".toRegex())
        }

        private fun clamp(value: Int, min: Int, max: Int): Int {
            if (max < min) return min
            return max(min, min(max, value))
        }
    }
}
