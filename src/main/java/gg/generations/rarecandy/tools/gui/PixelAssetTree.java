package gg.generations.rarecandy.tools.gui;

import gg.generationsmod.rarecandy.FileLocator;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PixelAssetTree extends JTree {

    public final PokeUtilsGui gui;

    public PixelAssetTree(PokeUtilsGui gui) {
        super();
        this.gui = gui;
        setDragEnabled(false);
        setTransferHandler(new FilesystemTransferHandler());
        setModel(null);

        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                var path = getClosestPathForLocation(e.getPoint().x, e.getPoint().y);
                if (path == null) {}
                else if (path.getParentPath() != null)
                    if (path.getParentPath().getLastPathComponent().toString().equals("animations")) {
                        PixelAssetTree.this.gui.handler.getCanvas().setAnimation(path.getLastPathComponent().toString().replace(".tranm", ""));
                    } else if (path.getParentPath().getLastPathComponent().toString().equals("variants")) {
                        PixelAssetTree.this.gui.handler.getCanvas().setVariant(path.getLastPathComponent().toString());
                    }
            }
        });

        addTreeSelectionListener(e -> {
            var selectedNode = (DefaultMutableTreeNode) getLastSelectedPathComponent();

            if (selectedNode != null && selectedNode.getParent() != null && selectedNode.getParent().toString().equals("animations")) {
                this.gui.handler.getCanvas().currentAnimation = selectedNode.toString();
                this.gui.handler.getCanvas().startTime = System.currentTimeMillis();
            }
        });
    }

    public void initializeAsset(FileLocator modelDir) throws IOException {
        var tree = node(modelDir.getPath().getFileName().toString());
        var animationsNode = node("animations");
        var imagesNode = node("images");

        List<String> animationStrings = new ArrayList<>();
        List<String> files = modelDir.getFiles();

        for (var s : files) {
            if (s.endsWith("tranm") || s.endsWith("gfbanm") || s.endsWith("smd"))
                animationStrings.add(s);
            else if (s.endsWith("gltf")) {
                var glbNode = node(s);
                if (!animationStrings.isEmpty()) {
                    var modelAnimationsNode = node("animations");
                    for (var name : animationStrings) modelAnimationsNode.add(node(name));
                    glbNode.add(modelAnimationsNode);
                }
                tree.add(glbNode);
            } else if (s.endsWith("jxl")) {
                imagesNode.add(node(s));
            } else if(s.equals("config.json")) {
                // TODO: water
//                tree.add(new ModConfigTreeNode(asset.getConfig()));
            } else tree.add(node(s));
        }

        animationStrings.stream().sorted().map(this::node).forEach(animationsNode::add);

        if (animationsNode.getChildCount() > 0) tree.add(animationsNode);
        if (imagesNode.getChildCount() > 0) tree.add(imagesNode);

        setEditable(true);
        setModel(new DefaultTreeModel(tree));
    }

    private DefaultMutableTreeNode node(String name, DefaultMutableTreeNode... children) {
        var node = new DefaultMutableTreeNode(name);
        for (var child : children) node.add(child);
        return node;
    }

    private static class FilesystemTransferHandler extends TransferHandler {

        @SuppressWarnings("unchecked")
        public boolean importData(JComponent comp, Transferable t) {
            if (!(comp instanceof PixelAssetTree tree) || !t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                return false;
            }

            try {
                var model = (DefaultTreeModel) tree.getModel();
                var root = (DefaultMutableTreeNode) model.getRoot();
                var data = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);

                for (var f : data) root.add(new DefaultMutableTreeNode(f.getName()));
                return true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            if (comp instanceof PixelAssetTree) {
                for (var transferFlavor : transferFlavors) {
                    if (!transferFlavor.equals(DataFlavor.javaFileListFlavor)) {
                        return false;
                    }
                }

                return true;
            }

            return false;
        }
    }
}