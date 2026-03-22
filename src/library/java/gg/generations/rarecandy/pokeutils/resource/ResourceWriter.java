package gg.generations.rarecandy.pokeutils.resource;

import java.io.IOException;
import java.nio.file.Path;

public interface ResourceWriter {
    void putFile(String key, byte[] fileBytes) throws IOException;
    boolean deleteFile(String key);
    void renameFile(String key, String newKey) throws IOException;
    void save() throws IOException;
    void save(Path path) throws IOException;
}