//package gg.generations.modelconfigviewer;
//
//import javax.swing.*;
//import javax.swing.event.ChangeEvent;
//import javax.swing.event.ChangeListener;
//import java.awt.*;
//import java.util.function.Consumer;
//
//public class FloatProvider extends JPanel implements ModelConfigTree.ComponentProvider {
//    private final JLabel label;
//    private final JSlider slider;
//    private final FloatTextField textField;
//    private final Consumer<Float> updater;
//
//    public FloatProvider(String labelText, float value, float minValue, float maxValue, Consumer<Float> updater) {
//        this.updater = updater;
////            this.minValue = minValue;
////            this.maxValue = maxValue;
//
//        setLayout(new BorderLayout());
//        label = new JLabel(labelText);
//        slider = new JSlider(JSlider.HORIZONTAL, (int) (minValue * 10000), (int) (maxValue * 10000), (int) (value * 10000));
//
//        slider.addChangeListener(e -> {
//            float sliderValue = slider.getValue() / 10000.0f;
//            update(sliderValue);
//        });
//
//        textField = new FloatTextField(minValue, maxValue, value, this::update);
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
//    public void setValue(float f) {
//        slider.setValue((int) (f * 10000));
//        textField.setValue(f);
//    }
//
//    private void update(float f) {
//        updater.accept(f);
//        setValue(f);
//    }
//
//    @Override
//    public Component getComponent() {
//        return this;
//    }
//}
