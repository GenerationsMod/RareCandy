package gg.generations.pokeutils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.jetbrains.annotations.Nullable;
import org.tukaani.xz.XZInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Pixelmon Asset (.pk) file.
 */
public class PixelAsset {
    public static final Gson GSON = new GsonBuilder().registerTypeHierarchyAdapter(ModelConfig.MaterialReference.class, new ModelConfig.Serializer())
            .create();

    public final Map<String, byte[]> files = new HashMap<>();
    public String modelName;
    private ModelConfig config;

    public PixelAsset(String modelName, byte[] glbFile) {
        this.modelName = modelName;
        files.put(modelName, glbFile);
    }

    public PixelAsset(InputStream is, @Nullable String debugName) {
        try {
            var tarFile = getTarFile(Objects.requireNonNull(is, "Input Stream is null"));

            for (var entry : tarFile.getEntries()) {
                if (entry.getName().endsWith(".glb")) this.modelName = entry.getName();

                files.put(entry.getName(), tarFile.getInputStream(entry).readAllBytes());
            }

            if (files.containsKey("config.json")) {
                config = GSON.fromJson(new InputStreamReader(new ByteArrayInputStream(files.get("config.json"))), ModelConfig.class);
            }

        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Failed to load " + debugName, e);
        }

        updateSettings();
    }

    public void updateSettings() {
    }

    private TarFile getTarFile(InputStream inputStream) {
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
}
