package gg.generations.modelconfigviewer;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public class FloatTextField extends JTextField {
    private final Consumer<Float> valueConsumer;

    public FloatTextField(float value, Consumer<Float> valueConsumer) {
        super();
        setHorizontalAlignment(JTextField.RIGHT);
        this.valueConsumer = valueConsumer;
        this.setText(String.valueOf(value));

        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!(Character.isDigit(c) || c == '.')) {
                    e.consume();
                }
            }
        });

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                completeAndSetValue();
            }
        });
    }

    private void completeAndSetValue() {
        String text = getText();
        if (!text.isEmpty()) {
            // Check if there is a missing '0' before or after the decimal point
            if (text.startsWith(".")) {
                text = "0" + text;
            }
            if (text.endsWith(".")) {
                text = text + "0";
            }

            try {
                float value = Float.parseFloat(text);
                valueConsumer.accept(value);
            } catch (NumberFormatException e) {
                // Invalid float value, set to 0
                valueConsumer.accept(0f);
            }
        } else {
            valueConsumer.accept(0f);
        }
    }
}
