package gg.generations.rarecandy.pokeutils.resource;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FolderAsset implements ResourceLocator {
    private final Path base;
    private final String name;

    public FolderAsset(Path base) {
        this(base, null);
    }

    public FolderAsset(Path base, String name) {
        this.base = base;
        this.name = name != null ? name : base.getFileName().toString();
    }

    @Override
    public void putFile(String key, byte[] fileBytes) throws IOException {
        Files.write(base.resolve(key), fileBytes);
    }

    @Override
    public void renameFile(String key, String newKey) throws IOException {
        Files.move(base.resolve(key), base.resolve(newKey));
    }

    @Override
    public void save() throws IOException {

    }

    @Override
    public void save(Path path) throws IOException {
        if(Files.notExists(path)) {
            Files.createDirectories(path);
        }

        FileUtils.copyDirectory(base.toFile(), path.toFile());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public byte[] getFile(String key) throws IOException {
        return Files.readAllBytes(base.resolve(key));
    }

    @Override
    public Set<String> getFileNames() throws IOException {
        return Files.walk(base, 1).skip(1).map(Path::getFileName).map(Path::toString).collect(Collectors.toSet());
    }

    @Override
    public boolean hasFile(String key) {
        return Files.exists(base.resolve(key));
    }

    @Override
    public InputStream getInputStream(String key) throws IOException {
        return Files.newInputStream(base.resolve(key));
    }

    @Override
    public boolean deleteFile(String key) {
        try {
            return Files.deleteIfExists(base.resolve(key));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}