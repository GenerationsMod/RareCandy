package com.pixelmongenerations.pkl.reader;

import com.pixelmongenerations.pkl.scene.Scene;
import org.apache.commons.compress.archivers.tar.TarFile;

import java.io.IOException;

public interface FileReader {

    Scene read(TarFile file) throws IOException;
}
