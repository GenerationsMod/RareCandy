package modelconfigviewer;

import gg.generations.pokeutils.ModelConfig;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.function.Consumer;

public class ModelConfigTree {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ModelConfigTree::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        // Create sample data
        var config = new ModelConfig();
        config.materials = new HashMap<>();
        config.materials.put("blep", new ModelConfig.MaterialReference("blep", "blep"));
        ModConfigTreeNode rootNode = new ModConfigTreeNode(config);

        var renderer = new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object userObject = node.getUserObject();

                if (userObject instanceof ComponentProvider provider) {
                    return provider.getComponent();
                }

                return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            }
        };

        // Create the JTree with custom cell renderer and editor
        JTree tree = new JTree(rootNode);
        tree.setCellRenderer(renderer);
        tree.setCellEditor(new DefaultTreeCellEditor(tree, renderer, new ComponentProviderEditor()));
        tree.setEditable(true);

        // Create the JFrame and add the JTree
        JFrame frame = new JFrame("Float Tree Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new JScrollPane(tree));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public interface ComponentProvider {
        Component getComponent();
    }

    public static class FloatProvider extends JPanel implements ComponentProvider {
        private final JLabel label;
        private final FloatTextField textField;

        public FloatProvider(String labelText, float value, Consumer<Float> updater) {
            setLayout(new BorderLayout());
            label = new JLabel(labelText);
            textField = new FloatTextField(value, updater);
            textField.setPreferredSize(new Dimension(50, textField.getPreferredSize().height));
            textField.setText(String.valueOf(value));
            add(label, BorderLayout.WEST);
            add(textField, BorderLayout.CENTER);
        }

        @Override
        public Component getComponent() {
            return this;
        }
    }
}

