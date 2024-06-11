//package gg.generations.modelconfigviewer;
//
//import org.joml.Vector3f;
//
//import javax.swing.*;
//import javax.swing.tree.DefaultMutableTreeNode;
//import java.awt.*;
//import java.util.function.Consumer;
//
//public class Vector3fComponent extends JPanel implements ModelConfigTree.ComponentProvider {
//    private final IntegerProvider red;
//    private final IntegerProvider green;
//    private final IntegerProvider blue;
//    private final JLabel text;
//    private Vector3f vector3f;
//    private final Consumer<Vector3f> updater;
//
//    public Vector3fComponent(Vector3f vector3f, Consumer<Vector3f> updater) {
//
//        this.vector3f = vector3f;
//        this.updater = updater;
//        red = new IntegerProvider("Red", (int) (vector3f.x() * 255), 0, 255,  aFloat -> update(0, aFloat));
//        green = new IntegerProvider("Green", (int) (vector3f.y() * 255), 0, 255, aFloat -> update(1, aFloat));
//        blue = new IntegerProvider("Blue", (int) (vector3f.z() * 255), 0, 255, aFloat -> update(2, aFloat));
//        var color = color(vector3f.x, vector3f.y, vector3f.z);
//        text = new JLabel(color);
//        text.setText(color);
//        text.setForeground(Color.decode(color));
//        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
//        add(red);
//        add(green);
//        add(blue);
//        add(text);
//    }
//
//    private void update(int component, int val) {
//        var value = val/255f;
//        vector3f.setComponent(component, value);
//        updater.accept(vector3f);
//
//        var color = color(vector3f.x, vector3f.y, vector3f.z);
//
//        text.setText(color);
//        text.setForeground(Color.decode(color));
//    }
//
//    private static float rgbTosRGB(float value) {
//        return (float) Math.pow(value, 1/2.2);
//    }
//    public static String color(float red, float green, float blue) {
//        return Integer.toHexString(new Color((int) (rgbTosRGB(red) * 255), (int) (rgbTosRGB(green) * 255), (int) (rgbTosRGB(blue) * 255)).getRGB()).replaceFirst("ff", "#");
//    }
//
//    @Override
//    public Component getComponent() {
//        return this;
//    }
//}
