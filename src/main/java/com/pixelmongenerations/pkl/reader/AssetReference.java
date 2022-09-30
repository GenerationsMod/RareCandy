package com.pixelmongenerations.pkl.reader;

import java.io.InputStream;

/**
 * Allows you to specify an asset without making it load
 */
public record AssetReference(InputStream is, Type type) implements AutoCloseable {

    @Override
    public void close() throws Exception {
        is.close();
    }

    public enum Type {
        GLB, PK
    }
}
