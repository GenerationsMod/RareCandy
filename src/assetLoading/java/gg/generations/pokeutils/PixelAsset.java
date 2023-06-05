package gg.generations.pokeutils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import gg.generations.pokeutils.ModelConfig.MaterialReference.MaterialReferenceTypeAdapter;
import gg.generations.pokeutils.ModelConfig.MaterialReference.SolidMaterialReference;
import gg.generations.pokeutils.ModelConfig.MaterialReference.TransparentMaterialReference;
import gg.generations.pokeutils.util.RuntimeTypeAdapterFactory;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.jetbrains.annotations.Nullable;
import org.tukaani.xz.XZInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Pixelmon Asset (.pk) file.
 */
public class PixelAsset {
    public static final Gson GSON = new GsonBuilder().registerTypeAdapter(ModelConfig.MaterialReference.class, new MaterialReferenceTypeAdapter())
            .create();

    public final Map<String, byte[]> files = new HashMap<>();
    private ModelConfig config;
    public String modelName;

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

            if(files.containsKey("config.json")) {
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
