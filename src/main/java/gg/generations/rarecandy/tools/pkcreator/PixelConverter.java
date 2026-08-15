package gg.generations.rarecandy.tools.pkcreator;

import dev.thecodewarrior.binarysmd.formats.SMDBinaryReader;
import dev.thecodewarrior.binarysmd.formats.SMDTextWriter;
import gg.generations.rarecandy.pokeutils.resource.ResourceLocator;
import org.msgpack.core.MessagePack;

import java.io.IOException;
import java.nio.file.*;

import static gg.generations.rarecandy.renderer.LoggerUtil.printError;

/**
 * Utility for writing and reading Pixelmon: Generation's model format.
 */
public class PixelConverter {

    public static void main(String[] args) throws IOException {
        Path inFolder = Paths.get("converter/in");
        Path outFolder = Paths.get("converter/out");

        Files.createDirectories(inFolder);
        Files.createDirectories(outFolder);


        Files.walk(inFolder).forEach(path -> {
            var relativePath = inFolder.relativize(path);
            var outputPath = outFolder.resolve(relativePath).getParent();
            if(!path.equals(inFolder)) {
                if (!Files.isDirectory(path)) {
                    if (path.toString().endsWith("smdx")) {

                        outputPath = outputPath.resolve(path.getFileName().toString().replace(".smdx", ".smd"));
                        convertToSmd(path, outputPath);

                    } else if (path.toString().endsWith(".pk")) {

                        outputPath = outputPath.resolve(path.getFileName().toString().replace(".pk", ""));
                        unpackPk(path, outputPath);
                    }
                } else {
                    outputPath = outputPath.resolve(path.getFileName() + ".pk");

                    unpackPk(path, outputPath);
                }
            }
        });
    }

    public static void unpackPk(Path path, Path outputPath) {
        try {
            ResourceLocator input = ResourceLocator.of(path);
            ResourceLocator output = ResourceLocator.of(outputPath);

            for(var file : input.getFileNames()) {
                output.putFile(file, input.getFile(file));
            }

            output.save();
        } catch (Exception e) {
            System.out.println("Issue: " + path);
            e.printStackTrace();
        }
    }

    private static void convertToSmd(Path path, Path outputPath) {
        try {
            Files.writeString(outputPath, new SMDTextWriter().write(new SMDBinaryReader().read(MessagePack.newDefaultUnpacker(Files.newInputStream(path)))));
        } catch (IOException e) {
            printError(e);
        }
    }
}