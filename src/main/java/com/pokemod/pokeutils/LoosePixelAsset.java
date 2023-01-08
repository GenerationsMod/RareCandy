package com.pokemod.pokeutils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public class LoosePixelAsset extends PixelAsset {

    public LoosePixelAsset(Path root, Path glbFile, Path... animations) {
        super(root.resolve(glbFile).getFileName().toString(), readPath(root.resolve(glbFile)));
        files.putAll(Arrays.stream(animations).collect(Collectors.toMap(path -> root.resolve(path).getFileName().toString(), o -> readPath(root.resolve(o)))));
    }

    private static byte[] readPath(Path glbFile) {
        try {
            return Files.readAllBytes(glbFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + glbFile, e);
        }
    }
}
