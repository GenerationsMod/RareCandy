package cf.hydos.pixelmonassetutils;

import cf.hydos.pixelmonassetutils.reader.InternalFileType;
import cf.hydos.pixelmonassetutils.scene.Scene;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.tukaani.xz.XZ;
import org.tukaani.xz.XZInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

/**
 * Pixelmon Asset (.pa) file.
 */
public class PixelAsset {
    private static final Random RANDOM = new Random();
    public final Scene scene;

    public PixelAsset(Path path) {
        if (!path.getFileName().toString().endsWith(".pa")) {
            System.err.println("It is recommended you name all Pixelmon Asset files with .pa");
        }

        try {
            TarFile tarFile = getTarFile(path);
            this.scene = findFormat(tarFile).reader.read(tarFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load scene", e);
        }
    }

    public PixelAsset(InputStream stream) {
        try {
            TarFile tarFile = getTarFile(stream);
            this.scene = findFormat(tarFile).reader.read(tarFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load scene", e);
        }
    }

    private InternalFileType findFormat(TarFile file) {
        InternalFileType type = null;
        for (TarArchiveEntry entry : file.getEntries()) {
            String name = entry.getName();

            if (name.endsWith(".glb")) {
                type = InternalFileType.GRAPHICS_LANGUAGE_BINARY;
            }

            if (name.endsWith(".gltf")) {
                type = InternalFileType.GRAPHICS_LANGUAGE_JSON;
            }
        }

        return type;
    }

    private TarFile getTarFile(Path path) {
        try {
            return getTarFile(Files.newInputStream(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file.", e);
        }
    }

    private TarFile getTarFile(InputStream inputStream) {
        try {
            InputStream unlockedInputStream = unlockArchive(inputStream.readAllBytes());
            XZInputStream xzInputStream = new XZInputStream(unlockedInputStream);
            return new TarFile(xzInputStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file.", e);
        }
    }

    /**
     * We change 1 bit to make file readers fail to load the file or find its format. I would rather not have reforged digging through the assets, honestly.
     */
    public static byte[] lockArchive(byte[] originalBytes) {
        originalBytes[0] = (byte) 6;
        return originalBytes;
    }

    /**
     * We change 1 bit to make file readers fail to load the file or find its format. I would rather not have reforged digging through the assets, honestly.
     */
    private InputStream unlockArchive(byte[] originalBytes) {
        System.arraycopy(XZ.HEADER_MAGIC, 0, originalBytes, 0, XZ.HEADER_MAGIC.length);
        return new ByteArrayInputStream(originalBytes);
    }
}
