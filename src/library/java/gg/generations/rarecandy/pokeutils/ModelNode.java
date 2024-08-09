package gg.generations.rarecandy.pokeutils;

import gg.generations.rarecandy.assimp.AIMatrix4x4;
import gg.generations.rarecandy.assimp.AINode;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for animations to find transformations for all bones
 */
public class ModelNode {
    public final String name;
    public final ModelNode parent;
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

    @Override
    public String toString() {
        return "Joint{" + "name='" + name + '\'' + '}';
    }

    public static ModelNode create(AINode aiRoot) {
        return new ModelNode(aiRoot, null);
    }


    public static Matrix4f from(AIMatrix4x4 aiMat4) {
        return new Matrix4f()
                .m00(aiMat4.a1()).m10(aiMat4.a2()).m20(aiMat4.a3()).m30(aiMat4.a4())
                .m01(aiMat4.b1()).m11(aiMat4.b2()).m21(aiMat4.b3()).m31(aiMat4.b4())
                .m02(aiMat4.c1()).m12(aiMat4.c2()).m22(aiMat4.c3()).m32(aiMat4.c4())
                .m03(aiMat4.d1()).m13(aiMat4.d2()).m23(aiMat4.d3()).m33(aiMat4.d4());
    }
}
