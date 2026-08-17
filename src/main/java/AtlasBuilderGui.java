import com.google.gson.JsonObject;
import gg.generations.rarecandy.pokeutils.IModelConfig;
import gg.generations.rarecandy.pokeutils.resource.PkResourceLocator;
import gg.generations.rarecandy.pokeutils.resource.ResourceLocator;
import gg.generations.rarecandy.renderer.launch.OpenGL;
import gg.generations.rarecandy.renderer.textures.ITexture;
import gg.generations.rarecandy.renderer.textures.Texture;
import gg.generations.rarecandy.tools.AppBase;
import gg.generations.rarecandy.tools.gui.AdvancedMenuBar;
import gg.generations.rarecandy.tools.gui.DialogueUtils;
import imgui.ImColor;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiMouseCursor;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AtlasBuilderGui extends AppBase {
    private static final int ATLAS_SIZE = 1024;
    private static final int MIN_WINDOW_WIDTH = 1180;
    private static final int MIN_WINDOW_HEIGHT = 760;
    private static final int GRID_STEP = 128;
    private static final Vector4f CLEAR_COLOR = new Vector4f(0.08f, 0.09f, 0.10f, 1.0f);

    private final AdvancedMenuBar menu;
    private final List<TextureEntry> textures = new ArrayList<>();
    private final List<TexturePlacement> placements = new ArrayList<>();
    private final List<String> skippedImages = new ArrayList<>();
    private final ImInt snapPixels = new ImInt(1);

    private ResourceLocator locator;
    private Path assetPath;
    private JsonObject baseConfig;
    private JsonObject currentConfig;
    private int pageCount;
    private int selectedPage;
    private TexturePlacement selected;
    private TexturePlacement dragging;
    private int dragStartX;
    private int dragStartY;
    private float dragStartMouseX;
    private float dragStartMouseY;
    private float zoom = 0.75f;
    private boolean deleteSourceImages = true;
    private boolean dirty;
    private String status = "Open a PK or folder to begin.";

    public AtlasBuilderGui() {
        super("Atlas Builder", MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT, new OpenGL());
        this.menu = configureMenu();
    }

    public static void main(String[] args) {
        DialogueUtils.init();
        new AtlasBuilderGui().run();
    }

    @Override
    protected void initWindow() {
        super.initWindow();
        GLFW.glfwSetWindowCloseCallback(window, ignored -> GLFW.glfwSetWindowShouldClose(window, true));
    }

    @Override
    protected void initGL() {
    }

    @Override
    protected void renderGui() {
        menu.render();
        renderAtlasWindow();
        renderTextureWindow();
        renderDetailsWindow();
    }

    @Override
    protected void render() {
    }

    @Override
    public Vector4f clearColor() {
        return CLEAR_COLOR;
    }

    @Override
    protected void cleanupGL() {
        closePreviewTextures();
        DialogueUtils.quit();
    }

    private AdvancedMenuBar configureMenu() {
        var toolbar = new AdvancedMenuBar();
        var file = toolbar.addMenu("File");
        file.addItem("Open PK (*.pk)", () -> DialogueUtils.chooseFile(defaultPath(), "PK;pk", this::open));
        file.addItem("Open Folder", () -> DialogueUtils.chooseFolder(defaultPath(), this::open));
        file.addItem("Save", () -> addRunnable(this::saveInPlace));
        file.addItem("Save As PK (*.pk)", () -> DialogueUtils.saveFile(defaultPath(), "PK;pk", path -> addRunnable(() -> saveAs(path))));

        var layout = toolbar.addMenu("Layout");
        layout.addItem("Auto Pack", () -> addRunnable(this::autoPack));
        layout.addItem("Add Page", () -> addRunnable(this::addPage));
        layout.addItem("Remove Empty Page", () -> addRunnable(this::removeCurrentPageIfEmpty));
        return toolbar;
    }

    private String defaultPath() {
        if (assetPath == null) return "";
        Path parent = Files.isDirectory(assetPath) ? assetPath : assetPath.getParent();
        return parent != null ? parent.toString() : assetPath.toString();
    }

    private void open(Path path) {
        if (path == null) return;
        addRunnable(() -> openNow(path));
    }

    private void openNow(Path path) {
        try {
            closePreviewTextures();
            textures.clear();
            placements.clear();
            skippedImages.clear();
            selected = null;
            dragging = null;
            pageCount = 0;
            selectedPage = 0;

            locator = ResourceLocator.of(path);
            assetPath = path;
            baseConfig = readConfig(locator);
            currentConfig = baseConfig != null ? baseConfig.deepCopy() : null;

            loadTextures(locator);
            autoPack();
            dirty = false;
            status = "Loaded " + textures.size() + " texture(s) from " + path.getFileName() + ".";
            setWindowTitle();
        } catch (Exception exception) {
            status = "Open failed: " + exception.getMessage();
            exception.printStackTrace();
        }
    }

    private JsonObject readConfig(ResourceLocator source) throws IOException {
        if (!source.hasFile("config.json")) return null;

        try (var reader = new InputStreamReader(source.getInputStream("config.json"), StandardCharsets.UTF_8)) {
            return IModelConfig.GSON.fromJson(reader, JsonObject.class);
        }
    }

    private void loadTextures(ResourceLocator source) throws IOException {
        var names = new ArrayList<>(source.getFileNames());
        names.sort(String::compareToIgnoreCase);

        for (String name : names) {
            if (name == null || name.isBlank()) continue;

            byte[] bytes = source.getFile(name);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) continue;

            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            int packWidth = imageWidth;
            int packHeight = imageHeight;

            if (imageWidth == ATLAS_SIZE && imageHeight == ATLAS_SIZE && isAtlasFileName(name)) {
                skippedImages.add(name + " - existing atlas page");
                continue;
            }

            if (imageWidth > ATLAS_SIZE || imageHeight > ATLAS_SIZE) {
                if (imageWidth == 2048 && imageHeight == 1024) {
                    packWidth = 1024;
                    packHeight = 512;
                } else if (imageWidth == 2048 && imageHeight == 2048) {
                    packWidth = 1024;
                    packHeight = 1024;
                } else {
                    skippedImages.add(name + " - too large (" + imageWidth + "x" + imageHeight + ")");
                    continue;
                }
            }

            ITexture previewTexture = null;
            try {
                previewTexture = Texture.read(bytes, name);
                if (previewTexture != null) previewTexture.id();
            } catch (Exception ignored) {
            }

            textures.add(new TextureEntry(
                    name,
                    image,
                    imageWidth,
                    imageHeight,
                    packWidth,
                    packHeight,
                    previewTexture,
                    colorForName(name)
            ));
        }
    }

    private void autoPack() {
        if (textures.isEmpty()) {
            placements.clear();
            pageCount = 0;
            selectedPage = 0;
            selected = null;
            status = locator == null ? "Open a PK or folder first." : "No packable textures found.";
            return;
        }

        placements.clear();
        selected = null;
        selectedPage = 0;

        List<PackingPage> pages = new ArrayList<>();
        var sorted = new ArrayList<>(textures);
        sorted.sort(
                Comparator.comparingInt(TextureEntry::area).reversed()
                        .thenComparing(Comparator.comparingInt(TextureEntry::maxDimension).reversed())
                        .thenComparing(TextureEntry::name)
        );

        for (TextureEntry texture : sorted) {
            PackingCandidate best = null;

            for (PackingPage page : pages) {
                PackingCandidate candidate = page.preview(texture.packWidth(), texture.packHeight());
                if (candidate != null && (best == null || candidate.isBetterThan(best))) {
                    best = candidate;
                }
            }

            if (best == null) {
                var page = new PackingPage(pages.size());
                pages.add(page);
                best = page.preview(texture.packWidth(), texture.packHeight());
            }

            if (best == null) {
                skippedImages.add(texture.name() + " - could not fit in a new atlas page");
                continue;
            }

            Rect rect = pages.get(best.pageIndex()).insert(texture.packWidth(), texture.packHeight());
            placements.add(new TexturePlacement(texture, best.pageIndex(), rect.x(), rect.y(), rect.width(), rect.height()));
        }

        pageCount = pages.size();
        selected = placements.isEmpty() ? null : placements.get(0);
        if (selected != null) selectedPage = selected.pageIndex;
        if (locator != null) markDirty();
        status = "Auto-packed " + placements.size() + " texture(s) into " + pageCount + " atlas page(s).";
    }

    private void addPage() {
        if (locator == null) {
            status = "Open a PK or folder first.";
            return;
        }

        pageCount++;
        selectedPage = Math.max(0, pageCount - 1);
        markDirty();
        status = "Added atlas page " + selectedPage + ".";
    }

    private void removeCurrentPageIfEmpty() {
        if (pageCount <= 0) return;

        for (TexturePlacement placement : placements) {
            if (placement.pageIndex == selectedPage) {
                status = "Page " + selectedPage + " is not empty.";
                return;
            }
        }

        int removed = selectedPage;
        pageCount--;
        for (TexturePlacement placement : placements) {
            if (placement.pageIndex > removed) placement.pageIndex--;
        }
        selectedPage = clamp(selectedPage, 0, Math.max(0, pageCount - 1));
        markDirty();
        status = "Removed empty atlas page " + removed + ".";
    }

    private void renderAtlasWindow() {
        ImGui.setNextWindowSize(820, 720, ImGuiCondOnce.VALUE);
        ImGui.begin("Atlas");

        if (locator == null) {
            ImGui.textWrapped(status);
            ImGui.end();
            return;
        }

        renderAtlasControls();

        if (pageCount <= 0) {
            ImGui.textWrapped("No atlas pages to show.");
            ImGui.end();
            return;
        }

        float canvasSize = ATLAS_SIZE * zoom;
        ImGui.beginChild("AtlasCanvasScroll", canvasSize + 24f, canvasSize + 24f, true, ImGuiWindowFlags.HorizontalScrollbar);
        drawAtlasCanvas(canvasSize);
        ImGui.endChild();

        ImGui.end();
    }

    private void renderAtlasControls() {
        if (ImGui.button("Auto Pack")) {
            autoPack();
        }
        ImGui.sameLine();
        if (ImGui.button("Add Page")) {
            addPage();
        }
        ImGui.sameLine();
        if (ImGui.button("Remove Empty Page")) {
            removeCurrentPageIfEmpty();
        }

        float[] zoomValue = { zoom };
        if (ImGui.sliderFloat("Zoom", zoomValue, 0.25f, 1.5f, "%.2fx")) {
            zoom = zoomValue[0];
        }

        if (ImGui.inputInt("Snap Pixels", snapPixels, 1, 16)) {
            snapPixels.set(Math.max(1, snapPixels.get()));
        }

        if (ImGui.checkbox("Delete source images on save", deleteSourceImages)) {
            deleteSourceImages = !deleteSourceImages;
        }

        if (pageCount > 0) {
            ImGui.text("Page " + (selectedPage + 1) + " / " + pageCount + " - " + pageUsagePercent(selectedPage) + "% used");
            if (ImGui.button("<")) {
                selectedPage = clamp(selectedPage - 1, 0, pageCount - 1);
            }
            ImGui.sameLine();
            if (ImGui.button(">")) {
                selectedPage = clamp(selectedPage + 1, 0, pageCount - 1);
            }
            ImGui.sameLine();
            for (int page = 0; page < pageCount; page++) {
                if (page > 0) ImGui.sameLine();
                if (ImGui.radioButton(String.valueOf(page + 1), selectedPage == page)) {
                    selectedPage = page;
                }
            }
        }

        if (hasOverlaps()) {
            ImGui.textColored(1f, 0.35f, 0.25f, 1f, "Resolve overlapping placements before saving.");
        }

        ImGui.separator();
    }

    private void drawAtlasCanvas(float canvasSize) {
        ImVec2 canvasMin = ImGui.getCursorScreenPos();
        float canvasMaxX = canvasMin.x + canvasSize;
        float canvasMaxY = canvasMin.y + canvasSize;
        float scale = canvasSize / ATLAS_SIZE;
        var drawList = ImGui.getWindowDrawList();

        drawList.addRectFilled(canvasMin.x, canvasMin.y, canvasMaxX, canvasMaxY, ImGui.getColorU32(ImGuiCol.FrameBg));
        drawCheckerboard(drawList, canvasMin, canvasSize);
        drawGrid(drawList, canvasMin, canvasSize);

        for (TexturePlacement placement : placementsForPage(selectedPage)) {
            drawPlacement(drawList, placement, canvasMin, scale);
        }

        ImGui.setCursorScreenPos(canvasMin.x, canvasMin.y);
        ImGui.invisibleButton("##atlasCanvas", canvasSize, canvasSize);
        handleCanvasInput(canvasMin, scale);
    }

    private void drawCheckerboard(imgui.ImDrawList drawList, ImVec2 canvasMin, float canvasSize) {
        int dark = ImColor.rgba(42, 45, 48, 255);
        int light = ImColor.rgba(52, 55, 58, 255);
        float tile = 32f * zoom;
        for (float y = 0; y < canvasSize; y += tile) {
            for (float x = 0; x < canvasSize; x += tile) {
                int color = (((int) (x / tile) + (int) (y / tile)) & 1) == 0 ? dark : light;
                drawList.addRectFilled(
                        canvasMin.x + x,
                        canvasMin.y + y,
                        canvasMin.x + Math.min(canvasSize, x + tile),
                        canvasMin.y + Math.min(canvasSize, y + tile),
                        color
                );
            }
        }
    }

    private void drawGrid(imgui.ImDrawList drawList, ImVec2 canvasMin, float canvasSize) {
        int color = ImColor.rgba(255, 255, 255, 26);
        for (int pixel = 0; pixel <= ATLAS_SIZE; pixel += GRID_STEP) {
            float pos = canvasMin.x + pixel * zoom;
            drawList.addLine(pos, canvasMin.y, pos, canvasMin.y + canvasSize, color);
            pos = canvasMin.y + pixel * zoom;
            drawList.addLine(canvasMin.x, pos, canvasMin.x + canvasSize, pos, color);
        }
    }

    private void drawPlacement(imgui.ImDrawList drawList, TexturePlacement placement, ImVec2 canvasMin, float scale) {
        float x0 = canvasMin.x + placement.x * scale;
        float y0 = canvasMin.y + placement.y * scale;
        float x1 = x0 + placement.width * scale;
        float y1 = y0 + placement.height * scale;
        boolean selectedPlacement = placement == selected;
        boolean overlapping = overlapsAny(placement);
        int border = selectedPlacement
                ? ImColor.rgba(255, 207, 84, 255)
                : overlapping ? ImColor.rgba(255, 84, 72, 255) : ImColor.rgba(120, 190, 255, 230);

        if (placement.texture.previewTexture() != null) {
            drawList.addImage(placement.texture.previewTexture().id(), x0, y0, x1, y1, 0f, 0f, 1f, 1f);
        } else {
            drawList.addRectFilled(x0, y0, x1, y1, placement.texture.color());
        }

        drawList.addRectFilled(x0, y0, x1, Math.min(y1, y0 + 22f), ImColor.rgba(0, 0, 0, 135));
        drawList.pushClipRect(x0 + 3f, y0 + 2f, x1 - 3f, Math.min(y1, y0 + 22f), true);
        drawList.addText(x0 + 4f, y0 + 3f, ImColor.rgba(245, 247, 250, 255), placement.texture.name());
        drawList.popClipRect();

        drawList.addRect(x0, y0, x1, y1, border, 0f, 0, selectedPlacement ? 3f : 2f);
    }

    private void handleCanvasInput(ImVec2 canvasMin, float scale) {
        boolean hovered = ImGui.isItemHovered();
        ImGuiIO io = ImGui.getIO();
        float mouseX = io.getMousePosX();
        float mouseY = io.getMousePosY();

        TexturePlacement hoveredPlacement = hovered ? placementAt(mouseX, mouseY, canvasMin, scale) : null;
        if (hoveredPlacement != null) {
            ImGui.setMouseCursor(ImGuiMouseCursor.ResizeAll);
        }

        if (hovered && ImGui.isMouseClicked(ImGuiMouseButton.Left)) {
            selected = hoveredPlacement;
            dragging = hoveredPlacement;
            if (dragging != null) {
                dragStartX = dragging.x;
                dragStartY = dragging.y;
                dragStartMouseX = mouseX;
                dragStartMouseY = mouseY;
            }
        }

        if (dragging != null) {
            if (!ImGui.isMouseDown(ImGuiMouseButton.Left)) {
                dragging = null;
                return;
            }

            int newX = dragStartX + Math.round((mouseX - dragStartMouseX) / scale);
            int newY = dragStartY + Math.round((mouseY - dragStartMouseY) / scale);
            movePlacement(dragging, newX, newY);
        }
    }

    private TexturePlacement placementAt(float mouseX, float mouseY, ImVec2 canvasMin, float scale) {
        var pagePlacements = placementsForPage(selectedPage);
        for (int i = pagePlacements.size() - 1; i >= 0; i--) {
            TexturePlacement placement = pagePlacements.get(i);
            float x0 = canvasMin.x + placement.x * scale;
            float y0 = canvasMin.y + placement.y * scale;
            float x1 = x0 + placement.width * scale;
            float y1 = y0 + placement.height * scale;
            if (mouseX >= x0 && mouseX <= x1 && mouseY >= y0 && mouseY <= y1) {
                return placement;
            }
        }
        return null;
    }

    private void movePlacement(TexturePlacement placement, int x, int y) {
        int snap = Math.max(1, snapPixels.get());
        x = Math.round(x / (float) snap) * snap;
        y = Math.round(y / (float) snap) * snap;
        int clampedX = clamp(x, 0, ATLAS_SIZE - placement.width);
        int clampedY = clamp(y, 0, ATLAS_SIZE - placement.height);
        if (placement.x != clampedX || placement.y != clampedY) {
            placement.x = clampedX;
            placement.y = clampedY;
            markDirty();
        }
    }

    private void renderTextureWindow() {
        ImGui.setNextWindowSize(340, 720, ImGuiCondOnce.VALUE);
        ImGui.begin("Textures");

        if (locator == null) {
            ImGui.textWrapped("No asset loaded.");
            ImGui.end();
            return;
        }

        ImGui.text(textures.size() + " packable texture(s)");
        if (!skippedImages.isEmpty() && ImGui.treeNode("Skipped (" + skippedImages.size() + ")")) {
            for (String skipped : skippedImages) {
                ImGui.textWrapped(skipped);
            }
            ImGui.treePop();
        }

        ImGui.separator();
        ImGui.beginChild("TextureList", 0f, 360f, true);
        for (TexturePlacement placement : placements) {
            ImGui.pushID(placement.texture.name());
            String label = placement.texture.name()
                    + "  [" + placement.texture.packWidth() + "x" + placement.texture.packHeight() + "]"
                    + "  p" + (placement.pageIndex + 1);
            if (ImGui.selectable(label, placement == selected)) {
                selected = placement;
                selectedPage = placement.pageIndex;
            }
            ImGui.popID();
        }
        ImGui.endChild();

        if (selected != null) {
            ImGui.separator();
            ImGui.textWrapped(selected.texture.name());
            drawPreview(selected.texture);
        }

        ImGui.end();
    }

    private void drawPreview(TextureEntry texture) {
        if (texture.previewTexture() == null) {
            ImGui.text("Preview unavailable");
            return;
        }

        float max = 256f;
        float width = texture.sourceWidth();
        float height = texture.sourceHeight();
        float scale = Math.min(max / width, max / height);
        ImGui.image(texture.previewTexture().id(), width * scale, height * scale);
    }

    private void renderDetailsWindow() {
        ImGui.setNextWindowSize(340, 250, ImGuiCondOnce.VALUE);
        ImGui.begin("Atlas Details");

        ImGui.textWrapped(status);
        if (assetPath != null) {
            ImGui.textWrapped("Asset: " + assetPath);
        }
        if (dirty) {
            ImGui.textColored(1f, 0.82f, 0.35f, 1f, "Unsaved layout changes");
        }

        ImGui.separator();
        if (selected == null) {
            ImGui.textWrapped("Select a texture placement to edit its atlas coordinates.");
        } else {
            renderSelectedPlacementControls();
        }

        ImGui.separator();
        if (ImGui.button("Save")) {
            saveInPlace();
        }
        ImGui.sameLine();
        if (ImGui.button("Save As PK")) {
            DialogueUtils.saveFile(defaultPath(), "PK;pk", path -> addRunnable(() -> saveAs(path)));
        }

        ImGui.end();
    }

    private void renderSelectedPlacementControls() {
        ImGui.text("Selected");
        ImGui.textWrapped(selected.texture.name());

        int[] position = { selected.x, selected.y };
        if (ImGui.inputInt2("Position", position)) {
            movePlacement(selected, position[0], position[1]);
        }

        int[] size = { selected.width, selected.height };
        if (ImGui.inputInt2("Size", size)) {
            int newWidth = clamp(size[0], 1, ATLAS_SIZE);
            int newHeight = clamp(size[1], 1, ATLAS_SIZE);
            if (newWidth != selected.width || newHeight != selected.height) {
                selected.width = newWidth;
                selected.height = newHeight;
                movePlacement(selected, selected.x, selected.y);
                markDirty();
            }
        }

        ImInt page = new ImInt(selected.pageIndex + 1);
        if (ImGui.inputInt("Page", page, 1, 1)) {
            int targetPage = clamp(page.get() - 1, 0, Math.max(0, pageCount - 1));
            if (targetPage != selected.pageIndex) {
                selected.pageIndex = targetPage;
                selectedPage = targetPage;
                movePlacement(selected, selected.x, selected.y);
                markDirty();
            }
        }

        if (ImGui.button("Native Size")) {
            selected.width = selected.texture.packWidth();
            selected.height = selected.texture.packHeight();
            movePlacement(selected, selected.x, selected.y);
            markDirty();
        }
    }

    private void saveInPlace() {
        if (locator == null || assetPath == null) {
            status = "Nothing to save.";
            return;
        }

        try {
            writeAtlas(locator);
            locator.save();
            dirty = false;
            status = "Saved " + assetPath.getFileName() + ".";
            setWindowTitle();
        } catch (Exception exception) {
            status = "Save failed: " + exception.getMessage();
            exception.printStackTrace();
        }
    }

    private void saveAs(Path path) {
        if (path == null || locator == null) return;

        try {
            Path target = normalizePkPath(path);
            ResourceLocator output = createOutputLocator(target);
            copyLocator(locator, output);
            writeAtlas(output);
            output.save(target);
            locator = output;
            assetPath = target;
            dirty = false;
            status = "Saved " + target.getFileName() + ".";
            setWindowTitle();
        } catch (Exception exception) {
            status = "Save As failed: " + exception.getMessage();
            exception.printStackTrace();
        }
    }

    private Path normalizePkPath(Path path) {
        String fileName = path.getFileName() != null ? path.getFileName().toString() : "";
        if (!fileName.contains(".")) {
            return path.resolveSibling(fileName + ".pk");
        }
        return path;
    }

    private ResourceLocator createOutputLocator(Path target) throws IOException {
        String fileName = target.getFileName().toString().toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".pk")) return new PkResourceLocator();
        return ResourceLocator.of(target);
    }

    private void copyLocator(ResourceLocator source, ResourceLocator output) throws IOException {
        for (String file : source.getFileNames()) {
            output.putFile(file, source.getFile(file));
        }
    }

    private void writeAtlas(ResourceLocator target) throws IOException {
        if (placements.isEmpty()) {
            throw new IllegalStateException("No atlas placements to save.");
        }
        if (hasOverlaps()) {
            throw new IllegalStateException("Atlas placements overlap.");
        }

        for (int page = 0; page < pageCount; page++) {
            BufferedImage atlasImage = new BufferedImage(ATLAS_SIZE, ATLAS_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = atlasImage.createGraphics();
            graphics.setComposite(AlphaComposite.Src);

            for (TexturePlacement placement : placementsForPage(page)) {
                graphics.drawImage(
                        placement.texture.image(),
                        placement.x,
                        placement.y,
                        placement.width,
                        placement.height,
                        null
                );
            }

            graphics.dispose();
            var outputStream = new ByteArrayOutputStream();
            ImageIO.write(atlasImage, "PNG", outputStream);
            target.putFile(atlasFileName(page), outputStream.toByteArray());
        }

        var atlasBuild = new AtlasCompacter.AtlasBuild(List.of(), packedTextures());
        if (baseConfig != null) {
            currentConfig = baseConfig.deepCopy();
            var transforms = MaterialCompressor.applyAtlasTextures(currentConfig, atlasBuild);
            MaterialCompressor.applyAtlasTransformsToDefaultVariant(currentConfig, transforms);
            MaterialCompressor.applyAtlasTransformsToAllVariants(currentConfig, transforms);
            MaterialCompressor.removeDuplicateMaterialsAfterAtlas(currentConfig, atlasBuild);
            target.putFile("config.json", IModelConfig.GSON.toJson(currentConfig).getBytes(StandardCharsets.UTF_8));
        }

        if (deleteSourceImages) {
            MaterialCompressor.deleteCompactedImages(target, atlasBuild);
        }
    }

    private Map<String, AtlasCompacter.PackedTexture> packedTextures() {
        Map<String, AtlasCompacter.PackedTexture> packed = new LinkedHashMap<>();
        for (TexturePlacement placement : placements) {
            packed.put(
                    placement.texture.name(),
                    new AtlasCompacter.PackedTexture(
                            placement.texture.name(),
                            atlasFileName(placement.pageIndex),
                            placement.x,
                            placement.y,
                            placement.width,
                            placement.height
                    )
            );
        }
        return Map.copyOf(packed);
    }

    private String atlasFileName(int page) {
        return "atlas_" + String.format(Locale.ROOT, "%02d", page) + ".png";
    }

    private List<TexturePlacement> placementsForPage(int page) {
        List<TexturePlacement> result = new ArrayList<>();
        for (TexturePlacement placement : placements) {
            if (placement.pageIndex == page) result.add(placement);
        }
        return result;
    }

    private boolean hasOverlaps() {
        for (int i = 0; i < placements.size(); i++) {
            for (int j = i + 1; j < placements.size(); j++) {
                TexturePlacement first = placements.get(i);
                TexturePlacement second = placements.get(j);
                if (first.pageIndex == second.pageIndex && overlaps(first, second)) return true;
            }
        }
        return false;
    }

    private boolean overlapsAny(TexturePlacement placement) {
        for (TexturePlacement other : placements) {
            if (placement != other && placement.pageIndex == other.pageIndex && overlaps(placement, other)) {
                return true;
            }
        }
        return false;
    }

    private boolean overlaps(TexturePlacement first, TexturePlacement second) {
        return first.x < second.x + second.width
                && first.x + first.width > second.x
                && first.y < second.y + second.height
                && first.y + first.height > second.y;
    }

    private int pageUsagePercent(int page) {
        int used = 0;
        for (TexturePlacement placement : placements) {
            if (placement.pageIndex == page) used += placement.width * placement.height;
        }
        return Math.round(used * 100f / (ATLAS_SIZE * ATLAS_SIZE));
    }

    private void setWindowTitle() {
        String name = assetPath != null ? assetPath.getFileName().toString() : "Atlas Builder";
        GLFW.glfwSetWindowTitle(window, "Atlas Builder - " + name + (dirty ? "*" : ""));
    }

    private void markDirty() {
        if (!dirty) {
            dirty = true;
            setWindowTitle();
        }
    }

    private void closePreviewTextures() {
        for (TextureEntry texture : textures) {
            if (texture.previewTexture() == null) continue;
            try {
                texture.previewTexture().close();
            } catch (IOException ignored) {
            }
        }
    }

    private static int colorForName(String name) {
        int hash = Math.abs(name.hashCode());
        int r = 90 + (hash & 0x7F);
        int g = 90 + ((hash >> 8) & 0x7F);
        int b = 90 + ((hash >> 16) & 0x7F);
        return ImColor.rgba(r, g, b, 255);
    }

    private static boolean isAtlasFileName(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.matches("atlas_\\d+\\.png");
    }

    private static int clamp(int value, int min, int max) {
        if (max < min) return min;
        return Math.max(min, Math.min(max, value));
    }

    private record TextureEntry(
            String name,
            BufferedImage image,
            int sourceWidth,
            int sourceHeight,
            int packWidth,
            int packHeight,
            ITexture previewTexture,
            int color
    ) {
        int area() {
            return packWidth * packHeight;
        }

        int maxDimension() {
            return Math.max(packWidth, packHeight);
        }
    }

    private static final class TexturePlacement {
        private final TextureEntry texture;
        private int pageIndex;
        private int x;
        private int y;
        private int width;
        private int height;

        private TexturePlacement(TextureEntry texture, int pageIndex, int x, int y, int width, int height) {
            this.texture = texture;
            this.pageIndex = pageIndex;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private record Rect(int x, int y, int width, int height) {
        private boolean contains(Rect other) {
            return other.x >= x
                    && other.y >= y
                    && other.x + other.width <= x + width
                    && other.y + other.height <= y + height;
        }
    }

    private record PackingCandidate(int pageIndex, Rect rect, int shortSideFit, int longSideFit) {
        private boolean isBetterThan(PackingCandidate other) {
            if (shortSideFit != other.shortSideFit) return shortSideFit < other.shortSideFit;
            if (longSideFit != other.longSideFit) return longSideFit < other.longSideFit;
            if (rect.y() != other.rect.y()) return rect.y() < other.rect.y();
            if (rect.x() != other.rect.x()) return rect.x() < other.rect.x();
            return pageIndex < other.pageIndex;
        }
    }

    private static final class PackingPage {
        private final int index;
        private final List<Rect> freeRectangles = new ArrayList<>();

        private PackingPage(int index) {
            this.index = index;
            freeRectangles.add(new Rect(0, 0, ATLAS_SIZE, ATLAS_SIZE));
        }

        private PackingCandidate preview(int width, int height) {
            PackingCandidate best = null;
            for (Rect freeRectangle : freeRectangles) {
                if (freeRectangle.width() < width || freeRectangle.height() < height) continue;

                int leftoverHoriz = freeRectangle.width() - width;
                int leftoverVert = freeRectangle.height() - height;
                var rect = new Rect(freeRectangle.x(), freeRectangle.y(), width, height);
                var candidate = new PackingCandidate(
                        index,
                        rect,
                        Math.min(leftoverHoriz, leftoverVert),
                        Math.max(leftoverHoriz, leftoverVert)
                );
                if (best == null || candidate.isBetterThan(best)) best = candidate;
            }
            return best;
        }

        private Rect insert(int width, int height) {
            PackingCandidate candidate = preview(width, height);
            if (candidate == null) return null;
            placeRect(candidate.rect());
            return candidate.rect();
        }

        private void placeRect(Rect usedRect) {
            for (int i = 0; i < freeRectangles.size(); i++) {
                Rect freeRect = freeRectangles.get(i);
                if (!splitFreeRect(freeRect, usedRect)) continue;

                freeRectangles.remove(i);
                i--;
            }
            pruneFreeList();
        }

        private boolean splitFreeRect(Rect freeRect, Rect usedRect) {
            if (!intersects(freeRect, usedRect)) return false;

            if (usedRect.x() < freeRect.x() + freeRect.width()
                    && usedRect.x() + usedRect.width() > freeRect.x()) {
                if (usedRect.y() > freeRect.y() && usedRect.y() < freeRect.y() + freeRect.height()) {
                    freeRectangles.add(new Rect(
                            freeRect.x(),
                            freeRect.y(),
                            freeRect.width(),
                            usedRect.y() - freeRect.y()
                    ));
                }

                int usedBottom = usedRect.y() + usedRect.height();
                int freeBottom = freeRect.y() + freeRect.height();
                if (usedBottom < freeBottom) {
                    freeRectangles.add(new Rect(
                            freeRect.x(),
                            usedBottom,
                            freeRect.width(),
                            freeBottom - usedBottom
                    ));
                }
            }

            if (usedRect.y() < freeRect.y() + freeRect.height()
                    && usedRect.y() + usedRect.height() > freeRect.y()) {
                if (usedRect.x() > freeRect.x() && usedRect.x() < freeRect.x() + freeRect.width()) {
                    freeRectangles.add(new Rect(
                            freeRect.x(),
                            freeRect.y(),
                            usedRect.x() - freeRect.x(),
                            freeRect.height()
                    ));
                }

                int usedRight = usedRect.x() + usedRect.width();
                int freeRight = freeRect.x() + freeRect.width();
                if (usedRight < freeRight) {
                    freeRectangles.add(new Rect(
                            usedRight,
                            freeRect.y(),
                            freeRight - usedRight,
                            freeRect.height()
                    ));
                }
            }

            return true;
        }

        private void pruneFreeList() {
            for (int i = 0; i < freeRectangles.size(); i++) {
                Rect current = freeRectangles.get(i);
                boolean removedCurrent = false;

                for (int j = i + 1; j < freeRectangles.size(); j++) {
                    Rect other = freeRectangles.get(j);
                    if (current.contains(other)) {
                        freeRectangles.remove(j);
                        j--;
                    } else if (other.contains(current)) {
                        freeRectangles.remove(i);
                        i--;
                        removedCurrent = true;
                        break;
                    }
                }

                if (removedCurrent) continue;
            }
        }

        private boolean intersects(Rect first, Rect second) {
            return first.x() < second.x() + second.width()
                    && first.x() + first.width() > second.x()
                    && first.y() < second.y() + second.height()
                    && first.y() + first.height() > second.y();
        }
    }

    private static final class ImGuiCondOnce {
        private static final int VALUE = 1 << 1;
    }
}
