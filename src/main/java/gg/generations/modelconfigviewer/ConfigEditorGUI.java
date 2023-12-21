//package gg.generations.modelconfigviewer;
//
//import gg.generations.rarecandy.pokeutils.MaterialReference;
//
//import javax.swing.*;
//import javax.swing.tree.DefaultMutableTreeNode;
//import javax.swing.tree.DefaultTreeCellRenderer;
//import java.awt.*;
//import java.util.HashMap;
//import java.util.Map;
//
//public class ConfigEditorGUI extends JFrame {
//
//    private JTree materialsTree;
//
//    public ConfigEditorGUI() {
//        setTitle("Model Configuration Editor");
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setLayout(new BorderLayout());
//
//        createMaterialsTree();
//
//        add(new JScrollPane(materialsTree), BorderLayout.CENTER);
//
//        pack();
//        setLocationRelativeTo(null);
//        setVisible(true);
//    }
//
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(ConfigEditorGUI::new);
//    }
//
//    private void createMaterialsTree() {
//        // Sample data for the materials tree
//        Map<String, MaterialReference> materials = createSampleMaterials();
//
//        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Materials");
//
//        for (String key : materials.keySet()) {
//            MaterialReference materialReference = materials.get(key);
//            DefaultMutableTreeNode materialNode = new DefaultMutableTreeNode(key);
//
//            JPanel panel = new JPanel(new GridLayout(2, 2));
////            JTextField textureTextField = new JTextField(materialReference.texture(), 10);
////            JTextField typeTextField = new JTextField(materialReference.type(), 10);
////
////            textureTextField.addActionListener(e -> materialReference.setTexture(textureTextField.getText()));
////
////            typeTextField.addActionListener(e -> materialReference.setType(typeTextField.getText()));
////
////            panel.add(new JLabel("Texture:"));
////            panel.add(textureTextField);
////            panel.add(new JLabel("Type:"));
////            panel.add(typeTextField);
//
//            DefaultMutableTreeNode panelNode = new DefaultMutableTreeNode(panel);
//            materialNode.add(panelNode);
//            rootNode.add(materialNode);
//        }
//
//        materialsTree = new JTree(rootNode);
//        materialsTree.setCellRenderer(new JPanelCellRenderer());
//    }
//
//    private Map<String, MaterialReference> createSampleMaterials() {
//        Map<String, MaterialReference> materials = new HashMap<>();
//
////        materials.put("Material 1", new MaterialReference("Texture 1", "Type 1"));
////        materials.put("Material 2", new MaterialReference("Texture 2", "Type 2"));
////        materials.put("Material 3", new MaterialReference("Texture 3", "Type 3"));
//
//        return materials;
//    }
//
//    private static class JPanelCellRenderer extends DefaultTreeCellRenderer {
//        @Override
//        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
//                                                      boolean leaf, int row, boolean hasFocus) {
//            Component renderer = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
//            if (value instanceof DefaultMutableTreeNode node) {
//                Object userObject = node.getUserObject();
//                if (userObject instanceof JPanel panel) {
//                    if (selected) {
//                        panel.setBackground(getBackgroundSelectionColor());
//                        panel.setForeground(getTextSelectionColor());
//                    } else {
//                        panel.setBackground(getBackgroundNonSelectionColor());
//                        panel.setForeground(getTextNonSelectionColor());
//                    }
//                    return panel;
//                }
//            }
//            return renderer;
//        }
//    }
//}