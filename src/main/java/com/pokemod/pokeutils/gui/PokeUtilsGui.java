package com.pokemod.pokeutils.gui;

import com.github.weisj.darklaf.LafManager;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.pokemod.pokeutils.PixelAsset;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.nfd.NativeFileDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PokeUtilsGui extends JPanel implements KeyListener {
    private static final String BASE_TITLE = "Pk Explorer";
    private String currentTitle = BASE_TITLE;
    private boolean dirty = false;
    private JFrame frame;
    private final List<Integer> pressedKeys = new ArrayList<>();

    public PokeUtilsGui() {
        LafManager.install(LafManager.themeForPreferredStyle(LafManager.getPreferredThemeStyle()));

        SwingUtilities.invokeLater(() -> {
            var frame = new JFrame(BASE_TITLE);
            frame.setSize(new Dimension(1920 / 2, 1080 / 2));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            initComponents();
            this.frame = frame;
            addKeyListener(this);
            openArchive.addActionListener(e -> {
                var chosenFile = chooseFile("pk");
                if (chosenFile != null) openAsset(chosenFile);
            });
            save.addActionListener(e -> save());

            frame.setContentPane(this);
            frame.setVisible(true);
        });
    }

    private void save() {
        if (dirty) {
            frame.setTitle(currentTitle.substring(0, currentTitle.length() - 1));
            dirty = false;
        }
    }

    public void markDirty() {
        if (!dirty) {
            this.dirty = true;
            frame.setTitle(frame.getTitle() + "*");
        }
    }

    public void openAsset(Path filePath) {
        try (var is = Files.newInputStream(filePath)) {
            fileViewer.initializeAsset(new PixelAsset(is), filePath.getFileName().toString());
            var title = BASE_TITLE + " - " + filePath.getFileName().toString();
            frame.setTitle(title);
            this.currentTitle = title;
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

    public static Path saveFile(String filterList) {
        var outPath = MemoryUtil.memAllocPointer(1);
        var result = NativeFileDialog.NFD_SaveDialog(filterList, null, outPath);
        if (result == NativeFileDialog.NFD_OKAY) {
            return Paths.get(outPath.getStringUTF8(0));
        }

        MemoryUtil.memFree(outPath);
        return null;
    }

    public static Path chooseFile(String filterList) {
        var outPath = MemoryUtil.memAllocPointer(1);
        var result = NativeFileDialog.NFD_OpenDialog(filterList, null, outPath);
        if (result == NativeFileDialog.NFD_OKAY) {
            return Paths.get(outPath.getStringUTF8(0));
        }

        MemoryUtil.memFree(outPath);
        return null;
    }

    public static void main(String[] args) {
        new PokeUtilsGui();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        var workPanel = new JPanel();
        toolbar = new JMenuBar();
        file = new JMenu();
        openArchive = new JMenuItem();
        save = new JMenuItem();
        saveAs = new JMenuItem();
        var splitPane1 = new JSplitPane();
        fileViewer = new PixelAssetTree(this);
        renderingWindow = new JPanel();

        //======== this ========
        setMinimumSize(null);
        setMaximumSize(null);
        setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));

        //======== workPanel ========
        {
            workPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        }
        add(workPanel, new GridConstraints(2, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

        //======== toolbar ========
        {
            toolbar.setMaximumSize(new Dimension(29, 20));
            toolbar.setMinimumSize(new Dimension(29, 20));
            toolbar.setPreferredSize(new Dimension(29, 20));
            toolbar.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

            //======== file ========
            {
                file.setText("File");

                //---- openArchive ----
                openArchive.setText("Open Archive (.pk)    ");
                file.add(openArchive);

                //---- save ----
                save.setText("Save");
                file.add(save);

                //---- saveAs ----
                saveAs.setText("Save As");
                file.add(saveAs);
            }
            toolbar.add(file);
        }
        add(toolbar, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

        //======== splitPane1 ========
        {
            splitPane1.setMaximumSize(null);
            splitPane1.setMinimumSize(null);
            splitPane1.setDividerSize(4);
            splitPane1.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

            //---- fileViewer ----
            fileViewer.setPreferredSize(null);
            fileViewer.setMaximumSize(null);
            fileViewer.setMinimumSize(new Dimension(200, 1080));
            splitPane1.setLeftComponent(fileViewer);

            //======== renderingWindow ========
            {
                renderingWindow.setMaximumSize(new Dimension(1920, 1080));
                renderingWindow.setPreferredSize(null);
                renderingWindow.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
            }
            splitPane1.setRightComponent(renderingWindow);
        }
        add(splitPane1, new GridConstraints(1, 0, 1, 1,
                GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                new Dimension(1920, 1080), new Dimension(1920, 1080), null));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JMenuBar toolbar;
    private JMenu file;
    private JMenuItem openArchive;
    private JMenuItem save;
    private JMenuItem saveAs;
    private PixelAssetTree fileViewer;
    private JPanel renderingWindow;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
