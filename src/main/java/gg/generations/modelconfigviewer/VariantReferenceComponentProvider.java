package gg.generations.modelconfigviewer;

import gg.generations.rarecandy.pokeutils.VariantDetails;

import javax.swing.*;
import java.awt.*;

public class VariantReferenceComponentProvider implements ModelConfigTree.ComponentProvider {
    private final JTextField textureField;
    private final Checkbox typeField;

    public VariantReferenceComponentProvider(VariantDetails materialReference) {
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