package com.pokemod.pokeutils.gui;

import com.pokemod.pokeutils.PixelAsset;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GuiHandler implements KeyListener {
    private static final LZMA2Options OPTIONS = new LZMA2Options();
    private static final String BASE_TITLE = "Pk Explorer";
    private final PokeUtilsGui gui;
    private String currentTitle = BASE_TITLE;
    private boolean dirty = false;
    public PixelAsset asset;
    public Path assetPath;
    private final JFrame frame;
    private final List<Integer> pressedKeys = new ArrayList<>();

    public GuiHandler(JFrame frame, PokeUtilsGui gui) {
        this.frame = frame;
        this.gui = gui;

        gui.setHandler(this);
        frame.setTitle(currentTitle);
        frame.setVisible(true);
        frame.pack();
        frame.transferFocus();
        gui.canvasPanel.addMouseWheelListener(e -> getCanvas().onMouseScroll(e));
    }

    public RareCandyCanvas getCanvas() {
        return (RareCandyCanvas) gui.canvasPanel.getComponents()[0];
    }

    public void initializeAsset(PixelAsset asset, Path path) {
        this.asset = asset;
        this.assetPath = path;
        ((PixelAssetTree) gui.fileViewer).initializeAsset(asset, path);
    }

    public void save() {
        save(assetPath);
    }

    public void save(Path savePath) {
        if (dirty) {
            try {
                if (!Files.exists(savePath)) {
                    Files.deleteIfExists(savePath);
                    Files.createDirectories(savePath.getParent());
                    Files.createFile(savePath);
                }

                var saveBox = new JDialog(frame, "Saving File", true);
                var progressBar = (JProgressBar) saveBox.add(BorderLayout.CENTER, new JProgressBar(0, 100));
                var fileChunk = 100 / asset.files.size();
                saveBox.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                saveBox.setSize(300, 50);
                saveBox.setLocationRelativeTo(frame);
                SwingUtilities.invokeLater(() -> saveBox.setVisible(true));

                new Thread(() -> {
                    try (var xzWriter = new XZOutputStream(Files.newOutputStream(assetPath), OPTIONS)) {
                        try (var tarWriter = new TarArchiveOutputStream(xzWriter)) {

                            for (var file : asset.files.entrySet()) {
                                var entry = new TarArchiveEntry(file.getKey());
                                entry.setSize(file.getValue().length);
                                tarWriter.putArchiveEntry(entry);
                                IOUtils.copy(new BufferedInputStream(new ByteArrayInputStream(file.getValue())), tarWriter);
                                tarWriter.closeArchiveEntry();
                                SwingUtilities.invokeLater(() -> progressBar.setValue(progressBar.getValue() + fileChunk));
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    SwingUtilities.invokeLater(() -> saveBox.setVisible(false));
                    frame.setTitle(currentTitle.substring(0, currentTitle.length() - 1));
                }).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            dirty = false;
        }
    }

    public void markDirty() {
        if (!dirty) {
            this.dirty = true;
            currentTitle = frame.getTitle() + "*";
            frame.setTitle(frame.getTitle() + "*");
        }
    }

    public void openAsset(Path filePath) {
        try (var is = Files.newInputStream(filePath)) {
            initializeAsset(new PixelAsset(is), filePath);
            var title = BASE_TITLE + " - " + filePath.getFileName().toString();
            frame.setTitle(title);
            this.currentTitle = title;
            getCanvas().openFile(asset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());

        var isCtrlPressed = pressedKeys.contains(KeyEvent.VK_CONTROL);
        if (e.getKeyCode() == KeyEvent.VK_S && isCtrlPressed) save();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove((Integer) e.getKeyCode());
    }

    public void convertGlb(Path chosenFile) {
        try {
            var is = Files.newInputStream(chosenFile);
            var filePath = Path.of(chosenFile.toString().replace(".glb", ".pk"));
            initializeAsset(new PixelAsset(chosenFile.getFileName().toString(), is.readAllBytes()), filePath);
            var title = BASE_TITLE + " - " + filePath.getFileName().toString();
            frame.setTitle(title);
            this.currentTitle = title;
            getCanvas().openFile(asset);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
