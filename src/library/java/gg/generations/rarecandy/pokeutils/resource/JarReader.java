package gg.generations.rarecandy.pokeutils.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public record JarReader() implements ResourceReader {
    @Override
    public String getName() {
        return "";
    }

    @Override
    public byte[] getFile(String key) throws IOException {
        return new byte[0];
    }

    @Override
    public Set<String> getFileNames() throws IOException {
        return Set.of();
    }

    @Override
    public boolean hasFile(String key) {
        return false;
    }

    @Override
    public InputStream getInputStream(String key) throws IOException {
        return null;
    }
}
