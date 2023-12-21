//package gg.generations.modelconfigviewer;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.List;
//import java.util.function.Consumer;
//
//public class ListDropdown<E> extends JPanel implements ModelConfigTree.ComponentProvider {
//    private final JComboBox<E> enumComboBox;
//
//    public ListDropdown(String name, List<E> list, E value, Consumer<E> consumer) {
//        setLayout(new FlowLayout(FlowLayout.LEFT));
//
//        enumComboBox = new JComboBox<>(new ListComboBoxModel<>(list));
//        enumComboBox.setSelectedItem(value);
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