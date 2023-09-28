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
import java.util.List;
import java.util.Optional;

import static gg.generations.rarecandy.LoggerUtil.printError;

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

            files = files.stream().filter(path -> !(path.toString().endsWith("fbx") || path.toString().endsWith("dae"))).toList();

            try (var xzWriter = new XZOutputStream(Files.newOutputStream(output), OPTIONS)) {
                try (var tarWriter = new TarArchiveOutputStream(xzWriter)) {
                    for (var archiveFile : files) {
                        processFileName(archiveFile, relativeFolder.resolve(output.getFileName().toString().substring(0, output.getFileName().toString().length() - 3))).ifPresent(name -> {
                            try {
                                var entry = new TarArchiveEntry(archiveFile, name);
                                tarWriter.putArchiveEntry(entry);
                                if (Files.isRegularFile(archiveFile)) {
                                    try (var is = new BufferedInputStream(Files.newInputStream(archiveFile))) {
                                        IOUtils.copy(is, tarWriter);
                                    }
                                }

                                tarWriter.closeArchiveEntry();
                            } catch (IOException e) {
                                printError(e);
                            }
                        });
                    }
                }
            }
        } catch (IOException e) {
            printError(e);
        }
    }

    //TODO: Add more animations
    private static Optional<String> processFileName(Path archiveFile, Path relativeFolder) {
        var fileName = relativeFolder.relativize(archiveFile).getFileName().toString();
        if (fileName.startsWith("pm") && !fileName.endsWith("png")) {
            var cleanName = fileName.substring("pmxxxx_xx_xx_xxxxx_".length()).replace(".tranm", "").replace(".gfbanm", "");

            return Optional.of(switch (cleanName) {
                case "defaultwait01_loop" -> "idle";
                case "battlewait01_loop" -> "battle_idle";
                case "walk01_loop" -> "walk";
                case "rest01_start" -> "rest_start";
                case "rest01_loop" -> "rest_loop";
                case "rest01_end" -> "rest_end";
                case "roar01" -> "roar";
                case "attack02" -> "attack";
                case "damage02" -> "damage";
                case "sleep01_loop" -> "sleep";
                case "down01_start" -> "faint";

                default -> "invalid_" + cleanName;
            } + ".tranm").filter(a -> !a.startsWith("invalid"));
        }
        return Optional.of(fileName);
    }

    public static void main(String[] args) throws IOException {
        var inFolder = Paths.get("converter/in");
        var outFolder = Paths.get("converter/out");

        Files.createDirectories(inFolder);
        Files.createDirectories(outFolder);

        Files.list(inFolder).forEach(path -> {
            if ((Files.isDirectory(path) || path.toString().endsWith(".glb"))) {
                try {
                    var relativePath = inFolder.relativize(path);
                    var outputPath = outFolder.resolve(relativePath).getParent().resolve(path.getFileName().toString().replace(".glb", "") + ".pk");

                    if (path.toString().endsWith(".glb")) {
                        convertToPk(inFolder, List.of(path), outputPath);
                    } else {
                        convertToPk(inFolder, Files.walk(path).toList(), outputPath);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}