package gg.generations.rarecandy.tools.pixelmonTester;

import gg.generations.rarecandy.pokeutils.reader.TextureReference;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public class TextureTest {
    public static void main(String[] args) throws IOException {
        var path = Path.of("D:\\Git Repos\\RareCandy\\run\\converter\\in\\alcremie\\toppings.png");

        displayImage(TextureReference.read(Files.readAllBytes(path), path.getFileName().toString()));
    }

    public static void displayImage(TextureReference reference) {
        displayImage(reference.data().getWidth(), reference.data().getHeight(), reference.data().getPixelsRGBA(), reference.name());
    }

    public static void displayImage(int width, int height, ByteBuffer pixelData, String imageName) {
        // Create a new BufferedImage with the specified width and height
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] imagePixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < imagePixels.length; i++) {
            int argb = pixelData.getInt(i * 4); // Assuming 4 bytes per pixel (ARGB)
            imagePixels[i] = argb;
        }

        // Create JFrame to display the image
        JFrame frame = new JFrame("Image Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ImageIcon imageIcon = new ImageIcon(image);
        JLabel label = new JLabel(imageIcon);
        frame.getContentPane().add(label);
        frame.pack();
        frame.setVisible(true);
    }


//        // Retrieve the pixel data from the ByteBuffer and set it in the BufferedImage
//        int[] pixelArray = new int[width * height];
//        pixelData.asIntBuffer().get(pixelArray);
//        image.setRGB(0, 0, width, height, pixelArray, 0, width);
//
//        // Create a JFrame to display the image
//        JFrame frame = new JFrame();
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setTitle(imageName);
//        frame.setSize(width, height);
//
//        // Create a custom JPanel to draw the image
//        JPanel panel = new JPanel() {
//            @Override
//            protected void paintComponent(Graphics g) {
//                super.paintComponent(g);
//                g.drawImage(image, 0, 0, null);
//            }
//        };
//
//        // Add the panel to the frame and make it visible
//        frame.add(panel);
//        frame.setVisible(true);
//    }
}
