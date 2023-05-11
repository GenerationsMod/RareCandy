package gg.generations.rarecandy.tools.pkcreator;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility for writing and reading Pixelmon: Generation's model format.
 */
public class PixelConverter {
    private static final LZMA2Options OPTIONS = new LZMA2Options();

    public static void convertToPk(Path pkFile, Path output) {
        try {
            if (!Files.exists(output)) {
                Files.createDirectories(output.getParent());
                Files.createFile(output);
            }

            try (var xzWriter = new XZOutputStream(Files.newOutputStream(output), OPTIONS)) {
                try (var tarWriter = new TarArchiveOutputStream(xzWriter)) {
                    tarWriter.putArchiveEntry(new TarArchiveEntry(pkFile, pkFile.getFileName().toString()));
                    IOUtils.copy(new BufferedInputStream(Files.newInputStream(pkFile)), tarWriter);
                    tarWriter.closeArchiveEntry();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Path inFolder = Paths.get("converter/in");
        Path outFolder = Paths.get("converter/out");

        Files.createDirectories(inFolder);
        Files.createDirectories(outFolder);


        Files.walk(inFolder).forEach(path -> {
            if (!Files.isDirectory(path) && !path.equals(inFolder)) {
                var relativePath = inFolder.relativize(path);
                var outputPath = outFolder.resolve(relativePath).getParent().resolve(path.getFileName().toString().replace(".glb", ".pk"));
                convertToPk(path, outputPath);
            }
        });
    }
}