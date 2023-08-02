package gg.generations.rarecandy.tools.swsh;

import gg.generations.rarecandy.tools.gui.DialogueUtils;
import org.lwjgl.util.nfd.NativeFileDialog;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class EyeTexture {

    public static void main(String[] args) throws IOException {
        NativeFileDialog.NFD_Init();
        var chosenFile = DialogueUtils.chooseFolder();

        if(chosenFile == null) {
            System.out.println("Didn't select a folder");
            return;
        }

        for (TexturePair texturePair : findFilePairs(chosenFile)) {
            BufferedImage expandedTextureA = processEye(texturePair.eyeTexture);
            BufferedImage textureB = processIris(texturePair.irisTexture);
            textureB.getGraphics().drawImage(expandedTextureA, 0, 0, null);

            var path = chosenFile.resolve((texturePair.type.isEmpty() ? "" : texturePair.type + "-") + "eyes.png");

            ImageIO.write(textureB, "PNG", path.toFile());
        }
    }

    private static BufferedImage processIris(Path irisPath) {
        try {
            BufferedImage texture = ImageIO.read(irisPath.toFile());
            BufferedImage flipped = horizontalFlip(texture);

            var width = texture.getWidth();
            var height = texture.getHeight();

            BufferedImage tiled = new BufferedImage(width * 4, height * 4, BufferedImage.TYPE_INT_ARGB);

            var graphics = tiled.getGraphics();

            for (int x = 0; x < 4; x++) {
                var image = x % 2 == 0 ? texture : flipped;

                for (int y = 0; y < 4; y++) {
                    graphics.drawImage(image, x * width, y * height, null);
                }
            }

            return tiled;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static BufferedImage processEye(Path eyePath) {
        try {
            BufferedImage eyeTexture = ImageIO.read(eyePath.toFile());
            int width = eyeTexture.getWidth();
            int height = eyeTexture.getHeight();

            // Calculate the width of each section based on the expanded width
            int cWidth = width / 2;


            // Create an empty texture with a width that is twice the width of Texture A and the same height
            BufferedImage expandedTexture = new BufferedImage(width * 2, height, BufferedImage.TYPE_INT_ARGB);

            // Paste column -1 at position (0, 0, cWidth, cWidth * 4)
            expandedTexture.getGraphics().drawImage(horizontalFlip(eyeTexture.getSubimage(0, 0, cWidth, height)), 0, 0, null);

            // Paste column 0 at position (cWidth, 0, cWidth * 2, cWidth * 4)
            expandedTexture.getGraphics().drawImage(eyeTexture, cWidth, 0, cWidth * 2, height, null);

            // Paste column 1 at position (cWidth * 3, 0, cWidth * 2, cWidth * 4)
            expandedTexture.getGraphics().drawImage(horizontalFlip(eyeTexture.getSubimage(cWidth, 0, cWidth, height)), 3 * cWidth, 0, null);

            return expandedTexture;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BufferedImage horizontalFlip(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Create an affine transformation for horizontal flip
        AffineTransform flip = AffineTransform.getScaleInstance(-1, 1);
        flip.translate(-width, 0);

        // Create a copy of the original image with the transformation applied
        BufferedImage flippedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = flippedImage.createGraphics();
        g.drawImage(image, flip, null);
        g.dispose();

        return flippedImage;
    }

    public static record TexturePair(String type, Path irisTexture, Path eyeTexture) {
    }

    private static List<TexturePair> findFilePairs(Path folderPath) throws IOException {
        List<Path> irisPaths = new ArrayList<>();
        List<Path> eyePaths = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath)) {
            for (Path file : stream) {
                String fileName = file.getFileName().toString();
                if (fileName.endsWith("iris.png")) {
                    irisPaths.add(file);
                    System.out.println(fileName);
                } else if (fileName.endsWith("eye.png")) {
                    eyePaths.add(file);
                    System.out.println(fileName);
                }
            }
        }

        List<TexturePair> filePairs = new ArrayList<>();
        for (Path irisPath : irisPaths) {
            String baseName = getBaseName(irisPath.getFileName().toString());
            Path eyePath = findMatchingEyePath(eyePaths, baseName);
            if (eyePath != null) {
                filePairs.add(new TexturePair(baseName, irisPath, eyePath));
            }
        }

        return filePairs;
    }

    private static String getBaseName(String fileName) {
        int extensionIndex = fileName.lastIndexOf('.');
        if (extensionIndex > 0) {
            fileName = fileName.substring(0, extensionIndex);
        }

        // Check for "-iris" or "-eye" suffix
        if (fileName.endsWith("-iris")) {
            return fileName.substring(0, fileName.length() - 5);
        } else if (fileName.endsWith("-eye")) {
            return fileName.substring(0, fileName.length() - 4);
        }

        return fileName;
    }

    private static String getTypeFromBaseName(String baseName) {
        int dashIndex = baseName.lastIndexOf('-');
        if (dashIndex >= 0) {
            return baseName.substring(0, dashIndex);
        }
        return ""; // If no type prefix, return empty string
    }

    private static Path findMatchingEyePath(List<Path> eyePaths, String baseName) {
        for (Path eyePath : eyePaths) {
            String eyeFileName = eyePath.getFileName().toString();
            if (getBaseName(eyeFileName).equals(baseName)) {
                return eyePath;
            }
        }
        return null;
    }
}