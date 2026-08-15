package gg.generations.rarecandy.pokeutils.resource;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public interface ResourceLocator extends ResourceReader, ResourceWriter {
    static ResourceLocator of(Path path) throws IOException {
        if(Files.isDirectory(path)) return new FolderAsset(path);

        var file = path.getFileName().toString();

        if(file.endsWith(".zip")) return new ZipResourceLocator(path);
        else if(file.endsWith(".pk")) return new PkResourceLocator(path);

        else throw new IllegalStateException("Not a supported format.");
    }
}
