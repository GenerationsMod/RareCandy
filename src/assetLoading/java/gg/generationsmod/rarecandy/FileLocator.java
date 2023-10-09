package gg.generationsmod.rarecandy;

import java.awt.image.BufferedImage;

/**
 * Method to find and locate files based on the name of the file.
 */
@FunctionalInterface
public interface FileLocator {

    byte[] getFile(String name);

    /**
     * Expects a Native Byte Buffer
     */
    default BufferedImage readImage(String name) {
        return null;
    }
}
