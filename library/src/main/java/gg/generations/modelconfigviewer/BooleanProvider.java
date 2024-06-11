//package gg.generations.modelconfigviewer;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.function.Consumer;
//
//public class BooleanProvider extends JPanel /*implements ModelConfigTree.ComponentProvider*/ {
//
//    public BooleanProvider(String labelText, boolean value, Consumer<Boolean> updater) {
//
//        setLayout(new BorderLayout());
//        var checkBox = new JCheckBox(labelText, value);
//        checkBox.addActionListener(e -> updater.accept(checkBox.isSelected()));
//        add(checkBox, BorderLayout.WEST);
//    }
////    @Override
//    public Component getComponent() {
//        return this;
//    }
//}
