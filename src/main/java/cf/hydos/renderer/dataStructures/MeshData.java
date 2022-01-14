package cf.hydos.renderer.dataStructures;

import java.util.List;

/**
 * This object contains all the mesh data for an animated model that is to be loaded into the VAO.
 */
public class MeshData {

    private final List<Vertex> vertices;
    private final float[] textureCoords;
    private final float[] normals;
    private final int[] indices;
    private final int[] jointIds;
    private final float[] vertexWeights;

    public MeshData(List<Vertex> vertices, float[] textureCoords, float[] normals, int[] indices, int[] jointIds, float[] vertexWeights) {
        this.vertices = vertices;
        this.textureCoords = textureCoords;
        this.normals = normals;
        this.indices = indices;
        this.jointIds = jointIds;
        this.vertexWeights = vertexWeights;
    }

    public int[] getJointIds() {
        return jointIds;
    }

    public float[] getVertexWeights() {
        return vertexWeights;
    }

    @Deprecated
    public float[] getVertices() {
        float[] rawVertices = new float[vertices.size() * 3];
        for (int i = 0; i < vertices.size(); i++) {
            Vertex vertex = this.vertices.get(i);
            rawVertices[i* 3] = vertex.getPosition().x;
            rawVertices[i * 3 + 1] = vertex.getPosition().y;
            rawVertices[i * 3 + 2] = vertex.getPosition().z;
        }
        return rawVertices;
    }

    public float[] getTextureCoords() {
        return textureCoords;
    }

    public float[] getNormals() {
        return normals;
    }

    public int[] getIndices() {
        return indices;
    }

    public int getVertexCount() {
        return vertices.size() / 3;
    }
}
