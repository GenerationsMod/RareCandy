package gg.generationsmod.rarecandy.model.animation;

import org.joml.Matrix4f;
import org.lwjgl.assimp.AIMatrix4x4;
import org.lwjgl.assimp.AINode;

import java.util.ArrayList;
import java.util.List;

public class BoneNode {
    public final String name;
    public final BoneNode parent;
    public final Matrix4f transform;
    public final List<BoneNode> children = new ArrayList<>();

    private BoneNode(AINode aiNode, BoneNode parent) {
        this.name = aiNode.mName().dataString();
        this.parent = parent;
        this.transform = from(aiNode.mTransformation());

        for (int i = 0; i < aiNode.mNumChildren(); i++)
            children.add(new BoneNode(AINode.create(aiNode.mChildren().get(i)), this));
    }

    @Override
    public String toString() {
        return "Joint{" + "name='" + name + '\'' + '}';
    }

    public static BoneNode create(AINode aiRoot) {
        return new BoneNode(aiRoot, null);
    }

    public static Matrix4f from(AIMatrix4x4 aiMat4) {
        return new Matrix4f().m00(aiMat4.a1()).m10(aiMat4.a2()).m20(aiMat4.a3()).m30(aiMat4.a4()).m01(aiMat4.b1()).m11(aiMat4.b2()).m21(aiMat4.b3()).m31(aiMat4.b4()).m02(aiMat4.c1()).m12(aiMat4.c2()).m22(aiMat4.c3()).m32(aiMat4.c4()).m03(aiMat4.d1()).m13(aiMat4.d2()).m23(aiMat4.d3()).m33(aiMat4.d4());
    }
}
