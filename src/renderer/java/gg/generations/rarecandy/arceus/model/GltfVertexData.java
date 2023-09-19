package gg.generations.rarecandy.arceus.model;

import de.javagl.jgltf.model.AccessorModel;
import gg.generations.pokeutils.DataUtils;
import gg.generations.rarecandy.arceus.model.lowlevel.VertexData;
import org.lwjgl.opengl.GL20;

import java.util.Map;

public class GltfVertexData extends VertexData {
    public GltfVertexData(Map<String, AccessorModel> attributes) {
        super();

        var position = attributes.get("POSITION");
        DataUtils.bindArrayBuffer(position.getBufferViewModel());
        vertexAttribPointer(position, 0);

        var uvs = attributes.get("TEXCOORD_0");
        DataUtils.bindArrayBuffer(uvs.getBufferViewModel());
        vertexAttribPointer(uvs, 1);

        var normal = attributes.get("NORMAL");
        DataUtils.bindArrayBuffer(normal.getBufferViewModel());
        vertexAttribPointer(normal, 2);

        var joints = attributes.get("JOINTS_0");

        if (joints != null) {

            DataUtils.bindArrayBuffer(joints.getBufferViewModel());
            vertexAttribPointer(joints, 3);

            var weights = attributes.get("WEIGHTS_0");
            DataUtils.bindArrayBuffer(weights.getBufferViewModel());
            vertexAttribPointer(weights, 4);
        }
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

}
