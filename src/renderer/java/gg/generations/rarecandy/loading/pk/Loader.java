package gg.generations.rarecandy.loading.pk;

import gg.generations.pokeutils.PixelAsset;
import gg.generations.rarecandy.arceus.model.Model;
import gg.generations.rarecandy.legacy.model.misc.ITexture;
import gg.generations.rarecandy.legacy.model.misc.Material;
import gg.generations.pokeutils.util.ResourceLocation;
import gg.generations.rarecandy.legacy.pipeline.ShaderProgram;
import gg.generations.rarecandy.loading.GltfLoader;
import gg.generations.rarecandy.loading.gltf.VariantModel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Loader {
    public static VariantModel load(ResourceLocation path, Function<String, ShaderProgram> program) throws IOException {
        var asset = new PixelAsset(Files.newInputStream(Path.of(path.path())), null);

        if(asset.getConfig() != null) {
            var images = new HashMap<String, ResourceLocation>();
            for (Map.Entry<String, byte[]> entry : asset.getImageFiles()) {
                var location = new ResourceLocation(path.modid(), path.path() + "/" + entry);

                ITexture texture = ITexture.generate(entry.getKey(), entry.getValue());

                TextureRegistery.register(location, texture);

                images.put(entry.getKey(), location);
            }

            var materials = asset.getConfig().materials.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, a -> Material.create(a.getValue(), images)));

            BiFunction<Model, String, VariantModel.ModelSet> function = (model, name) -> {
                var materialMap = new HashMap<String, Material>();
                var hiddenMap = new HashMap<String, Boolean>();


                asset.getConfig().variants.get(name).forEach((k, v) -> {
                    materialMap.put(k, materials.get(v.material()));
                    hiddenMap.put(k, v.hide());
                });

                return new VariantModel.ModelSet(model, materialMap, hiddenMap);
            };

            return GltfLoader.load(new ByteArrayInputStream(asset.getModelFile()), program, function);
        } else {
            throw new RuntimeException("Model %s lacks a config.json");
        }
    }
}
