package gg.generations.rarecandy.pokeutils.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public interface ResourceReader {
    String getName();
    byte[] getFile(String key) throws IOException;
    Set<String> getFileNames() throws IOException;
    boolean hasFile(String key);
    InputStream getInputStream(String key) throws IOException;
}