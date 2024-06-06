package gg.generations.rarecandy.pokeutils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.tukaani.xz.XZInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Pixelmon Asset (.pk) file.
 */
public class PixelAsset {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapter(VariantParent.class, new VariantParent.Serializer())
            .registerTypeAdapter(MaterialReference.class, new MaterialReference.Serializer())
            .registerTypeAdapter(Vector2f.class, (JsonDeserializer<Vector2f>) (json, type, context) -> {
                var vec = new Vector2f();
                if (json.isJsonArray())
                    if (json.getAsJsonArray().size() == 2)
                        vec.set(json.getAsJsonArray().get(0).getAsFloat(), json.getAsJsonArray().get(1).getAsFloat());
                return vec;
            })
            .create();

    public final Map<String, byte[]> files = new HashMap<>();
    public String modelName;
    public String name;
    protected ModelConfig config;

    public PixelAsset(String modelName, byte[] glbFile) {
        this.name = modelName;
        this.modelName = modelName;
        files.put(modelName, glbFile);
    }

    public PixelAsset(Path path) {
        this(path, path.getFileName().toString() + ".pk");
    }

    public PixelAsset(Path path, String name) {
        this.name = name;
        load(path);
    }

    public void load(Path dir) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (!Files.isDirectory(entry)) {

                    if (entry.getFileName().toString().endsWith(".glb"))
                        this.modelName = entry.getFileName().toString();

                    try {
                        files.put(entry.getFileName().toString(), Files.readAllBytes(entry));
                    } catch (IOException e) {

                    }
                }
            }

            if (files.containsKey("config.json")) {
                config = GSON.fromJson(new InputStreamReader(new ByteArrayInputStream(files.get("config.json"))), ModelConfig.class);
            }

            updateSettings();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PixelAsset(InputStream is, @Nullable String debugName) {
        this.name = debugName;

        try {
            var tarFile = getTarFile(Objects.requireNonNull(is, "Input Stream is null"));

            for (var entry : tarFile.getEntries()) {
                if (entry.getName().endsWith("/")) continue;

                if (entry.getName().endsWith(".glb")) this.modelName = entry.getName();

                files.put(entry.getName(), tarFile.getInputStream(entry).readAllBytes());
            }

            if (files.containsKey("config.json")) {
                config = GSON.fromJson(new InputStreamReader(new ByteArrayInputStream(files.get("config.json"))), ModelConfig.class);
            }

            updateSettings();
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Failed to load " + debugName, e);
        }

    }

    public void updateSettings() {
    }

    public static TarFile getTarFile(InputStream inputStream) {
        try {
            var xzInputStream = new XZInputStream(inputStream);
            return new TarFile(xzInputStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file.", e);
        }
    }

    public byte[] getModelFile() {
        return files.get(modelName);
    }

    public List<Map.Entry<String, byte[]>> getAnimationFiles() {
        return files.entrySet().stream().filter(a -> a.getKey().endsWith("smd")).toList();
    }

    public List<Map.Entry<String, byte[]>> getImageFiles() {
        return files.entrySet().stream().filter(a -> {
            var key = a.getKey();

            return key.endsWith("jxl") || key.endsWith("jpg") || key.endsWith("png");
        }).toList();
    }


    public ModelConfig getConfig() {
        return config;
    }

    public byte[] get(String key) {
        return this.files.get(key);
    }
}
