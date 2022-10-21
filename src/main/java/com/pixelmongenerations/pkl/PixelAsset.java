package com.pixelmongenerations.pkl;

import com.pixelmongenerations.pkl.reader.AssetType;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.tukaani.xz.XZ;
import org.tukaani.xz.XZInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Pixelmon Asset (.pk) file.
 */
public class PixelAsset {

    public byte[] modelFile;

    public PixelAsset(InputStream is, AssetType type) {
        try {
            switch (type) {
                case PK -> {
                    var tarFile = getTarFile(Objects.requireNonNull(is, "Input Stream is null"));

                    for (var entry : tarFile.getEntries()) {
                        if (entry.getName().endsWith(".glb")) {
                            this.modelFile = tarFile.getInputStream(entry).readAllBytes();
                        }
                    }
                }

                case GLB -> this.modelFile = Objects.requireNonNull(is, "Input Stream is null").readAllBytes();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load scene", e);
        }
    }

    private TarFile getTarFile(InputStream inputStream) {
        try {
            var unlockedArchive = unlockArchive(inputStream.readAllBytes());
            var xzInputStream = new XZInputStream(unlockedArchive);
            return new TarFile(xzInputStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file.", e);
        }
    }

    /**
     * We change 1 bit to make file readers fail to load the file or find its format. I would rather not have reforged digging through the assets, honestly.
     */
    private InputStream unlockArchive(byte[] originalBytes) {
        System.arraycopy(XZ.HEADER_MAGIC, 0, originalBytes, 0, XZ.HEADER_MAGIC.length);
        return new ByteArrayInputStream(originalBytes);
    }
}
