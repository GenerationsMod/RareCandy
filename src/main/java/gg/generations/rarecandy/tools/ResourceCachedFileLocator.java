package gg.generations.rarecandy.tools;

import com.thebombzen.jxlatte.JXLDecoder;
import com.thebombzen.jxlatte.JXLOptions;
import gg.generationsmod.rarecandy.FileLocator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ResourceCachedFileLocator implements FileLocator {

    private final Map<String, byte[]> fileCache = new HashMap<>();
    private final Path root;

    public ResourceCachedFileLocator(Path root) {
        this.root = root;
    }

    @Override
    public byte[] getFile(String name) {
        return fileCache.computeIfAbsent(name, s -> {
            try {
                return Files.readAllBytes(root.resolve(name));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public BufferedImage read(byte[] imageBytes) throws IOException {
        var options = new JXLOptions();
        options.hdr = JXLOptions.HDR_OFF;
        options.threads = 2;
        var reader = new JXLDecoder(new ByteArrayInputStream(imageBytes), options);
        var image = reader.decode();
        return image.asBufferedImage();
    }

    @Override
    public BufferedImage readImage(String name) {
        try {
            var is = Files.newInputStream(root.resolve(name));
            var image = name.endsWith(".jxl") ? read(is.readAllBytes()) : ImageIO.read(is);
            int height = image.getHeight();
            int width = image.getWidth();

            // Mirror image if not square. TODO: maybe do this in the shader to save gpu memory and upload time in general
            if (height / width == 2) {
                var mirror = new BufferedImage(width * 2, height, BufferedImage.TYPE_INT_ARGB);
                for (int y = 0; y < height; y++) {
                    for (int lx = 0, rx = width * 2 - 1; lx < width; lx++, rx--) {
                        int p = image.getRGB(lx, y);
                        mirror.setRGB(lx, y, p);
                        mirror.setRGB(rx, y, p);
                    }
                }

                image = mirror;
            }

            return image;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}