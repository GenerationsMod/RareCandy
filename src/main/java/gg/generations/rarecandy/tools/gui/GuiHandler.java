package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.PixelAsset;
import gg.generations.rarecandy.tools.pkcreator.PixelmonArchiveBuilder;
import imgui.ImGui;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.tukaani.xz.LZMA2Options;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static gg.generations.rarecandy.tools.gui.RareCandyCanvas.lightLevel;

public class GuiHandler implements KeyListener {
    public static final Path TEMP = Path.of("temp");
    public static final LZMA2Options OPTIONS = new LZMA2Options();
    public static final String BASE_TITLE = "Pk Explorer";
    private final PokeUtilsGui gui;

    private final Set<Integer> pressedKeys = new HashSet<>();
    private final ArcballOrbit arcBall;

    public PixelAsset asset;
    public Path assetPath;
    public int index = 0;
    public int amount;
    private boolean dirty = false;
    public List<Path> filesToOpen = new ArrayList<>();

    public GuiHandler(PokeUtilsGui gui) {
        this.gui = gui;
        arcBall = new ArcballOrbit(getCanvas(), 3f, 0.125f, 0f);
    }

    public void attach(long window) {
        MouseMotionListener.attach(window, arcBall);
        MouseWheelListener.attach(window, arcBall);
        MouseListener.attach(window, arcBall);

        KeyListener.attach(window, this);
    }

    public RareCandyCanvas getCanvas() {
        return gui.canvas;
    }

    public void initializeAsset(PixelAsset asset, Path path) {
        this.asset = asset;
        this.assetPath = path;
    }

    public void save() {
        save(assetPath);
    }

