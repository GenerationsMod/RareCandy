package gg.generations.rarecandy.loading;

import de.javagl.jgltf.model.AccessorModel;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.MeshModel;
import de.javagl.jgltf.model.MeshPrimitiveModel;
import de.javagl.jgltf.model.io.GltfModelReader;
import gg.generations.pokeutils.DataUtils;
import gg.generations.rarecandy.arceus.model.GltfVertexData;
import gg.generations.rarecandy.arceus.model.Model;
import gg.generations.rarecandy.arceus.model.lowlevel.DrawMode;
import gg.generations.rarecandy.arceus.model.lowlevel.IndexType;
import gg.generations.rarecandy.arceus.model.lowlevel.RenderData;
import gg.generations.rarecandy.arceus.model.lowlevel.VertexData;
import gg.generations.rarecandy.legacy.pipeline.ShaderProgram;
import gg.generations.rarecandy.loading.gltf.VariantModel;

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


    public static VariantModel load(InputStream is, Function<String, ShaderProgram> program, BiFunction<Model, String, VariantModel.ModelSet> function) {
        var gltf = read(is);

        if(gltf.getSceneModels().size() > 1 && gltf.getMeshModels().size() > 1) throw new RuntimeException();

        List<VariantModel.ModelSet> list = new ArrayList<>();

        for(MeshModel meshModel : gltf.getMeshModels()) {
            String name = meshModel.getName();

            if(meshModel.getMeshPrimitiveModels().size() > 1) throw new RuntimeException(meshModel.getName() + " has more than 1 mesh");

            Model model = new Model(createRenderData(meshModel.getMeshPrimitiveModels().get(0)), program);

            list.add(function.apply(model, name));

        }

        return new VariantModel(list);
    }

    public static RenderData createRenderData(MeshPrimitiveModel meshPrimitiveModel) {
        return new RenderData(DrawMode.fromGlType(meshPrimitiveModel.getMode()), createVertexData(meshPrimitiveModel.getAttributes()), DataUtils.makeDirect(meshPrimitiveModel.getIndices().getBufferViewModel().getBufferViewData()), IndexType.fromGlType(meshPrimitiveModel.getIndices().getComponentType()), meshPrimitiveModel.getIndices().getCount());
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
