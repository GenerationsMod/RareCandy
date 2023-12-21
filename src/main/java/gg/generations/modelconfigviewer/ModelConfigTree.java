//package gg.generations.modelconfigviewer;
//
//import gg.generations.rarecandy.pokeutils.*;
//import gg.generations.rarecandy.tools.gui.DialogueUtils;
//import org.joml.Vector3f;
//import org.lwjgl.util.nfd.NativeFileDialog;
//
//import javax.swing.*;
//import javax.swing.event.CellEditorListener;
//import javax.swing.tree.*;
//import java.awt.*;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.util.*;
//import java.util.List;
//import java.util.function.*;
//
//public class ModelConfigTree {
//    public static void main(String[] args) throws IOException {
//        NativeFileDialog.NFD_Init();
//        ModelConfigTree.createAndShowGUI();
//    }
//
//    private static void createAndShowGUI() throws IOException {
//        var path = DialogueUtils.chooseFile("PK;pk");
//
//        if(path == null) return;
////
//        var asset = new PixelAsset(Files.newInputStream(path), path.getFileName().toString());
//        var config = asset.getConfig();
////        var images = asset.getImageFiles().stream().map(Map.Entry::getKey).toList();
////        var imagesNode = images.stream().reduce(new DefaultMutableTreeNode("Images"), new BiFunction<DefaultMutableTreeNode, String, DefaultMutableTreeNode>() {
////            @Override
////            public DefaultMutableTreeNode apply(DefaultMutableTreeNode defaultMutableTreeNode, String s) {
////                defaultMutableTreeNode.add(new DefaultMutableTreeNode(s));
////                return defaultMutableTreeNode;
////            }
////        }, (defaultMutableTreeNode, defaultMutableTreeNode2) -> defaultMutableTreeNode);
////
////        var rootNode = new DefaultMutableTreeNode(asset.name);
////
////        rootNode.add(imagesNode);
////        rootNode.add(createRoot(config, images));
////
//        var renderer = new DefaultTreeCellRenderer() {
//            @Override
//            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
//                if (value instanceof ComponentProvider<?, ?> provider) {
//                    return provider.label;
//                }
//
//                return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
//            }
//        };
//
////        var editor = new TreeCellEditor() {
////
////            @Override
////            public Object getCellEditorValue() {
////                return null;
////            }
////
////            @Override
////            public boolean isCellEditable(EventObject anEvent) {
////                return false;
////            }
////
////            @Override
////            public boolean shouldSelectCell(EventObject anEvent) {
////                return false;
////            }
////
////            @Override
////            public boolean stopCellEditing() {
////                return false;
////            }
////
////            @Override
////            public void cancelCellEditing() {
////
////            }
////
////            @Override
////            public void addCellEditorListener(CellEditorListener l) {
////
////            }
////
////            @Override
////            public void removeCellEditorListener(CellEditorListener l) {
////
////            }
////
////            @Override
////            public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
////                if(value instanceof ComponentProvider<?,?> provider) return provider.editingComponent();
////                else return null;
////            }
////        }
//
//        // Create the JTree with custom cell renderer and editor
////        JTree tree = new JTree((ComponentProvider) () -> new Label("Rawr"));
////        tree.setToggleClickCount(1);
////        tree.setCellRenderer(renderer);
//////        tree.setCellEditor(new DefaultTreeCellEditor(tree, renderer, new ComponentProviderEditor()));
////        tree.setEditable(true);
//
//        // Create the JFrame and add the JTree
//        JFrame frame = new JFrame("Float Tree Example");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
////        frame.getContentPane().add(new JScrollPane(tree));
//        frame.pack();
//        frame.setLocationRelativeTo(null);
//        frame.setVisible(true);
//    }
//
//    private static DefaultMutableTreeNode createRoot(ModelConfig config, List<String> images) {
//
//        var scale = new DefaultMutableTreeNode(new FloatProvider("Scale", config.scale, 0, 10, s -> config.scale = s));
//        var materials = new DefaultMutableTreeNode("Materials");
//        var list = fullImageList(images);
//        config.materials.forEach((s, reference) -> materials.add(materialReferenceNode(s, reference, list)));
//
//        var node = new DefaultMutableTreeNode("Config");
//        node.add(scale);
//        node.add(materials);
//
//        return node;
//    }
//
//    public static DefaultMutableTreeNode materialReferenceNode(String name, MaterialReference reference, List<String> images) {
//        var root = new DefaultMutableTreeNode(name);
//        root.add(new DefaultMutableTreeNode(new EnumDropdown<>("Blend", BlendType.class, reference.blend, blendType -> reference.blend = blendType)));
//        root.add(new DefaultMutableTreeNode(new EnumDropdown<>("Cull", CullType.class, reference.cull, cullType -> reference.cull = cullType)));
//        root.add(new DefaultMutableTreeNode(new ListDropdown<>("Shader", types, reference.shader, shader -> reference.shader = shader)));
//        root.add(mapNode("Images", reference.images, new BiFunction<String, String, DefaultMutableTreeNode>() {
//            @Override
//            public DefaultMutableTreeNode apply(String type, String image) {
//                return new DefaultMutableTreeNode(new ListDropdown<>(type, images, image, (name) -> reference.images.put(type, name)));
//            }
//        }));
//
//        root.add(mapNode("Values", reference.values, (type, value) -> {
//            if(value instanceof Float val) {
//                return new DefaultMutableTreeNode(new FloatProvider(type, val, 0.0f, 10.0f, a -> reference.values.put(type, val)));
//            } else if(value instanceof Vector3f val) {
//                var label = new Label(type + " ( " + Vector3fComponent.color(val.x, val.y, val.z) + " )");
//                var holder = new StringHolder(label::getText);
//                var node = new DefaultMutableTreeNode(new StringHolder(label::getText));
//                node.setUserObject(holder);
//                node.add(new DefaultMutableTreeNode(new Vector3fComponent(val, a -> {
//                    reference.values.put(type, val);
//                    label.setText(type + " (" + Vector3fComponent.color(val.x, val.y, val.z) + ")");
//                    node.setUserObject(holder);
//                })));
//                return node;
//            } else if(value instanceof Boolean val) {
//                return new DefaultMutableTreeNode(new BooleanProvider(type, val, a -> reference.values.put(type, val)));
//            } else {
//                return null;
//            }
//        }));
//        return root;
//    }
//
//    public static <E> DefaultMutableTreeNode mapNode(String name, Map<String, E> map, BiFunction<String, E, DefaultMutableTreeNode> function) {
//        var node = new DefaultMutableTreeNode(name);
//        map.forEach(new BiConsumer<String, E>() {
//            @Override
//            public void accept(String s, E e) {
//                var node1 = function.apply(s, e);
//
//                if(node1 != null) node.add(node1);
//            }
//        });
//
//        return node;
//    }
//
//    public static class ComponentProvider<T, V extends Component> {
//        private final Function<T, String> function;
//        private JLabel label = new JLabel();
//        private final Supplier<T> supplier;
//        private final Consumer<T> consumer;
//        private final V editingComponent;
//        private final BiConsumer<T, V> setFunction;
//        private final Function<V, T> getFunction;
//
//        public ComponentProvider(Supplier<T> supplier, Function<T, String> function, Consumer<T> consumer, V editingComponent, BiConsumer<T, V> setFunction, Function<V, T> getFunction) {
//            this.supplier = supplier;
//            this.function = function;
//
//            this.consumer = consumer;
//            this.editingComponent = editingComponent;
//            this.setFunction = setFunction;
//            this.getFunction = getFunction;
//
//            label.setName(function.apply(supplier.get()));
//        }
//
//        public V editingComponent() {
//            return editingComponent;
//        }
//
//        public void update(T val) {
//            consumer.accept(val);
//            label.setName(function.apply(supplier.get()));
//        }
//    }
//
//    public static class ComponentProviderTreeNode implements TreeNode {
//        private ComponentProviderTreeNode parent = null;
//        private List<ComponentProviderTreeNode> children = new ArrayList<>();
//
//        @Override
//        public TreeNode getChildAt(int childIndex) {
//            return children.get(childIndex);
//        }
//
//        @Override
//        public int getChildCount() {
//            return children.size();
//        }
//
//        @Override
//        public TreeNode getParent() {
//            return parent;
//        }
//
//        @Override
//        public int getIndex(TreeNode node) {
//            return children.indexOf(node);
//        }
//
//        @Override
//        public boolean getAllowsChildren() {
//            return true;
//        }
//
//        @Override
//        public boolean isLeaf() {
//            return children.size() == 0;
//        }
//
//        @Override
//        public Enumeration<? extends TreeNode> children() {
//            return Collections.enumeration(children);
//        }
//    }
//
//
//    public static List<String> fullImageList(List<String> list) {
//        var images = new ArrayList<String>();
//        images.add("dark");
//        images.add("neutral");
//        images.add("bright");
//        images.add("paradox_mask");
//        images.add("blank");
//        images.addAll(list);
//        return images;
//    }
//
//    public static java.util.List<String> types = List.of(
//            "masked",
//            "layered",
//            "paradox",
//            "solid"
//    );
//
//    public static record StringHolder(Supplier<String> supplier) {
//        @Override
//        public String toString() {
//            return supplier.get();
//        }
//    }
//
//    public static class ModelConfigTreeNode implements TreeNode {
//
//        @Override
//        public TreeNode getChildAt(int childIndex) {
//            return null;
//        }
//
//        @Override
//        public int getChildCount() {
//            return 0;
//        }
//
//        @Override
//        public TreeNode getParent() {
//            return null;
//        }
//
//        @Override
//        public int getIndex(TreeNode node) {
//            return 0;
//        }
//
//        @Override
//        public boolean getAllowsChildren() {
//            return false;
//        }
//
//        @Override
//        public boolean isLeaf() {
//            return false;
//        }
//
//        @Override
//        public Enumeration<? extends TreeNode> children() {
//            return null;
//        }
//    }
//}