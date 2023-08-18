package gg.generations.rarecandy.tools.swsh;

import gg.generations.pokeutils.Pair;
import gg.generations.rarecandy.LoggerUtil;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gg.generations.rarecandy.LoggerUtil.print;

public class EyeTexture {

    public static void main(String[] args) throws IOException {
        NativeFileDialog.NFD_Init();
        var chosenFile = DialogueUtils.chooseFolder();

        if (chosenFile == null) {
            print("Didn't select a folder");
            return;
        }

        for (Map.Entry<String, Pair<Path, Path>> texturePair : findFilePairs(chosenFile).entrySet()) {
            BufferedImage expandedTextureA = processEye(texturePair.getValue().b());
            BufferedImage textureB = processIris(texturePair.getValue().a());
            textureB.getGraphics().drawImage(expandedTextureA, 0, 0, null);

//            var path = chosenFile.resolve((texturePair.getKey().isEmpty() ? "" : texturePair.getKey() + "-") + "eyes.png");

            ImageIO.write(textureB, "PNG", texturePair.getValue().b().toFile());
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

    protected static BufferedImage processEye(Path eyePath) {
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
            LoggerUtil.printError(e);
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

    private static Map<String, Pair<Path, Path>> findFilePairs(Path folderPath) throws IOException {
//        List<Path> irisPaths = new ArrayList<>();
//        List<Path> eyePaths = new ArrayList<>();

        Map<String, Pair<Path, Path>> filePairs = new HashMap<>();
        filePairs.put("shiny", new Pair<>(null, null));
        filePairs.put("", new Pair<>(null, null));

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath)) {
            for (Path file : stream) {
                String fileName = file.getFileName().toString();
                var pair = filePairs.get(fileName.contains("rare") ? "shiny" : "");

                if (fileName.contains("_Iris_lyc")) { //iris.png")) {
                    pair.a(file);

//                    irisPaths.add(file);
                    print(fileName);
                } else if (fileName.contains("_Eye_col")) { //.endsWith("eye.png")) {
//                    eyePaths.add(file);
                    pair.b(file);
                    print(fileName);
                }
            }
        }

//        List<TexturePair> filePairs = new ArrayList<>();
//        for (Path irisPath : irisPaths) {
//            String baseName = getBaseName(irisPath.getFileName().toString());
//            Path eyePath = findMatchingEyePath(eyePaths, baseName);
//            if (eyePath != null) {
//                filePairs.add(new TexturePair(baseName, irisPath, eyePath));
//            }
//        }

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
        } else if (fileName.equals("iris")) {
            return "";
        } else if (fileName.equals("eye")) {
            return "";
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

    public record TexturePair(String type, Path irisTexture, Path eyeTexture) {
    }
}