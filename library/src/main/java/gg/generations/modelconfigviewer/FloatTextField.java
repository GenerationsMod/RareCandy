//package gg.generations.modelconfigviewer;
//
//import javax.swing.*;
//import java.awt.event.FocusAdapter;
//import java.awt.event.FocusEvent;
//import java.awt.event.KeyEvent;
//import java.util.function.Consumer;
//
//import javax.swing.*;
//import java.awt.event.FocusAdapter;
//import java.awt.event.FocusEvent;
//import java.awt.event.KeyEvent;
//import java.util.function.Consumer;
//
//public class FloatTextField extends JTextField {
//    private final Consumer<Float> valueConsumer;
//    private final float minValue;
//    private final float maxValue;
//
//    public FloatTextField(float value, float minValue, float maxValue, Consumer<Float> valueConsumer) {
//        super();
//        setHorizontalAlignment(JTextField.RIGHT);
//        this.valueConsumer = valueConsumer;
//        this.minValue = minValue;
//        this.maxValue = maxValue;
//        this.setText(String.valueOf(value));
//
//        addKeyListener(new java.awt.event.KeyAdapter() {
//            @Override
//            public void keyTyped(KeyEvent e) {
//                char c = e.getKeyChar();
//                if (!(Character.isDigit(c) || c == '.')) {
//                    e.consume();
//                }
//            }
//        });
//
//        addFocusListener(new FocusAdapter() {
//            @Override
//            public void focusLost(FocusEvent e) {
//                completeAndSetValue();
//            }
//        });
//    }
//
//    private void completeAndSetValue() {
//        String text = getText();
//        if (!text.isEmpty()) {
//            // Check if there is a missing '0' before or after the decimal point
//            if (text.startsWith(".")) {
//                text = "0" + text;
//            }
//            if (text.endsWith(".")) {
//                text = text + "0";
//            }
//
//            try {
//                float value = Float.parseFloat(text);
//                value = clampValue(value); // Ensure the value is within the specified range
//                valueConsumer.accept(value);
//                setText(String.valueOf(value)); // Update the text field with the clamped value
//            } catch (NumberFormatException e) {
//                // Invalid float value, set to 0
//                valueConsumer.accept(0f);
//                setText("0");
//            }
//        } else {
//            valueConsumer.accept(0f);
//        }
//    }
//
//    private float clampValue(float value) {
//        // Clamp the value to the specified range
//        return Math.max(Math.min(value, maxValue), minValue);
//    }
//
//    public void setValue(float sliderValue) {
//        var clamped = clampValue(sliderValue);
//        setText(String.valueOf(sliderValue));
//    }
//}
