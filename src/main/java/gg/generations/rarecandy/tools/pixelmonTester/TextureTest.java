package gg.generations.rarecandy.tools.pixelmonTester;

import gg.generations.pokeutils.reader.TextureReference;
import gg.generations.rarecandy.tools.gui.DialogueUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

public class TextureTest {
    public static void main(String[] args) throws IOException {
        var path = Path.of("C:\\Users\\water\\Desktop\\blep.jxl");

        if(path != null) displayImage(TextureReference.read(Files.readAllBytes(path), path.getFileName().toString()));
    }

    public static void displayImage(TextureReference reference) {
        displayImage(reference.data().getWidth(), reference.data().getHeight(), reference.data().getPixelsRGBA(), reference.name());
    }

    public static void displayImage(int width, int height, ByteBuffer pixelData, String imageName) {
        // Create a new BufferedImage with the specified width and height
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Retrieve the pixel data from the ByteBuffer and set it in the BufferedImage
        int[] pixelArray = new int[width * height];
        pixelData.asIntBuffer().get(pixelArray);
        image.setRGB(0, 0, width, height, pixelArray, 0, width);

        // Create a JFrame to display the image
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle(imageName);
        frame.setSize(width, height);

        // Create a custom JPanel to draw the image
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, null);
            }
        };

        // Add the panel to the frame and make it visible
        frame.add(panel);
        frame.setVisible(true);
    }
}
