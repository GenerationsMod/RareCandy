package com.pixelmongenerations.pkl;

import com.pixelmongenerations.pkl.reader.AssetType;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.tukaani.xz.XZ;
import org.tukaani.xz.XZInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Pixelmon Asset (.pk) file.
 */
public class PixelAsset {

    public byte[] modelFile;

    public PixelAsset(InputStream is, AssetType type) {
        try {
            switch (type) {
                case PK -> {
                    var tarFile = getTarFile(is);

                    for (var entry : tarFile.getEntries()) {
                        if (entry.getName().endsWith(".glb")) {
                            this.modelFile = tarFile.getInputStream(entry).readAllBytes();
                        }
                    }
                }

                case GLB -> this.modelFile = is.readAllBytes();
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
}
