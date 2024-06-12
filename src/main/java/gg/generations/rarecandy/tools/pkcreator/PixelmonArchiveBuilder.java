package gg.generations.rarecandy.tools.pkcreator;

import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.tukaani.xz.LZMA2Options;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static gg.generations.rarecandy.renderer.LoggerUtil.printError;

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

            try (var sevenZOutput = new SevenZOutputFile(output.toFile())) {
                for (var file : files) {
                    processFileName(file, relativeFolder.resolve(output.getFileName().toString().substring(0, output.getFileName().toString().length() - 3))).ifPresent(name -> {
                        try {
                            var entry = sevenZOutput.createArchiveEntry(file, name);
                            sevenZOutput.putArchiveEntry(entry);
                            if (Files.isRegularFile(file)) {
                                try (var is = new BufferedInputStream(Files.newInputStream(file))) {
                                    byte[] buffer = new byte[1024];
                                    int length;
                                    while ((length = is.read(buffer)) > 0) {
                                        sevenZOutput.write(buffer, 0, length);
                                    }
                                }
                            }

                            sevenZOutput.closeArchiveEntry();
                        } catch (IOException e) {
                            printError(e);
                        }
                    });
                }
                sevenZOutput.finish();
            }
        } catch (IOException e) {
            printError(e);
        }
    }

    //TODO: Add more animations
    private static Optional<String> processFileName(Path archiveFile, Path relativeFolder) {
        var fileName = relativeFolder.relativize(archiveFile).getFileName().toString();
//        if (fileName.startsWith("pm") && !fileName.endsWith("png")) {
//            var cleanName = fileName.substring("pmxxxx_xx_xx_xxxxx_".length()).replace(".tranm", "").replace(".gfbanm", "");
//
//            return Optional.of(switch (cleanName) {
//                case "defaultwait01_loop" -> "idle";
//                case "battlewait01_loop" -> "battle_idle";
//                case "walk01_loop" -> "walk";
//                case "rest01_start" -> "rest_start";
//                case "rest01_loop" -> "rest_loop";
//                case "rest01_end" -> "rest_end";
//                case "roar01" -> "roar";
//                case "attack02" -> "attack";
//                case "damage02" -> "damage";
//                case "sleep01_loop" -> "sleep";
//                case "down01_start" -> "faint";
//
//                default -> "invalid_" + cleanName;
//            } + ".tranm").filter(a -> !a.startsWith("invalid"));
//        }
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