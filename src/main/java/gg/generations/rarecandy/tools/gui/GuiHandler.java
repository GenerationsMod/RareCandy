package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.PixelAsset;
import gg.generations.rarecandy.tools.pkcreator.PixelmonArchiveBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.tukaani.xz.LZMA2Options;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static gg.generations.rarecandy.tools.gui.RareCandyCanvas.lightLevel;

public class GuiHandler implements KeyListener {
    private static final Path TEMP = Path.of("temp");
    public static final LZMA2Options OPTIONS = new LZMA2Options();
    private static final String BASE_TITLE = "Pk Explorer";
    private final PokeUtilsGui gui;
    private final JFrame frame;
    private final Set<Integer> pressedKeys = new HashSet<>();
    private final ArcballOrbit arcBall;

    public PixelAsset asset;
    public Path assetPath;
    public int index = 0;
    public int amount;
    private String currentTitle = BASE_TITLE;
    private boolean dirty = false;
    public List<Path> filesToOpen = new ArrayList<>();

    public GuiHandler(JFrame frame, PokeUtilsGui gui) {
        this.frame = frame;
        this.gui = gui;

        gui.setHandler(this);
        frame.setTitle(currentTitle);
        frame.setVisible(true);
        frame.pack();
        frame.transferFocus();
        arcBall = new ArcballOrbit(getCanvas(), 1f, 0.25f, 0);
        getCanvas().attachArcBall(arcBall);
        getCanvas().addKeyListener(this);
    }

    public RareCandyCanvas getCanvas() {
        return (RareCandyCanvas) gui.canvasPanel.getComponents()[0];
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
            currentTitle = frame.getTitle() + "*";
            frame.setTitle(frame.getTitle() + "*");
        }
    }

    public void openAsset(Path filePath) {
        try {

            initializeAsset(new PixelAsset(move(filePath), filePath.getFileName().toString()), filePath);
            var title = BASE_TITLE + " - " + filePath.getFileName().toString();
            frame.setTitle(title);
            this.currentTitle = title;
            getCanvas().openFile(asset, FilenameUtils.getBaseName(filePath.getFileName().toString()), () -> ((PixelAssetTree) gui.fileViewer).initializeAsset(asset, assetPath, Set.of()), true);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reloadCurrent() throws IOException {
        var filePath = TEMP;

        initializeAsset(new PixelAsset(filePath, assetPath.getFileName().toString()), assetPath);
        getCanvas().openFile(asset, FilenameUtils.getBaseName(assetPath.getFileName().toString()));
    }

    public Map<String, byte[]> move(Path path) throws IOException {
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
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());

        var isCtrlPressed = e.isControlDown();
        var isShiftPressed = e.isShiftDown();
        var isAltPressed = e.isAltDown();
        var code = e.getKeyCode();

        if (isCtrlPressed) {
            switch (code) {
                case KeyEvent.VK_S -> save();
                case KeyEvent.VK_SPACE -> arcBall.reset();
            }
        } else if(isAltPressed) {
//            switch (code) {
//                case KeyEvent.VK_A -> new RareCandyCanvas.CycleVariants(getCanvas(), true);
//                case KeyEvent.VK_Z -> new RareCandyCanvas.CycleVariants(getCanvas(), false);
//            }
        } else if(isShiftPressed) {
            switch (code) {
                case KeyEvent.VK_P -> {
                    new RareCandyCanvas.CycleVariants(getCanvas(), true);
                }
                case KeyEvent.VK_O -> {
                    var chosenFiles = DialogueUtils.chooseMultipleFiles("PK;pk");
                    if (chosenFiles != null) openAsset(chosenFiles);
                }
                case KeyEvent.VK_SPACE -> {
                    try {
                        reloadCurrent();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        } else {
            switch (code) {
                case KeyEvent.VK_P -> {
                    new RareCandyCanvas.CycleVariants(getCanvas(), false);
                }
                case KeyEvent.VK_O -> {
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
                case KeyEvent.VK_OPEN_BRACKET -> RareCandyCanvas.setLightLevel((float) Math.max(lightLevel - 0.01, 0));
                case KeyEvent.VK_CLOSE_BRACKET -> RareCandyCanvas.setLightLevel((float) Math.min(lightLevel + 0.01, 1));

                case KeyEvent.VK_SPACE -> RareCandyCanvas.animate = !RareCandyCanvas.animate;
                default -> arcBall.keyPressed(code);
            }
        }

//        System.out.println(pressedKeys);
    }

    @Override
    public void keyReleased(KeyEvent e) {
//        System.out.println("Before: " + pressedKeys);
//        pressedKeys.remove((Integer) e.getKeyCode());
//        System.out.println("After: " + pressedKeys);
    }

    public void convertGlb(Path chosenFile) {
        try {
            var is = Files.newInputStream(chosenFile);
            var filePath = Path.of(chosenFile.toString().replace(".glb", ".pk"));
            initializeAsset(new PixelAsset(chosenFile.getFileName().toString(), is.readAllBytes()), filePath);
            var title = BASE_TITLE + " - " + filePath.getFileName().toString();
            frame.setTitle(title);
            this.currentTitle = title;
            getCanvas().openFile(asset, "");

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

}