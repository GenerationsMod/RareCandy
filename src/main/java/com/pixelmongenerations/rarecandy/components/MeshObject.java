package com.pixelmongenerations.rarecandy.components;

import com.pixelmongenerations.pkl.reader.TextureReference;
import com.pixelmongenerations.pkl.scene.material.Material;
import com.pixelmongenerations.rarecandy.ThreadSafety;
import com.pixelmongenerations.rarecandy.pipeline.Pipeline;
import com.pixelmongenerations.rarecandy.rendering.InstanceState;
import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.image.PixelDatas;
import me.hydos.gogoat.GLModel;
import me.hydos.gogoat.MeshDrawCommand;
import me.hydos.gogoat.util.DataUtils;
import org.lwjgl.opengl.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MeshObject extends RenderObject {

    protected GLModel glModel;

    public MeshObject(List<Material> glMaterials, Map<String, Material> variants, GLModel glModel, Pipeline pipeline) {
        ThreadSafety.assertContextThread();
        this.materials = glMaterials;
        this.variants = variants;
        this.glModel = glModel;
        this.pipeline = pipeline;
        this.ready = true;
        System.out.println("Model Ready");
    }

    public static MeshObject create(GltfModel gltfModel, Pipeline pipeline) {
        var textures = gltfModel.getTextureModels().stream().map(raw -> new TextureReference(PixelDatas.create(raw.getImageModel().getImageData()), raw.getImageModel().getName())).toList();

        for (var sceneModel : gltfModel.getSceneModels()) {
            for (var nodeModel : sceneModel.getNodeModels()) {
                var skinModel = nodeModel.getSkinModel();

                // Model Loading Method #1
                for (var meshModel : nodeModel.getMeshModels()) {
                    return processMeshModel(nodeModel, meshModel, textures, pipeline);
                }

                // Model Loading Method #2
                for (var child : nodeModel.getChildren()) {
                    for (var meshModel : child.getMeshModels()) {
                        return processMeshModel(nodeModel, meshModel, textures, pipeline);
                    }
                }

                if (skinModel != null) {
                    // Animation stuff
                }
            }
        }

        throw new RuntimeException("No Primitive Models inside of GLTF model");
    }

    private static MeshObject processMeshModel(NodeModel nodeModel, MeshModel meshModel, List<TextureReference> materials, Pipeline pipeline) {
        for (var primitiveModel : meshModel.getMeshPrimitiveModels()) {
            var glMaterials = materials.stream().map(Material::new).toList();
            var variants = glMaterials.stream().collect(Collectors.toMap(mat -> mat.getDiffuseTexture().name, mat -> mat));
            var glModel = processPrimitiveModel(nodeModel, meshModel, primitiveModel);;
            return new MeshObject(glMaterials, variants, glModel, pipeline);
        }

        return null;
    }

    private static GLModel processPrimitiveModel(NodeModel nodeModel, MeshModel meshModel, MeshPrimitiveModel primitiveModel) {
        var model = new GLModel();
        var attributes = primitiveModel.getAttributes();

        var vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        var position = attributes.get("POSITION");
        DataUtils.bindArrayBuffer(position.getBufferViewModel());
        vertexAttribPointer(position, 0);

        var uvs = attributes.get("TEXCOORD_0");
        DataUtils.bindArrayBuffer(uvs.getBufferViewModel());
        vertexAttribPointer(uvs, 1);

        var normal = attributes.get("NORMAL");
        DataUtils.bindArrayBuffer(normal.getBufferViewModel());
        vertexAttribPointer(normal, 2);

        var ebo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GL15.glBufferData(GL15C.GL_ELEMENT_ARRAY_BUFFER, DataUtils.makeDirect(primitiveModel.getIndices().getBufferViewModel().getBufferViewData()), GL15.GL_STATIC_DRAW);

        var mode = primitiveModel.getMode();
        model.meshDrawCommands.add(new MeshDrawCommand(vao, mode, primitiveModel.getIndices().getComponentType(), ebo, primitiveModel.getIndices().getCount()));

        return model;
    }

    private static void vertexAttribPointer(AccessorModel data, int binding) {
        GL20.glEnableVertexAttribArray(binding);
        GL20.glVertexAttribPointer(
                binding,
                data.getElementType().getNumComponents(),
                data.getComponentType(),
                false,
                data.getByteStride(),
                data.getByteOffset());
    }

    public void render(List<InstanceState> instances) {
        pipeline.bind();

        for (var instance : instances) {
            pipeline.updateUniforms(instance, this);
            glModel.runDrawCalls();
        }
    }
}
