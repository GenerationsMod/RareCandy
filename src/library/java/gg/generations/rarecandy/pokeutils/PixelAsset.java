package gg.generations.rarecandy.pokeutils;

import com.google.gson.*;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.tukaani.xz.XZInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Pixelmon Asset (.pk) file.
 */
public class PixelAsset {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient()
            .registerTypeAdapter(VariantParent.class, new VariantParent.Serializer())
            .registerTypeAdapter(MaterialReference.class, new MaterialReference.Serializer())
            .registerTypeAdapter(Vector2f.class, (JsonDeserializer<Vector2f>) (json, typeOfT, context) -> {
                var vec = new Vector2f();
                if (json.isJsonArray()) {
                    if (json.getAsJsonArray().size() == 2) {
                        vec.set(json.getAsJsonArray().get(0).getAsFloat(), json.getAsJsonArray().get(1).getAsFloat());
                    }
                }

                return vec;
            })
            .registerTypeAdapter(Vector3f.class, new GenericJsonThing<Vector3f>((json, ctx) -> {
                var array =  new JsonArray();
                array.add(json.x);
                array.add(json.y);
                array.add(json.z);
                return array;
            }, (json, ctx) -> {
                var vec = new Vector3f();
                if (json.isJsonArray()) {
                    if (json.getAsJsonArray().size() == 3) {
                        vec.set(json.getAsJsonArray().get(0).getAsFloat(), json.getAsJsonArray().get(1).getAsFloat(), json.getAsJsonArray().get(2).getAsFloat());
                    }
                }

                return vec;
            }))
            .registerTypeAdapter(Quaternionf.class, new GenericJsonThing<Quaternionf>((json, ctx) -> {
                var array =  new JsonArray();
                array.add(json.x);
                array.add(json.y);
                array.add(json.z);
                array.add(json.w);
                return array;
            }, (json, ctx) -> {
                var vec = new Quaternionf();
                if (json.isJsonArray()) {
                    if (json.getAsJsonArray().size() == 3) {
                        vec.rotationXYZ(json.getAsJsonArray().get(0).getAsFloat(), json.getAsJsonArray().get(1).getAsFloat(), json.getAsJsonArray().get(2).getAsFloat());
                    } else if (json.getAsJsonArray().size() == 4) {
                        vec.set(json.getAsJsonArray().get(0).getAsFloat(), json.getAsJsonArray().get(1).getAsFloat(), json.getAsJsonArray().get(2).getAsFloat(), json.getAsJsonArray().get(3).getAsFloat());
                    }
                }

                return vec;
            }))
            .registerTypeAdapter(MeshOptions.class, new GenericJsonThing<MeshOptions>((meshOptions, ctx) -> {
                var json = new JsonObject();
                json.addProperty("invert", meshOptions.invert());
                return json;
            }, (json, ctx) -> {
                if (json.isJsonPrimitive()) return new MeshOptions(json.getAsBoolean());
                else {
                    var invert = json.getAsJsonObject().getAsJsonPrimitive("invert").getAsBoolean();
                    return new MeshOptions(invert);
                }
            }))
            .registerTypeAdapter(SkeletalTransform.class, new GenericJsonThing<SkeletalTransform>(new BiFunction<SkeletalTransform, JsonSerializationContext, JsonElement>() {
                @Override
                public JsonElement apply(SkeletalTransform skeletalTransform, JsonSerializationContext jsonSerializationContext) {
                    var obj = new JsonObject();
                    var position = skeletalTransform.position();
                    if (position.x != 0 || position.y != 0 || position.z != 0) {
                        obj.add("position", jsonSerializationContext.serialize(position));
                    }
                    var rotation = skeletalTransform.rotation();
                    if (rotation.x != 0 || rotation.y != 0 || rotation.z != 0 || rotation.w != 0) {
                        obj.add("rotation", jsonSerializationContext.serialize(rotation));
                    }
                    return obj;
                }
            }, (element, context) -> {
                var obj = element.getAsJsonObject();

                var position = new Vector3f();

                if(obj.has("position")) {
                    position = context.deserialize(obj.get("position"), Vector3f.class);
                }

                var rotation = new Quaternionf();

                if(obj.has("rotation =")) {
                    rotation = context.deserialize(obj.get("rotation"), Quaternionf.class);
                }

                return new SkeletalTransform(position, rotation);
            }))
            .create();

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
                var json = new String(entry.getValue());

                config = GSON.fromJson(json, ModelConfig.class);
            }

            files.put(entry.getKey(), entry.getValue());
        }



        updateSettings();

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
                config = GSON.fromJson(new InputStreamReader(new ByteArrayInputStream(files.get("config.json"))), ModelConfig.class);
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
        return files.entrySet().stream().filter(a -> a.getKey().endsWith("png")).toList();
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
