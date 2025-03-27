package gg.generations.rarecandy.pokeutils;

import com.google.gson.*;
import gg.generations.rarecandy.pokeutils.codec.JsonIo;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.jetbrains.annotations.Nullable;
import org.tukaani.xz.XZInputStream;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static gg.generations.rarecandy.renderer.LoggerUtil.printError;

/**
 * Pixelmon Asset (.pk) file.
 */
public class PixelAsset {
    public final Map<String, byte[]> files = new HashMap<>();
    public String modelName;
    public String name;
    private ModelConfig config;

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

    public PixelAsset(Map<String, byte[]> map, String name) {
        this.name = name;

        for (var entry : map.entrySet()) {
            if(entry.getKey().endsWith(".glb")) {
                this.modelName = entry.getKey();
            } else if(entry.getKey().equals("config.json")) {

                config = JsonIo.read(ModelConfig.CODEC, entry.getValue());
            }

            files.put(entry.getKey(), entry.getValue());
        }



        updateSettings();

    }

    public static PixelAsset open(Path path) {
        return new PixelAsset(getSevenZipFile(path), null);
    }

    public static void save(Path path, PixelAsset pk) {
        var files = pk.files;
        files.put("config.json", JsonIo.write(ModelConfig.CODEC, pk.config));

        save(path, files);
    }

    public static void save(Path path, Map<String, byte[]> files) {
        try (var sevenZOutput = new SevenZOutputFile(path.toFile())) {
            for (var pair : files.entrySet()) {
                var name = pair.getKey();
                var data = pair.getValue();
                try {
                    // Create an archive entry
                    var entry = sevenZOutput.createArchiveEntry(new File(name), name);
                    sevenZOutput.putArchiveEntry(entry);

                    // Handle file input stream
                    try (BufferedInputStream is = new BufferedInputStream(new ByteArrayInputStream(data))) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = is.read(buffer)) > 0) {
                            sevenZOutput.write(buffer, 0, length);
                        }
                    }

                    sevenZOutput.closeArchiveEntry();
                } catch (IOException e) {
                    printError(e);
                }
            }
            sevenZOutput.finish();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void load(Path dir) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (!Files.isDirectory(entry)) {

                    if (entry.getFileName().toString().endsWith(".glb")) this.modelName = entry.getFileName().toString();

                    try {
                        files.put(entry.getFileName().toString(), Files.readAllBytes(entry));
                    } catch (IOException e) {
                        throw new RuntimeException();
                    }
                }
            }

            if (files.containsKey("config.json")) {
                config = JsonIo.read(ModelConfig.CODEC, files.get("config.json"));
            }

            updateSettings();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PixelAsset of(Path path, @Nullable String debugName) {
        if(Files.isRegularFile(path)) {
            return new PixelAsset(getSevenZipFile(path), debugName);
        } else {
            return new PixelAsset(path, debugName);
        }
    }

    public PixelAsset(InputStream is, @Nullable String debugName) {
        this(getSevenZipFile(is), debugName);
    }

    public PixelAsset(SevenZFile is, @Nullable String debugName) {
        this.name = debugName;

        try {
            for (var entry : is.getEntries()) {
                if(entry.getName().endsWith("/")) continue;

                if (entry.getName().endsWith(".glb")) this.modelName = entry.getName();

                files.put(entry.getName(), is.getInputStream(entry).readAllBytes());
            }

            if (files.containsKey("config.json")) {
                config = JsonIo.read(ModelConfig.CODEC, files.get("config.json"));
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

    public static SevenZFile getSevenZipFile(InputStream stream) {
        try {
            return new SevenZFile(new SeekableInMemoryByteChannel(stream.readAllBytes()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file.", e);
        }
    }

    public static SevenZFile getSevenZipFile(Path path) {
        try {
            return new SevenZFile(path.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file. %s".formatted(path), e);
        }
    }


    public byte[] getModelFile() {
        return files.get(modelName);
    }

    public List<Map.Entry<String, byte[]>> getImageFiles() {
        return files.entrySet().stream().filter(a -> a.getKey().endsWith("png") || a.getKey().endsWith("dds")).toList();
    }


    public ModelConfig getConfig() {
        return config;
    }

    public byte[] get(String key) {
        return this.files.get(key);
    }

    public record GenericJsonThing<T>(BiFunction<T, JsonSerializationContext, JsonElement> serializer, BiFunction<JsonElement, JsonDeserializationContext, T> deserializer) implements JsonSerializer<T>, JsonDeserializer<T> {


        @Override
        public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return deserializer.apply(json, context);
        }

        @Override
        public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
            return serializer.apply(src, context);
        }
    }

}
