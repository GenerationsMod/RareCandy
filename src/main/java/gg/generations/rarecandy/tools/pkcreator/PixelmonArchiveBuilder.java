package gg.generations.rarecandy.tools.pkcreator;

import com.google.gson.JsonObject;
import gg.generations.rarecandy.pokeutils.IModelConfig;
import gg.generations.rarecandy.pokeutils.resource.PkResourceLocator;
import gg.generations.rarecandy.pokeutils.resource.ResourceLocator;
import gg.generations.rarecandy.pokeutils.resource.ResourceReader;
import gg.generations.rarecandy.pokeutils.resource.ResourceWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static gg.generations.rarecandy.renderer.LoggerUtil.printError;

/**
 * Utility for writing and reading Pixelmon: Generation's model format.
 */
public class PixelmonArchiveBuilder {
    public static void convertToPk(ResourceReader source, ResourceWriter dest, Float scale) throws IOException {
        for (var key : source.getFileNames()) {
            var bytes = source.getFile(key);

            if (scale != null && key.equals("config.json")) {
                var config = IModelConfig.GSON.fromJson(new String(bytes), JsonObject.class);
                config.addProperty("scale", scale);
                bytes = IModelConfig.GSON.toJson(config).getBytes();
            }

            dest.putFile(key, bytes);
        }

        dest.save();
    }

    public static void main(String[] args) throws IOException {
        var inFolder = Paths.get("converter/in");
        var outFolder = Paths.get("converter/out");

        Files.createDirectories(inFolder);
        Files.createDirectories(outFolder);

        Files.list(inFolder).forEach(path -> {
            if (Files.isDirectory(path) || path.toString().endsWith(".glb")) {
                try {
                    var relativePath = inFolder.relativize(path);
                    var outputPath = outFolder.resolve(relativePath).getParent().resolve(path.getFileName().toString().replace(".glb", "") + ".pk");

                    var source = ResourceLocator.of(path);
                    var dest = new PkResourceLocator(outputPath);

                    convertToPk(source, dest, null);
                    dest.save(outputPath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}