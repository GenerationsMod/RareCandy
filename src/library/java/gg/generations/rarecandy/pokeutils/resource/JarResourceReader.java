package gg.generations.rarecandy.pokeutils.resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class JarResourceReader implements ResourceReader {

    private final String subfolder;
    private final Class<?> contextClass;

    public JarResourceReader(String subfolder, Class<?> contextClass) {
        if (subfolder != null) {
            if (subfolder.startsWith("/")) subfolder = subfolder.substring(1);
            if (subfolder.endsWith("/")) subfolder = subfolder.substring(0, subfolder.length() - 1);
        }
        this.subfolder = (subfolder == null || subfolder.isEmpty()) ? "" : subfolder;
        this.contextClass = contextClass != null ? contextClass : getClass();
    }

    public static ResourceReader create(String path, Class<?> contextClass) {
        return new JarResourceReader(path, contextClass);
    }

    @Override
    public String getName() {
        return "jar";
    }

    private String resolvePath(String key) {
        String cleanKey = key.startsWith("/") ? key.substring(1) : key;
        if (subfolder.isEmpty()) {
            return "/" + cleanKey;
        }
        return "/" + subfolder + "/" + cleanKey;
    }

    @Override
    public InputStream getInputStream(String key) throws IOException {
        String absolutePath = resolvePath(key);
        InputStream is = contextClass.getResourceAsStream(absolutePath);
        if (is == null) {
            throw new FileNotFoundException("Resource not found inside JAR subfolder [" + subfolder + "]: " + key);
        }
        return is;
    }

    @Override
    public byte[] getFile(String key) throws IOException {
        try (InputStream is = getInputStream(key)) {
            return is.readAllBytes();
        }
    }

    @Override
    public boolean hasFile(String key) {
        String absolutePath = resolvePath(key);
        return contextClass.getResource(absolutePath) != null;
    }

    @Override
    public Set<String> getFileNames() throws IOException {
        ClassLoader loader = contextClass.getClassLoader();
        if (loader == null) loader = ClassLoader.getSystemClassLoader();

        Set<String> fileNames = new HashSet<>();

        Enumeration<URL> roots = loader.getResources(subfolder);
        while (roots.hasMoreElements()) {
            URL root = roots.nextElement();
            try {
                collect(root.toURI(), fileNames);
            } catch (URISyntaxException | ProviderNotFoundException e) {
                throw new IOException("Failed to resolve classpath folder [" + subfolder + "] at " + root, e);
            }
        }

        return fileNames;
    }

    private void collect(URI uri, Set<String> fileNames) throws IOException {
        if ("jar".equals(uri.getScheme())) {
            FileSystem fileSystem;
            boolean owned = false;
            try {
                fileSystem = FileSystems.getFileSystem(uri);
            } catch (FileSystemNotFoundException e) {
                fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
                owned = true;
            }

            try {
                walk(fileSystem.getPath("/" + subfolder), fileNames);
            } finally {
                if (owned) fileSystem.close();
            }
            return;
        }

        if ("file".equals(uri.getScheme())) {
            walk(Paths.get(uri), fileNames);
        }
    }

    private void walk(Path targetFolder, Set<String> fileNames) throws IOException {
        if (!Files.isDirectory(targetFolder)) return;

        try (Stream<Path> walk = Files.walk(targetFolder)) {
            walk.filter(Files::isRegularFile)
                    .filter(path -> !path.getFileName().toString().endsWith(".class"))
                    .forEach(path -> fileNames.add(targetFolder.relativize(path).toString().replace("\\", "/")));
        }
    }
}
