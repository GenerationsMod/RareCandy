package modelconfigviewer;

import gg.generations.pokeutils.VariantReference;

import javax.swing.*;
import java.awt.*;

public class VariantReferenceComponentProvider implements ModelConfigTree.ComponentProvider {
    private JTextField textureField;
    private Checkbox typeField;

    public VariantReferenceComponentProvider(VariantReference materialReference) {
        textureField = new JTextField(materialReference.material());
        typeField = new Checkbox("", materialReference.hide());
    }

    public Component getComponent() {
        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Material:"));
        panel.add(textureField);
        panel.add(new JLabel("Hide:"));
        panel.add(typeField);
        return panel;
    }
}