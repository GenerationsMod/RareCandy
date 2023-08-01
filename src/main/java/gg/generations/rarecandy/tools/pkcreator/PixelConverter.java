package gg.generations.rarecandy.tools.pkcreator;

import dev.thecodewarrior.binarysmd.formats.SMDBinaryReader;
import dev.thecodewarrior.binarysmd.formats.SMDTextReader;
import dev.thecodewarrior.binarysmd.formats.SMDTextWriter;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.msgpack.core.MessagePack;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

import java.io.*;
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
            if(path.toString().endsWith("smdx")) {
                if (!Files.isDirectory(path) && !path.equals(inFolder)) {
                    var relativePath = inFolder.relativize(path);
                    var outputPath = outFolder.resolve(relativePath).getParent().resolve(path.getFileName().toString().replace(".smdx", ".smd"));
                    convertToSmd(path, outputPath);
                }
            } else if(path.toString().endsWith(".pk")) {
                if (!Files.isDirectory(path) && !path.equals(inFolder)) {
                    var relativePath = inFolder.relativize(path);
                    var outputPath = outFolder.resolve(relativePath).getParent().resolve(path.getFileName().toString().replace(".pk", ""));
                    unpackPk(path, outputPath);
                }
            }
        });
    }

    private static void unpackPk(Path path, Path outputPath) {
        try (var xzReader = new XZInputStream(Files.newInputStream(path))) {
            try (var tarReader = new TarArchiveInputStream(xzReader)) {
                extractTarArchive(tarReader, outputPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void extractTarArchive(TarArchiveInputStream tarIn, Path targetFolderPath) throws IOException {
        TarArchiveEntry entry;
        while ((entry = tarIn.getNextTarEntry()) != null) {
            Path outputFile = targetFolderPath.resolve(entry.getName());
            if (entry.isDirectory()) {
                Files.createDirectories(outputFile);
            } else {
                Path parent = outputFile.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }

                try (OutputStream outputStream = Files.newOutputStream(outputFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = tarIn.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
            }
        }
    }

    private static void convertToSmd(Path path, Path outputPath) {
        try {
            Files.writeString(outputPath, new SMDTextWriter().write(new SMDBinaryReader().read(MessagePack.newDefaultUnpacker(Files.newInputStream(path)))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}