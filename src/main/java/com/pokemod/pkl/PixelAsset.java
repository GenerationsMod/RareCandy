package com.pokemod.pkl;

import org.apache.commons.compress.archivers.tar.TarFile;
import org.tukaani.xz.XZ;
import org.tukaani.xz.XZInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Pixelmon Asset (.pk) file.
 */
public class PixelAsset {

    public final Map<String, byte[]> files = new HashMap<>();
    public String modelName;

    public PixelAsset(InputStream is) {
        try {
            var tarFile = getTarFile(Objects.requireNonNull(is, "Input Stream is null"));

            for (var entry : tarFile.getEntries()) {
                if (entry.getName().endsWith(".glb")) {
                    this.modelName = entry.getName();
                }

                files.put(entry.getName(), tarFile.getInputStream(entry).readAllBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load scene", e);
        }
    }

    private TarFile getTarFile(InputStream inputStream) {
        try {
            var xzInputStream = new XZInputStream(inputStream);
            return new TarFile(xzInputStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file.", e);
        }
    }

    public byte[] getModelFile() {
        return files.get(modelName);
    }
}
