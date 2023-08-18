package gg.generations.modelconfigviewer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import java.awt.*;

public class ComponentProviderEditor extends AbstractCellEditor implements TreeCellEditor {
    private ModelConfigTree.ComponentProvider editorComponentProvider;

    public ComponentProviderEditor() {
    }

    public void setEditorComponentProvider(ModelConfigTree.ComponentProvider editorComponentProvider) {
        this.editorComponentProvider = editorComponentProvider;
        Component editorComponent = editorComponentProvider.getComponent();
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object userObject = node.getUserObject();

        if (userObject instanceof ModelConfigTree.ComponentProvider provider) {
            setEditorComponentProvider(provider);
            return provider.getComponent();
        }

        return null;
    }

    @Override
    public Object getCellEditorValue() {
        if (editorComponentProvider != null) {
            return editorComponentProvider;
        }

        return null;
    }
}