    public void save(Path savePath) {
        try {
            PixelmonArchiveBuilder.convertToPk(TEMP, Files.walk(TEMP).toList(), savePath, getCanvas().scaleModifier);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        if (dirty) {
//
//
//
//            dirty = false;
//        }
//
//        LoggerUtil.print("Saving disabled for now.");
    }

    public void markDirty() {
        if (!dirty) {
            this.dirty = true;

            gui.setTitle(gui.title + "*");
        }
    }

    public void openAsset(Path filePath) {
        try {

            initializeAsset(new PixelAsset(move(filePath), filePath.getFileName().toString()), filePath);
            var title = BASE_TITLE + " - " + filePath.getFileName().toString();
            gui.setTitle(title);
            getCanvas().openFile(asset, FilenameUtils.getBaseName(filePath.getFileName().toString()), () -> gui.fileViewer.initializeAsset(asset, assetPath, getCanvas().loadedModel.animationNameToId.keySet()), true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reloadCurrent() throws IOException {
        var filePath = TEMP;

        initializeAsset(new PixelAsset(filePath, assetPath.getFileName().toString()), assetPath);
        getCanvas().openFile(asset, FilenameUtils.getBaseName(assetPath.getFileName().toString()));
    }

    public static Map<String, byte[]> move(Path path) throws IOException {
        FileUtils.deleteDirectory(TEMP.toFile());
        Files.createDirectories(TEMP);

        var seven = PixelAsset.getSevenZipFile(path);

        var files = new HashMap<String, byte[]>();

        for (var entry : seven.getEntries()) {
            files.put(entry.getName(), seven.getInputStream(entry).readAllBytes());
        }

//        System.out.println(files.keySet());

        for (var file : files.entrySet()) {
            if(file.getKey().isEmpty()) continue;

            var filePath = TEMP.resolve(file.getKey());
//            System.out.println(filePath);

            if(Files.isDirectory(filePath)) continue;
            Files.createFile(filePath);
            Files.write(filePath, file.getValue());
        }

        return files;
    }

    @Override
    public void keyTyped(char keyChar, int keyCode, int scancode, int mods) {
    }

    @Override
    public void keyPressed(int key, int scancode, int mods) {
        handleKey(key, scancode, mods, false);
    }

    private void handleKey(int key, int scancode, int mods, boolean heldDown) {

        pressedKeys.add(key);

        boolean isCtrlPressed  = (mods & GLFW.GLFW_MOD_CONTROL) != 0;
        boolean isShiftPressed = (mods & GLFW.GLFW_MOD_SHIFT)   != 0;
        boolean isAltPressed   = (mods & GLFW.GLFW_MOD_ALT)     != 0;

        if(heldDown) {
            arcBall.keyPressed(key);
        } else if (isCtrlPressed) {
            switch (key) {
                case GLFW.GLFW_KEY_S -> save();
                case GLFW.GLFW_KEY_SPACE -> arcBall.reset();
            }
        } else if(isAltPressed) {
//            switch (code) {
//                case KeyEvent.VK_A -> new RareCandyCanvas.CycleVariants(getCanvas(), true);
//                case KeyEvent.VK_Z -> new RareCandyCanvas.CycleVariants(getCanvas(), false);
//            }
        } else if(isShiftPressed) {
            switch (key) {
                case GLFW.GLFW_KEY_P -> {
                    new RareCandyCanvas.CycleVariants(getCanvas(), true);
                }
                case GLFW.GLFW_KEY_O -> {
                    var chosenFiles = DialogueUtils.chooseMultipleFiles("PK;pk");
                    if (chosenFiles != null) openAsset(chosenFiles);
                }
                case GLFW.GLFW_KEY_SPACE -> {
                    try {
                        reloadCurrent();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        } else {
            switch (key) {
                case GLFW.GLFW_KEY_P -> {
                    new RareCandyCanvas.CycleVariants(getCanvas(), false);
                }
                case GLFW.GLFW_KEY_O -> {
                    Path chosenFile;
                    if (filesToOpen.isEmpty()) {

                        chosenFile = DialogueUtils.chooseFile("PK;pk");
                    }
                    else {
                        index++;
                        System.out.println("Selecting: " + (index + 1) + "/" + amount);
                        chosenFile = filesToOpen.remove(0);
                    }

                    if (chosenFile != null) openAsset(chosenFile);
                }
                case GLFW.GLFW_KEY_LEFT_BRACKET -> RareCandyCanvas.setLightLevel((float) Math.max(lightLevel - 0.01, 0));
                case GLFW.GLFW_KEY_RIGHT_BRACKET -> RareCandyCanvas.setLightLevel((float) Math.min(lightLevel + 0.01, 1));

                case GLFW.GLFW_KEY_SPACE -> RareCandyCanvas.animate = !RareCandyCanvas.animate;
                default -> arcBall.keyPressed(key);
            }
        }

//        System.out.println(pressedKeys);
    }

    @Override
    public void keyReleased(int keyCode, int scancode, int mods) {
//        System.out.println("Before: " + pressedKeys);
//        pressedKeys.remove((Integer) e.getKeyCode());
//        System.out.println("After: " + pressedKeys);
    }

    @Override
    public void keyHeld(int key, int scancode, int mods) {
        handleKey(key, scancode, mods, true);
    }

    public void convertGlb(Path chosenFile) {
        try {
            var is = Files.newInputStream(chosenFile);
            var filePath = Path.of(chosenFile.toString().replace(".glb", ".pk"));
            initializeAsset(new PixelAsset(chosenFile.getFileName().toString(), is.readAllBytes()), filePath);
            var title = BASE_TITLE + " - " + filePath.getFileName().toString();
            gui.setTitle(title);
            gui.canvas.openFile(asset, "");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void openAsset(List<Path> chosenFiles) {
        System.out.println("Loading " + chosenFiles.size() + " into queue.");
        index = 0;
        amount = chosenFiles.size();

        filesToOpen.addAll(chosenFiles);

        System.out.println("Selecting: " + (index + 1) + "/" + amount);
        openAsset(filesToOpen.remove(0));
    }

    public class ArcballOrbit implements MouseMotionListener, MouseWheelListener, MouseListener {
        private final Matrix4f viewMatrix;
        private final RareCandyCanvas canvas;
        private float radius;
        private float angleX;
        private float angleY;
        private float lastX, lastY, offsetX, offsetY;

        private final Vector3f centerOffset = new Vector3f();
        private final Vector3f forward = new Vector3f(0, 0, -1);
        private final Vector3f right   = new Vector3f(1, 0, 0);

        public ArcballOrbit(RareCandyCanvas canvas, float radius, float angleX, float angleY) {
            this.viewMatrix = canvas.viewMatrix;
            this.canvas = canvas;
            this.radius = radius;
            this.angleX = angleX;
            this.angleY = angleY;
            update();
        }

        public void update() {
            viewMatrix.identity().arcball(radius, centerOffset.x, centerOffset.y, centerOffset.z, (angleY + offsetY) * (float) Math.PI * 2f, (angleX + offsetX) * (float) Math.PI * 2f);

            float yaw = (angleX + offsetX) * (float)Math.PI * 2f;
            float cos = (float) Math.cos(yaw);
            float sin = (float) Math.sin(yaw);

            forward.set(sin, 0f, -cos);
            right.set(cos, 0f, sin);
        }

        @Override
        public void mouseDragged(long window, double x, double y) {
            float dx = (float)((x - lastX) * 0.001f);
            float dy = (float)((y - lastY) * 0.001f);

            // Determine inversion from current orientation (no prediction)
            float currentPitch = (angleY + offsetY) * (float)Math.PI * 2f;
            currentPitch = currentPitch - (float)Math.floor(currentPitch); // normalize to [0,1)
            if (Math.cos(currentPitch) < 0f) {
                dx = -dx;
            }

            offsetX = dx;
            offsetY = dy;
            update();
        }


        @Override
        public void mouseMoved(long window, double x, double y) {}

        @Override
        public void mouseWheelMoved(long window, double xoffset, double yoffset) {
            if(ImGui.getIO().getWantCaptureMouse()) return;
            float scrollAmount = (float) yoffset;
            radius += scrollAmount * 0.1f;
            update();
        }

        @Override
        public void mouseClicked(long window, int button, int mods, double x, double y) {}

        @Override
        public void mousePressed(long window, int button, int mods, double x, double y) {
            offsetX = 0;
            offsetY = 0;

            lastX = (float) x;
            lastY = (float) y;
        }

        @Override
        public void mouseReleased(long window, int button, int mods, double x, double y) {
            angleX += offsetX;
            angleY += offsetY;
            offsetX = 0;
            offsetY = 0;
            lastX = 0;
            lastY = 0;

            update();
        }

        public void keyPressed(int code) {
            float lateralStep = 0.01f; // Adjust the step size as needed

            if(!RareCandyCanvas.cycling) {

                switch (code) {
                    case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_A -> centerOffset.fma(-lateralStep, right);
                    case GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_D -> centerOffset.fma(lateralStep, right);
                    case GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_W -> centerOffset.fma(lateralStep, forward);
                    case GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_S -> centerOffset.fma(-lateralStep, forward);
                    case GLFW.GLFW_KEY_PAGE_UP, GLFW.GLFW_KEY_Q -> centerOffset.y += lateralStep;
                    case GLFW.GLFW_KEY_PAGE_DOWN, GLFW.GLFW_KEY_E -> centerOffset.y -= lateralStep;
                }

                update();
            }
        }

        public void reset() {
            if(canvas.loadedModel == null) {
                radius = 2f;
                centerOffset.set(0, 0, 0);
            } else {
                radius = ((canvas.loadedModel.dimensions.get(canvas.loadedModel.dimensions.maxComponent())) * canvas.loadedModel.scale)/2f;
                centerOffset.set(0, radius, 0);
            }

            lastX = lastY = 0;
            angleX = -0.125f;
            angleY = 0.125f;
        }

    }
}