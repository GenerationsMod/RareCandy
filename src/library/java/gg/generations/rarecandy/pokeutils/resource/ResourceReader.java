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

    default String readAsString(String value) throws IOException {
        return new String(getFile(value));
    }

    enum DummyReader implements ResourceReader {
        INSTANCE;

        @Override
        public String getName() {
            return "dummy";
        }

        @Override
        public byte[] getFile(String key) throws IOException {
            throw new IOException("This is a dummy reader");
        }

        @Override
        public Set<String> getFileNames() throws IOException {
            throw new IOException("This is a dummy reader");
        }

        @Override
        public boolean hasFile(String key) {
            return true;
        }

        @Override
        public InputStream getInputStream(String key) throws IOException {
            throw new IOException("This is a dummy reader");
        }

        @Override
        public String readAsString(String value) throws IOException {
            return value;
        }
    }
}