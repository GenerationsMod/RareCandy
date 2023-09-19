package gg.generations.rarecandy.loading.pk;

import gg.generations.pokeutils.Pair;
import gg.generations.pokeutils.PixelAsset;
import gg.generations.pokeutils.reader.TextureReference;
import gg.generations.rarecandy.arceus.model.Model;
import gg.generations.rarecandy.legacy.model.misc.Material;
import gg.generations.rarecandy.legacy.model.misc.SolidMaterial;
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
import java.util.stream.Collectors;

public class Loader {
    public static VariantModel load(Path path, ShaderProgram program) throws IOException {
        var asset = new PixelAsset(Files.newInputStream(path), null);

        BiFunction<Model, String, VariantModel.ModelSet> function = null;

        if(asset.getConfig() != null) {
            var images = asset.getImageFiles().stream().map(a -> new Pair<>(a.getKey(), TextureReference.read(a.getValue(), a.getKey()))).collect(Collectors.toMap(Pair::a, Pair::b));
            var materials = asset.getConfig().materials.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, a -> new SolidMaterial(images.get(a.getKey()))));

            function = (model, name) -> {
                var materialMap = new HashMap<String, Material>();
                var hiddenMap = new HashMap<String, Boolean>();

                asset.getConfig().variants.get(name).forEach((k, v) -> {
                    materialMap.put(k, materials.get(v.material()));
                    hiddenMap.put(k, v.hide());
                });

                return new VariantModel.ModelSet(model, materialMap, hiddenMap);
            };
        }

        GltfLoader.load(new ByteArrayInputStream(asset.getModelFile()), program, function);

        return null;
    }
}
