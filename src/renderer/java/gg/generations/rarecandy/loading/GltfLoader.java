package gg.generations.rarecandy.loading;

import de.javagl.jgltf.model.AccessorModel;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.MeshModel;
import de.javagl.jgltf.model.image.PixelDatas;
import de.javagl.jgltf.model.io.GltfModelReader;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import gg.generations.pokeutils.DataUtils;
import gg.generations.pokeutils.reader.TextureReference;
import gg.generations.rarecandy.arceus.model.GltfVertexData;
import gg.generations.rarecandy.arceus.model.Model;
import gg.generations.rarecandy.arceus.model.lowlevel.*;
import gg.generations.rarecandy.legacy.model.misc.SolidMaterial;
import gg.generations.rarecandy.legacy.pipeline.ShaderProgram;
import gg.generations.rarecandy.loading.gltf.VariantModel;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class GltfLoader {
    private static final GltfModelReader reader = new GltfModelReader();


    public static VariantModel load(InputStream is, ShaderProgram program, @Nullable BiFunction<Model, String, VariantModel.ModelSet> function) {
        var gltf = read(is);

        if(gltf.getSceneModels().size() > 1 && gltf.getMeshModels().size() > 1) throw new RuntimeException();

        List<SolidMaterial> materials = new ArrayList<>();

        for(var mat : gltf.getMaterialModels()) {
            var material = (MaterialModelV2) mat;

            TextureReference reference;

            reference = new TextureReference(PixelDatas.create(material.getBaseColorTexture().getImageModel().getImageData()), material.getBaseColorTexture().getImageModel().getName());
            materials.add(new SolidMaterial(material.getName(), reference));
        }

        MeshModel meshModel = gltf.getMeshModels().get(0);

        List<VariantModel.ModelSet> list = new ArrayList<>();

        meshModel.getMeshPrimitiveModels().forEach(a -> {
            Model model = new Model(new RenderData(DrawMode.fromGlType(a.getMode()), createVertexData(a.getAttributes()), DataUtils.makeDirect(a.getIndices().getBufferViewModel().getBufferViewData()), IndexType.fromGlType(a.getIndices().getComponentType()), a.getIndices().getCount()), program);

            list.add(new VariantModel.ModelSet(model, Map.of("default", materials.stream().filter(mat -> a.getMaterialModel().getName().equals(mat.getMaterialName())).findAny().orElse(materials.get(0))), Map.of("default", true)));
        });

        return new VariantModel(list);
    }

    private static VertexData createVertexData(Map<String, AccessorModel> attributesMap) {
        return new GltfVertexData(attributesMap);
    }

    public static GltfModel read(byte[] path) throws IOException {
        return read(new ByteArrayInputStream(path));
    }

    public static GltfModel read(Path path) throws IOException {
        return read(Files.newInputStream(path));
    }
    private static GltfModel read(InputStream path) {
        try {
            return reader.readWithoutReferences(path);
        } catch (IOException e) {
            throw new RuntimeException("Issue reading GLTF Model", e);
        }
    }
}
