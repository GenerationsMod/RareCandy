package com.pokemod.pokeutils.gui;

import com.pokemod.pokeutils.PixelAsset;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

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
                if (e.isPopupTrigger()) {
                    var path = getClosestPathForLocation(e.getPoint().x, e.getPoint().y);
                    if (path.getLastPathComponent().toString().equals("animations")) return;
                    new TreeNodePopup(PixelAssetTree.this, PixelAssetTree.this.gui.handler, e).show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        addTreeSelectionListener(e -> {
            var selectedNode = (DefaultMutableTreeNode) getLastSelectedPathComponent();

            if (selectedNode != null) {
                this.gui.handler.getCanvas().currentAnimation = selectedNode.toString();
                this.gui.handler.getCanvas().startTime = System.currentTimeMillis();
            }
        });
    }

    public void initializeAsset(PixelAsset asset, Path assetPath) {
        var tree = node(assetPath.getFileName().toString());
        var animationsNode = node("animations");

        for (var s : asset.files.keySet()) {
            if (s.endsWith("pkanm")) animationsNode.add(node(s));
            else tree.add(node(s));
        }

        if (animationsNode.children().hasMoreElements()) tree.add(animationsNode);
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

                for (File f : data) root.add(new DefaultMutableTreeNode(f.getName()));
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
