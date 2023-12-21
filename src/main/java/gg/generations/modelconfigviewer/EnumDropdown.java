//package gg.generations.modelconfigviewer;
//
//import gg.generations.rarecandy.pokeutils.BlendType;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.function.Consumer;
//
//public class EnumDropdown<E extends Enum<E>> extends JPanel /*implements ModelConfigTree.ComponentProvider*/ {
//    private final JComboBox<E> enumComboBox;
//
//    public EnumDropdown(String name, Class<E> enumClass, E value, Consumer<E> consumer) {
//        setLayout(new FlowLayout(FlowLayout.LEFT));
//
//        enumComboBox = new JComboBox<>(enumClass.getEnumConstants());
//        enumComboBox.setSelectedIndex(value.ordinal());
//
//        enumComboBox.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                E selectedEnum = (E) enumComboBox.getSelectedItem();
//                if (selectedEnum != null) {
//                    consumer.accept(selectedEnum);
//                }
//            }
//        });
//
//        add(new JLabel(name));
//        add(enumComboBox);
//    }
//
//    public E getSelectedEnum() {
//        return (E) enumComboBox.getSelectedItem();
//    }
//
//    @Override
//    public Component getComponent() {
//        return this;
//    }
//}