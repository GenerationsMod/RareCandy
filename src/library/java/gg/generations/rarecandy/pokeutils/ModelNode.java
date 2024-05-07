package gg.generations.rarecandy.pokeutils;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.assimp.AIMatrix4x4;
import org.lwjgl.assimp.AINode;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Used for animations to find transformations for all bones
 */
public class ModelNode {
    public static Charset charset = StandardCharsets.UTF_8;

    public final String name;
    public ModelNode parent;
    public final Matrix4f transform;
    public final Vector3f posePosition;
    public final Quaternionf poseRotation;
    public final Vector3f poseScale;
    public final List<ModelNode> children = new ArrayList<>();
    public int id = -1;

    private ModelNode(AINode aiNode, ModelNode parent) {
        this.name = aiNode.mName().dataString();

        this.parent = parent;
        this.transform = from(aiNode.mTransformation());
        this.posePosition = transform.getTranslation(new Vector3f());
        this.poseRotation = transform.getUnnormalizedRotation(new Quaternionf());
        this.poseScale = transform.getScale(new Vector3f());


        for (int i = 0; i < aiNode.mNumChildren(); i++)
            children.add(new ModelNode(AINode.create(aiNode.mChildren().get(i)), this));
    }

    public ModelNode(String name, Matrix4f matrix) {
        this.name = name;
        this.transform = matrix;
        this.posePosition = transform.getTranslation(new Vector3f());
        this.poseRotation = transform.getUnnormalizedRotation(new Quaternionf());
        this.poseScale = transform.getScale(new Vector3f());
    }

    @Override
    public String toString() {
        return "Joint{" + "name='" + name + '\'' + '}';
    }

    public static ModelNode create(AINode aiRoot) {
        return new ModelNode(aiRoot, null);
    }

    public static ModelNode fromBuffer(ByteBuffer buffer) {
        var length = buffer.get();
        var nodes = new ModelNode[length];
        for (int i = 0; i < length; i++) {
            var matrix = new Matrix4f().set(buffer);
            var name = extractName(buffer);
            var node = new ModelNode(name, matrix);

            var parentId = buffer.get();
            var parent = parentId == -1 ? null : nodes[parentId];
            nodes[i] = node.setParent(parent);
        }

        return nodes[0];
    }

    public static void toBuffer(ByteBuffer buffer, ModelNode root) {
        var nodes = new ArrayList<ModelNode>();

        probe(nodes, root);


    }

    private static void probe(ArrayList<ModelNode> nodes, ModelNode root) {
        nodes.add(root);
        root.children.forEach(modelNode -> probe(nodes, modelNode));
    }


    private ModelNode setParent(ModelNode parent) {
        this.parent = parent;
        this.parent.children.add(this);
        return this;
    }

    public static String extractName(ByteBuffer buffer) {
        var length = buffer.get();
        var array = new byte[length];
        buffer.get(array);
        return new String(array);
    }

    public static Matrix4f from(AIMatrix4x4 aiMat4) {
        return new Matrix4f()
                .m00(aiMat4.a1()).m10(aiMat4.a2()).m20(aiMat4.a3()).m30(aiMat4.a4())
                .m01(aiMat4.b1()).m11(aiMat4.b2()).m21(aiMat4.b3()).m31(aiMat4.b4())
                .m02(aiMat4.c1()).m12(aiMat4.c2()).m22(aiMat4.c3()).m32(aiMat4.c4())
                .m03(aiMat4.d1()).m13(aiMat4.d2()).m23(aiMat4.d3()).m33(aiMat4.d4());
    }
}
