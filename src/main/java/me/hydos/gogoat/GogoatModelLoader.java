package me.hydos.gogoat;

import com.pixelmongenerations.rarecandy.animation.TransformStorage;
import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.io.GltfModelReader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class GogoatModelLoader {
    private final GltfModelReader reader = new GltfModelReader();
    protected final Map<BufferViewModel, Integer> bufferViewModelToGlBufferView = new IdentityHashMap<>();
    private List<Integer> bufferViews;
    private List<Runnable> renderCommand;

    public GogoatModelLoader(int modelLoadingThreads) {
        try {
            GltfModel gltfModel = reader.readWithoutReferences(GogoatModelLoader.class.getResourceAsStream("/pinsir-mega.glb"));
            test(gltfModel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void test(GltfModel gltfModel) {
        for (var sceneModel : gltfModel.getSceneModels()) {
            for (var nodeModel : sceneModel.getNodeModels()) {
                var skinModel = nodeModel.getSkinModel();

                for (NodeModel child : nodeModel.getChildren()) {
                    for (var meshModel : child.getMeshModels()) {
                        for (var primitiveModel : meshModel.getMeshPrimitiveModels()) {
                            processPrimitiveModel(nodeModel, meshModel, primitiveModel);
                        }
                    }
                }

                if (skinModel != null) {
                    // Animation stuff
                }
            }
        }
    }

    private void processPrimitiveModel(NodeModel nodeModel, MeshModel meshModel, MeshPrimitiveModel primitiveModel) {
        var window = new Window("Gogoat Model Viewer", 1920, 1080);
        var attributes = primitiveModel.getAttributes();
        var materialModel = primitiveModel.getMaterialModel();

        var glVertexArray = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(glVertexArray);

        var position = attributes.get("POSITION");
        bindArrayBuffer(position.getBufferViewModel());
        vertexAttribPointer(position, 0);

        var uvs = attributes.get("TEXCOORD_0");
        bindArrayBuffer(position.getBufferViewModel());
        vertexAttribPointer(position, 1);

        var normal = attributes.get("NORMAL");
        bindArrayBuffer(position.getBufferViewModel());
        vertexAttribPointer(position, 2);

        var mode = primitiveModel.getMode();
        renderCommand.add(() -> {
            GL30.glBindVertexArray(glVertexArray);
            GL11.glDrawElements(mode, 0, primitiveModel.getIndices().getAccessorData().createByteBuffer());
        });
    }

    private void vertexAttribPointer(AccessorModel data, int binding) {
        GL20.glEnableVertexAttribArray(binding);
        GL20.glVertexAttribPointer(
                binding,
                data.getElementType().getNumComponents(),
                data.getComponentType(),
                false,
                data.getByteStride(),
                data.getByteOffset());
    }

    public void bindArrayBuffer(BufferViewModel bufferViewModel) {
        var glBufferView = bufferViewModelToGlBufferView.get(bufferViewModel);

        if (glBufferView == null) {
            glBufferView = GL15.glGenBuffers();
            bufferViews.add(glBufferView);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferView);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, bufferViewModel.getBufferViewData(), GL15.GL_STATIC_DRAW);
            bufferViewModelToGlBufferView.put(bufferViewModel, glBufferView);
        }

        else GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferView);
    }

    public static void main(String[] args) {
        new GogoatModelLoader(4);
    }
}
