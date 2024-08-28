package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.PixelAsset;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
                else if (path.getParentPath() != null) {
                    var node = path.getParentPath().getLastPathComponent().toString();

                    switch (node) {
                        case "animations" -> PixelAssetTree.this.gui.handler.getCanvas().setAnimation(path.getLastPathComponent().toString().replace(".tranm", "").replace(".smd", "").replace(".gfbanm", ""));
                        case "variants" -> PixelAssetTree.this.gui.handler.getCanvas().setVariant(path.getLastPathComponent().toString());
                        case "objects" -> {
                            var object1 = path.getLastPathComponent();

                            var object = object1.toString();
                            var add = object.startsWith("-");

                            if(add) object = object.substring(1);

                            if(object1 instanceof DefaultMutableTreeNode) {
                                PixelAssetTree.this.gui.handler.getCanvas().toggleObject(add, object);
                                ((DefaultMutableTreeNode) object1).setUserObject(!add ? "-" + object : object);
                            }
                        }
                    }
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

    public void initializeAsset(PixelAsset asset, Path assetPath, Set<String> animations) {
        var tree = node(assetPath.getFileName().toString());
        var animationsNode = node("animations");
        var imagesNode = node("images");

        List<String> variants = asset.getConfig() != null && asset.getConfig().variants != null ? List.copyOf(asset.getConfig().variants.keySet()) : new ArrayList<>();

        var objs = asset.getConfig().defaultVariant.keySet();

        for (var s : asset.files.keySet()) {
            if(s.endsWith("tranm") || s.endsWith("tracm") || s.endsWith("gfbanm") || s.endsWith("smd")) {
//                if(!animationStrings.contains(s)) {
//                    animationStrings.add(s.replace(".tracm", "").replace(".tranm", "").replace(".gfbanm", "").replace(".smd", ""));
//                }
            } else if (s.endsWith("png")) {
                imagesNode.add(node(s));
            }/* else if(s.equals("config.json")) {
                tree.add(new ModConfigTreeNode(asset.getConfig()));
            }*/ else tree.add(node(s));
        }

        animations.stream().sorted().map(this::node).forEach(animationsNode::add);

        if (animationsNode.getChildCount() > 0) tree.add(animationsNode);
        if (imagesNode.getChildCount() > 0) tree.add(imagesNode);

        if (!variants.isEmpty()) {
            var modelAnimationsNode = node("variants");
            for (var name : variants) modelAnimationsNode.add(node(name));
            tree.add(modelAnimationsNode);
        }

        var modelAnimationsNode = node("objects");
        for (var name : objs) modelAnimationsNode.add(node(name));
        tree.add(modelAnimationsNode);


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