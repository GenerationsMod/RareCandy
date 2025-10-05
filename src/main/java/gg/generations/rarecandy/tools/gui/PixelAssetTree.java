package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.pokeutils.PixelAsset;
import gg.generations.rarecandy.renderer.animation.Animation;
import imgui.ImGui;

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
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class PixelAssetTree {

    public final PokeUtilsGui gui;
    public final PokeUtilsGui.FloatInputComponent scale;

    private CompositeNode tree = new CompositeNode("N/A");

    public PixelAssetTree(PokeUtilsGui gui) {
        super();
        this.gui = gui;
        scale = new PokeUtilsGui.FloatInputComponent("Scale", () -> gui.canvas.originalScaleModifer, scale -> gui.canvas.scaleModifier = (float) scale);

//
//        setDragEnabled(false);
//        setTransferHandler(new FilesystemTransferHandler());
//        setModel(null);
//
//        addMouseListener(new MouseAdapter() {
//            public void mouseReleased(MouseEvent e) {
//                var path = getClosestPathForLocation(e.getPoint().x, e.getPoint().y);
//
//                if (path == null) return;
//
//                if (e.isPopupTrigger())
//                    switch (path.getLastPathComponent().toString()) {
//                        case "animations" ->
//                                new AnimationNodePopup(PixelAssetTree.this, PixelAssetTree.this.gui.handler, e).show(e.getComponent(), e.getX(), e.getY());
//                        case "images" ->
//                                new ImageNodePopup(PixelAssetTree.this, PixelAssetTree.this.gui.handler, e).show(e.getComponent(), e.getX(), e.getY());
//                        default ->
//                                new TreeNodePopup(PixelAssetTree.this, PixelAssetTree.this.gui.handler, e).show(e.getComponent(), e.getX(), e.getY());
//                    }
//                else if (path.getParentPath() != null) {
//                    var node = path.getParentPath().getLastPathComponent().toString();
//
//                    switch (node) {
//                        case "animations" -> PixelAssetTree.this.gui.handler.getCanvas().setAnimation(path.getLastPathComponent().toString().replace(".tranm", "").replace(".smd", "").replace(".gfbanm", ""));
//                        case "variants" -> PixelAssetTree.this.gui.handler.getCanvas().setVariant(path.getLastPathComponent().toString());
//                        case "objects" -> {
//                            var object1 = path.getLastPathComponent();
//
//                            var object = object1.toString();
//                            var add = object.startsWith("-");
//
//                            if(add) object = object.substring(1);
//
//                            if(object1 instanceof DefaultMutableTreeNode) {
//                                PixelAssetTree.this.gui.handler.getCanvas().toggleObject(add, object);
//                                ((DefaultMutableTreeNode) object1).setUserObject(!add ? "-" + object : object);
//                            }
//                        }
//                    }
//                }
//            }
//        });
//
//        addTreeSelectionListener(e -> {
//            var selectedNode = (DefaultMutableTreeNode) getLastSelectedPathComponent();
//
//            if (selectedNode != null && selectedNode.getParent() != null && selectedNode.getParent().toString().equals("animations")) {
//                this.gui.handler.getCanvas().currentAnimation = selectedNode.toString();
//                this.gui.handler.getCanvas().startTime = System.currentTimeMillis();
//            }
//        });
    }

    public void render() {
        ImGui.begin("File Tree");

        tree.render();
        ImGui.separator();

        // --- Scale input integrated here ---
        scale.render();

        ImGui.end();
    }

    private List<String> safeList(List<String> list) {
        return list != null ? list : List.of();
    }

    public void initializeAsset(PixelAsset asset, Path assetPath, Set<String> animations) {
        tree = new CompositeNode(assetPath.getFileName().toString());
        var animationsNode = new RadioListNode("animations", animation -> gui.canvas.setAnimation(animation));
        var imagesNode = new CompositeNode("images");



        List<String> variants = asset.getConfig() != null && asset.getConfig().variants != null ? List.copyOf(asset.getConfig().variants.keySet()) : new ArrayList<>();

        var objs = asset.getConfig().defaultVariant.keySet();

        for (var s : asset.files.keySet()) {
            if(s.endsWith("tranm") || s.endsWith("tracm") || s.endsWith("gfbanm") || s.endsWith("smd")) {
//                if(!animations.contains(s)) {
//                    animationsNode.add(s.replace(".tracm", "").replace(".tranm", "").replace(".gfbanm", "").replace(".smd", ""));
//                }
            } else if (s.endsWith("png")) {
                imagesNode.add(new TextNode(s));
            }/* else if(s.equals("config.json")) {
                tree.add(new ModConfigTreeNode(asset.getConfig()));
            }*/ else tree.add(new TextNode(s));
        }

        animations.stream().sorted().forEach(animationsNode::add);

        if (animationsNode.size() > 0) {
            gui.canvas.setAnimation(animationsNode.getSelectedOption());

            tree.add(animationsNode);
        }
        if (imagesNode.size() > 0) tree.add(imagesNode);

        if (!variants.isEmpty()) {
            var variantsNode = new RadioListNode("variants", variant -> gui.canvas.setVariant(variant));
            for (var name : variants) variantsNode.add(name);
            gui.canvas.setVariant(variantsNode.getSelectedOption());
            tree.add(variantsNode);
        }

        var objectsNode = new CheckboxListNode("objects", (item) -> gui.canvas.toggleObject(item.checked, item.text));
        for (var name : objs) objectsNode.add(name, true);
        tree.add(objectsNode);
    }

    @FunctionalInterface
    public interface Node {
        void render();
    }

    public static class CompositeNode implements Node {
        private final String label;
        private final List<Node> children = new ArrayList<>();

        public CompositeNode(String label) {
            this.label = label;
        }

        public void add(Node child) {
            children.add(child);
        }

        @Override
        public void render() {
            if (ImGui.treeNode(label)) {
                for (Node child : children) {
                    child.render();
                }
                ImGui.treePop();
            }
        }

        public int size() {
            return children.size();
        }
    }

    public record TextNode(String name) implements Node {

        @Override
        public void render() {
            ImGui.text(name);
        }
    }

    public static class RadioListNode implements Node {
        private final String label;
        private final Consumer<String> consumer;
        private final List<String> items = new ArrayList<>();
        private int selected = -1;

        public RadioListNode(String label, Consumer<String> consumer) {
            this.label = label;
            this.consumer = consumer;
        }

        public void add(String text) {
            items.add(text);

            if(selected == -1) {
                selected = 0;
            }
        }

        public int getSelectedIndex() {
            return selected;
        }

        public String getSelectedOption() {
            return selected >= 0 && selected < items.size() ? items.get(selected) : null;
        }

        public void select(int item) {
            selected = item;
            consumer.accept(getSelectedOption());
        }

        @Override
        public void render() {
            if (ImGui.treeNode(label)) {
                for (int i = 0; i < items.size(); i++) {
                    if (ImGui.radioButton(items.get(i), selected == i)) {
                        select(i);
                    }
                }
                ImGui.treePop();
            }
        }

        public boolean contains(String name) {
            return items.contains(name);
        }

        public int size() {
            return items.size();
        }
    }

    public static class CheckboxListNode implements Node {
        private final String label;
        private final Consumer<Item> consumer;
        private final List<Item> items = new ArrayList<>();

        public class Item implements Node {
            public final String text;
            public boolean checked;
            public Item(String text, boolean checked) {
                this.text = text;
                this.checked = checked;
            }

            @Override
            public void render() {
                if(ImGui.checkbox(text, checked)) {
                    checked = !checked;
                    CheckboxListNode.this.consumer.accept(this);
                }
            }
        }

        public CheckboxListNode(String label, Consumer<Item> consumer) {
            this.label = label;
            this.consumer = consumer;
        }

        public void add(String text, boolean initial) {
            items.add(new Item(text, initial));
        }

        public List<Item> getItems() {
            return items;
        }

        @Override
        public void render() {
            if (ImGui.treeNode(label)) {
                for (Item item : items) {
                    item.render();

                }
                ImGui.treePop();
            }
        }
    }

//    private static class FilesystemTransferHandler extends TransferHandler {
//
//        @SuppressWarnings("unchecked")
//        public boolean importData(JComponent comp, Transferable t) {
//            if (!(comp instanceof PixelAssetTree tree) || !t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
//                return false;
//            }
//
//            try {
//                var model = (DefaultTreeModel) tree.getModel();
//                var root = (DefaultMutableTreeNode) model.getRoot();
//                var data = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
//
//                for (var f : data) root.add(new DefaultMutableTreeNode(f.getName()));
//                return true;
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
//            if (comp instanceof PixelAssetTree) {
//                for (var transferFlavor : transferFlavors) {
//                    if (!transferFlavor.equals(DataFlavor.javaFileListFlavor)) {
//                        return false;
//                    }
//                }
//
//                return true;
//            }
//
//            return false;
//        }
//    }
}