package gg.generations.pokeutils.reader;

import com.thebombzen.jxlatte.JXLDecoder;
import com.thebombzen.jxlatte.JXLOptions;
import de.javagl.jgltf.model.image.PixelData;
import gg.generations.pokeutils.util.ImageUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;

public record TextureReference(PixelData data, String name) {
    public static TextureReference read(byte[] imageBytes, String name) throws IOException {
        BufferedImage pixelData;
        if (name.endsWith("jxl")) {
            var options = new JXLOptions();
            options.hdr = JXLOptions.HDR_OFF;
            options.threads = 2;
            pixelData = new JXLDecoder(new ByteArrayInputStream(imageBytes), options).decode().asBufferedImage();
        } else {
            pixelData = ImageIO.read(new ByteArrayInputStream(imageBytes));
        }

        return new TextureReference(read(pixelData), name);
    }

    public static PixelData read(BufferedImage textureImage) {
        if (textureImage == null) {
//            ImageUtils.warning("Could not read image from image data");
            return null;
        }

        ByteBuffer pixelDataARGB = ImageUtils.getImagePixelsARGB(textureImage, false);
        int width = textureImage.getWidth();
        int height = textureImage.getHeight();
        return new gg.generations.pokeutils.util.DefaultPixelData(width, height, pixelDataARGB);
    }
}
