package gg.generations.modelconfigviewer;

import gg.generations.rarecandy.pokeutils.MaterialReference;
import gg.generations.rarecandy.pokeutils.ModelConfig;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

public class ModConfigTreeNode extends DefaultMutableTreeNode {
//    public ModConfigTreeNode(ModelConfig config) {
//        setUserObject("config.json");
//        this.add(new DefaultMutableTreeNode(new ModelConfigTree.FloatProvider("scale: ", config.scale, value -> {
//        })));
//        this.add(new MapTreeNode<>("materials", config.materials, MaterialReferenceComponentProvider::new));
//        this.add(new MapTreeNode<>("defaultVariant", config.defaultVariant, VariantReferenceComponentProvider::new));
////        this.add(new NestedMapNode<>("variants", config.variants, (key, materialReference) -> {
////            var larp = materialReference.fillIn(config.defaultVariant.get(key));
////            var blep = new VariantReferenceComponentProvider(larp);
////            return blep;
////        }));
//    }
//
//    public class MaterialReferenceComponentProvider implements ModelConfigTree.ComponentProvider {
//        private final MaterialReference materialReference;
//        private final JTextField textureField;
//        private final JTextField typeField;
//
//        public MaterialReferenceComponentProvider(MaterialReference materialReference) {
//            this.materialReference = materialReference;
//            textureField = new JTextField(materialReference.texture());
//            typeField = new JTextField(materialReference.type());
//        }
//
//        public Component getComponent() {
//            JPanel panel = new JPanel(new GridLayout(2, 2));
//            panel.add(new JLabel("Texture:"));
//            panel.add(textureField);
//            panel.add(new JLabel("Type:"));
//            panel.add(typeField);
//            return panel;
//        }
//    }
}



