//package gg.generations.modelconfigviewer;
//
//import javax.swing.*;
//import java.awt.*;
//import java.util.function.Consumer;
//
//public class IntegerProvider extends JPanel implements ModelConfigTree.ComponentProvider {
//    private final JLabel label;
//    private final JSlider slider;
//    private final IntegerTextField textField;
//    private final Consumer<Integer> updater;
//
//    public IntegerProvider(String labelText, int value, int minValue, int maxValue, Consumer<Integer> updater) {
//        this.updater = updater;
//
//        setLayout(new BorderLayout());
//        label = new JLabel(labelText);
//        slider = new JSlider(JSlider.HORIZONTAL, minValue, maxValue, value);
//
//        slider.addChangeListener(e -> {
//            var sliderValue = slider.getValue();
//            update(sliderValue);
//        });
//
//        textField = new IntegerTextField(minValue, maxValue, value, this::update);
//
//
//        textField.setPreferredSize(new Dimension(50, textField.getPreferredSize().height));
//        textField.setText(String.valueOf(value));
//
//        add(label, BorderLayout.WEST);
//        add(slider, BorderLayout.CENTER);
//        add(textField, BorderLayout.EAST);
//    }
//
//    public void setValue(int f) {
//        slider.setValue(f);
//        textField.setValue(f);
//    }
//
//    private void update(int f) {
//        updater.accept(f);
//        setValue(f);
//    }
//
//    @Override
//    public Component getComponent() {
//        return this;
//    }
//}
//
