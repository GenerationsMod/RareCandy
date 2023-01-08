package com.pokemod.pokeutils;

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
import java.util.List;

/**
 * Utility for writing and reading Pixelmon: Generation's model format.
 */
public class PixelmonArchiveBuilder {
    private static final LZMA2Options OPTIONS = new LZMA2Options();

    public static void convertToPk(Path relativeFolder, List<Path> files, Path output) {
        try {
            if (!Files.exists(output)) {
                Files.createDirectories(output.getParent());
                Files.createFile(output);
            }

            try (var xzWriter = new XZOutputStream(Files.newOutputStream(output), OPTIONS)) {
                try (var tarWriter = new TarArchiveOutputStream(xzWriter)) {
                    for (var archiveFile : files) {
                        var entry = new TarArchiveEntry(archiveFile, relativeFolder.relativize(archiveFile).toString());
                        tarWriter.putArchiveEntry(entry);
                        if (Files.isRegularFile(archiveFile)) {
                            try (var is = new BufferedInputStream(Files.newInputStream(archiveFile))) {
                                IOUtils.copy(is, tarWriter);
                            }
                        }
                        tarWriter.closeArchiveEntry();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        var inFolder = Paths.get("converter/in");
        var outFolder = Paths.get("converter/out");

        Files.createDirectories(inFolder);
        Files.createDirectories(outFolder);

        Files.list(inFolder).forEach(path -> {
            if (Files.isDirectory(path) && !path.equals(inFolder)) {
                try {
                    var relativePath = inFolder.relativize(path);
                    var outputPath = outFolder.resolve(relativePath).getParent().resolve(path.getFileName().toString() + ".pk");
                    convertToPk(inFolder, Files.walk(path).toList(), outputPath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}