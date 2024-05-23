package gg.generations.rarecandy.tools.gui;

import de.javagl.jgltf.model.NamedModelElement;
import de.javagl.jgltf.model.io.GltfModelReader;
import gg.generations.rarecandy.pokeutils.PixelAsset;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class PixelAssetTree extends JTree {

    public final PokeUtilsGui gui;

    public PixelAssetTree(PokeUtilsGui gui) {
        super();
        this.gui = gui;
        setDragEnabled(false);
        setTransferHandler(new FilesystemTransferHandler());
        setModel(null);

//        var renderer = new DefaultTreeCellRenderer() {
//            @Override
//            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
//                if (((DefaultMutableTreeNode) value).getUserObject() instanceof ModelConfigTree.ComponentProvider provider)
//                    return provider.getComponent();
//
//                return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
//            }
//        };

        // Create the JTree with custom cell renderer and editor
//        setCellRenderer(renderer);
//        setCellEditor(new DefaultTreeCellEditor(this, renderer, new ComponentProviderEditor()));

        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                var path = getClosestPathForLocation(e.getPoint().x, e.getPoint().y);

                if (path == null) return;

                if (e.isPopupTrigger())
                    switch (path.getLastPathComponent().toString()) {
                        case "animations" ->
                                new AnimationNodePopup(PixelAssetTree.this, PixelAssetTree.this.gui.handler, e).show(e.getComponent(), e.getX(), e.getY());
                        case "images" ->
                                new ImageNodePopup(PixelAssetTree.this, PixelAssetTree.this.gui.handler, e).show(e.getComponent(), e.getX(), e.getY());
                        default ->
                                new TreeNodePopup(PixelAssetTree.this, PixelAssetTree.this.gui.handler, e).show(e.getComponent(), e.getX(), e.getY());
                    }
                else if (path.getParentPath() != null)
                    if (path.getParentPath().getLastPathComponent().toString().equals("variants")) {
                        var string = path.getLastPathComponent().toString();
                        var index = string.indexOf("(");

                        if(index > -1) string = string.substring(0, index);

                        PixelAssetTree.this.gui.handler.getCanvas().setVariant(string);
                    }
            }
        });

        addTreeSelectionListener(e -> {
            var selectedNode = (DefaultMutableTreeNode) getLastSelectedPathComponent();
        });
    }

    public void initializeAsset(PixelAsset asset, Path assetPath) {
        var tree = node(assetPath.getFileName().toString());
        var imagesNode = node("images");

        List<String> variants = asset.getConfig() != null && asset.getConfig().materials != null ? List.copyOf(asset.getConfig().materials.entrySet().stream().map(a -> a.getKey() + "(" + a.getValue().shader + ")").toList()) : new ArrayList<>();

        variants = variants.stream().sorted().toList();


        for (var s : asset.files.keySet()) {
        if (s.endsWith("jxl")) imagesNode.add(node(s));
            else tree.add(node(s));
        }

        if (!variants.isEmpty()) {
            var modelAnimationsNode = node("variants");
            for (var name : variants) modelAnimationsNode.add(node(name));
            tree.add(modelAnimationsNode);
        }
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