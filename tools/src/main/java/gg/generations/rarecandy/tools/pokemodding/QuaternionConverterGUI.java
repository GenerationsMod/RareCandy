package gg.generations.rarecandy.tools.pokemodding;

import gg.generations.rarecandy.renderer.animation.TranmUtilExperimental;
import org.joml.Quaternionf;

import java.awt.*;
import java.awt.event.*;

import static java.awt.event.ItemEvent.SELECTED;

public class QuaternionConverterGUI {
    private Frame frame;
    private Panel inputPanel;
    private Panel inputPanel1;
    private TextField[] inputFields;
    private TextArea resultArea;
    private Button convertButton;
    private CheckboxGroup conversionGroup;
    private boolean convertQuaterions = true;

    public QuaternionConverterGUI() {
        frame = new Frame("Quaterion Swizzle Test");
        frame.setSize(500, 300);
        frame.setLayout(new GridLayout(4, 1));
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        createInputPanel();
        createResultArea();
        createConvertButton();

        frame.setVisible(true);
    }

    private void createInputPanel() {
        inputPanel = new Panel();
        inputPanel.setLayout(new GridLayout());

        Label titleLabel = new Label("Select Conversion:");

        conversionGroup = new CheckboxGroup();
        Checkbox quaternionCheckbox = new Checkbox("Quat -> Packed", conversionGroup, true);
        Checkbox packedStateCheckbox = new Checkbox("Packed -> Quat", conversionGroup, false);
        quaternionCheckbox.addItemListener(e -> {
            if(e.getStateChange() == SELECTED) {
                convertQuaterions = true;
                inputFields[3].setVisible(true);
            }
        });
        packedStateCheckbox.addItemListener(e -> {
            if(e.getStateChange() == SELECTED) {
                convertQuaterions = false;
                inputFields[3].setVisible(false);
            }
        });

        inputFields = new TextField[4];
        for (int i = 0; i < 4; i++) {
            inputFields[i] = new TextField("1", 10);
        }

        inputPanel.add(titleLabel);
        inputPanel.add(quaternionCheckbox);
        inputPanel.add(packedStateCheckbox);

        inputPanel1 = new Panel();
        inputPanel1.setLayout(new GridLayout());
        inputPanel1.setLayout(new GridLayout());
        for (TextField textField : inputFields) {
            inputPanel1.add(textField);
        }

        frame.add(inputPanel);
        frame.add(inputPanel1);
    }

    private void createResultArea() {
        resultArea = new TextArea();
        resultArea.setEditable(false);
        frame.add(resultArea);
    }

    private void createConvertButton() {
        convertButton = new Button("Convert");
        convertButton.addActionListener(e -> convertAndDisplayResult());

        frame.add(convertButton);
    }

    private void convertAndDisplayResult() {
        if (conversionGroup.getSelectedCheckbox().getLabel().equals("Quat -> Packed")) {
            convertQuaternionToPackedState();
        } else {
            convertPackedStateToQuaternion();
        }
    }

    // Slot for Quaternion to Packed State Conversion Function
    private void convertQuaternionToPackedState() {
        var text = "";

        try {
            var x = Float.parseFloat(inputFields[0].getText());
            var y = Float.parseFloat(inputFields[1].getText());
            var z = Float.parseFloat(inputFields[2].getText());
            var w = Float.parseFloat(inputFields[3].getText());

            var packed = TranmUtilExperimental.pack(new Quaternionf(x,y,z,w));
            text = "x: %s\ny: %s\nz: %s".formatted(packed[0], packed[1], packed[2]);
        } catch (Exception e) {
            e.printStackTrace();
            text = "Error! Invalid values. %s";
        }

        resultArea.setText(text);
    }

    // Slot for Packed State to Quaternion Conversion Function
    private void convertPackedStateToQuaternion() {
        var text = "";

        try {
            var x = Integer.parseInt(inputFields[0].getText());
            var y = Integer.parseInt(inputFields[1].getText());
            var z = Integer.parseInt(inputFields[2].getText());

            var quat = TranmUtilExperimental.unpack(x,y,z);
            text = "x: %s\ny: %s\nz: %s\nw: %s".formatted(quat.x, quat.y, quat.z, quat.w);
        } catch (Exception e) {
            text = "Error! Invalid values.";
            e.printStackTrace();
        }

        resultArea.setText(text);
    }

    public static void main(String[] args) {
        new QuaternionConverterGUI();
    }
}