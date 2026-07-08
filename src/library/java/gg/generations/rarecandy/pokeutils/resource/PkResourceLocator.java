package gg.generations.rarecandy.pokeutils.resource;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PkResourceLocator implements ResourceLocator {
    private final Map<String, byte[]> entries = new HashMap<>();
    private final String name;
    private final Path path;

    public PkResourceLocator() throws IOException {
        this.name = "";
        this.path = null;
    }

    public PkResourceLocator(Path path) throws IOException {
        this.name = path.getFileName().toString();
        this.path = path;

        if(Files.exists(path)) {

            try (SevenZFile file = new SevenZFile(path.toFile())) {
                SevenZArchiveEntry entry;
                while ((entry = file.getNextEntry()) != null) {
                    if (!entry.isDirectory()) {
                        entries.put(entry.getName(), file.getInputStream(entry).readAllBytes());
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void putFile(String key, byte[] fileBytes) {
        entries.put(key, fileBytes);
    }

    @Override
    public boolean deleteFile(String key) {
        return entries.remove(key) != null;
    }

    @Override
    public void renameFile(String key, String newKey) {
        byte[] value = entries.remove(key);
        if (value != null) {
            entries.put(newKey, value);
        }
    }

    @Override
    public byte[] getFile(String key) throws IOException {
        if (!entries.containsKey(key)) throw new IOException("Entry not found: " + key);
        return entries.get(key);
    }

    @Override
    public Set<String> getFileNames() {
        return entries.keySet();
    }

    @Override
    public boolean hasFile(String key) {
        return entries.containsKey(key);
    }

    @Override
    public InputStream getInputStream(String key) throws IOException {
        return new ByteArrayInputStream(getFile(key));
    }

    @Override
    public void save(Path path) throws IOException {
        if(Files.notExists(path)) {
            Files.createFile(path);
        }

        try (var out = new SevenZOutputFile(path.toFile())) {
            for (var entry : entries.entrySet()) {
                var e = out.createArchiveEntry(new File(entry.getKey()), entry.getKey());
                out.putArchiveEntry(e);
                out.write(entry.getValue());
                out.closeArchiveEntry();
            }
        }
    }

    public void save() throws IOException {
        if(path != null) save(path);
    }
}
