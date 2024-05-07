package gg.generations.rarecandy.pokeutils.reader;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class BufferedImageViewer implements Runnable {

    private BufferedImage image;

    public BufferedImageViewer(BufferedImage image) {
        this.image = image;
    }

    @Override
    public void run() {
        JFrame frame = new JFrame("Image Viewer");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        JLabel label = new JLabel(new ImageIcon(image));
        frame.add(label, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    public void launchViewer() {
        SwingUtilities.invokeLater(this);
    }

    // Usage example
    public static void main(String[] args) {
        // Load your BufferedImage here
        BufferedImage image = null; // Load your image here

        // Create and launch the viewer on a separate thread
        BufferedImageViewer viewer = new BufferedImageViewer(image);
        Thread viewerThread = new Thread(viewer);
        viewerThread.start();
    }
}