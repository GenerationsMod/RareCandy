package gg.generationsmod.rarecandy;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;

/**
 * Method to find and locate files based on the name of the file.
 */
public interface FileLocator {

    List<String> getFiles();

    byte[] getFile(String name);

    Path getPath();

    /**
     * Expects a Native Byte Buffer
     */
    default BufferedImage readImage(String name) {
        return null;
    }
}
