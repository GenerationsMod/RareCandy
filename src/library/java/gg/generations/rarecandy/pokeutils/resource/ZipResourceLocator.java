package gg.generations.rarecandy.pokeutils.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipResourceLocator implements ResourceLocator {
    private final Map<String, byte[]> entries = new HashMap<>();
    private final String name;
    private final Path path;

    public ZipResourceLocator(Path path) throws IOException {
        this.name = path.getFileName().toString();
        this.path = path;
        try (ZipFile zip = new ZipFile(path.toFile())) {
            var iter = zip.entries();
            while (iter.hasMoreElements()) {
                var entry = iter.nextElement();
                if (!entry.isDirectory()) {
                    entries.put(entry.getName(), zip.getInputStream(entry).readAllBytes());
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
        entries.put(newKey, entries.get(key));
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

    public void save() throws IOException {
        save(path);
    }

    public void save(Path path) throws IOException {
        if(Files.notExists(path)) {
            Files.createFile(path);
        }

        try (var zos = new ZipOutputStream(Files.newOutputStream(path))) {
            for (var entry : entries.entrySet()) {
                zos.putNextEntry(new ZipEntry(entry.getKey()));
                zos.write(entry.getValue());
                zos.closeEntry();
            }
        }
    }
}