package modelconfigviewer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageViewer extends JFrame {

    public ImageViewer(String name, BufferedImage image) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle(name);
        setSize(400, 400);
        setLocationRelativeTo(null);

        JLabel imageLabel = new JLabel(new ImageIcon(image));

        JScrollPane scrollPane = new JScrollPane(imageLabel);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        getContentPane().add(scrollPane);
    }

    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            // Load the image (replace with your own image loading logic)
//            BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
//            Graphics2D g2d = image.createGraphics();
//            g2d.setColor(Color.WHITE);
//            g2d.fillRect(0, 0, 800, 600);
//            g2d.setColor(Color.RED);
//            g2d.fillRect(200, 150, 400, 300);
//            g2d.dispose();
//
//            ImageViewer imageViewer = new ImageViewer(image);
//            imageViewer.setVisible(true);
//        });
    }
}