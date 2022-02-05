package cf.hydos.engine.components;

import cf.hydos.engine.rendering.Bone;
import cf.hydos.engine.rendering.shader.ShaderProgram;
import cf.hydos.pixelmonassetutils.assimp.AssimpUtils;
import cf.hydos.pixelmonassetutils.scene.material.Material;
import cf.hydos.pixelmonassetutils.scene.material.Texture;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.assimp.AIAnimation;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AINodeAnim;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL45C;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

@SuppressWarnings("ConstantConditions")
public class AnimatedRenderObject extends GameComponent {
    public static final long TIMER = System.currentTimeMillis();

    public Matrix4f globalInverseTransform;
    public Bone[] bones;
    public Matrix4f[] boneTransforms;
    public AINode root;
    public AIAnimation animation;
    public ShaderProgram shaderProgram;
    public Material material;
    private int vao;
    private int indexCount;

    public void addVertices(ShaderProgram program, FloatBuffer vertices, IntBuffer indices, Texture diffuseTexture) {
        this.shaderProgram = program;
        material = new Material(diffuseTexture);

        vao = GL45C.glCreateVertexArrays(); // VertexArrayObject (Vertex Layout)
        int vbo = GL45C.glCreateBuffers(); // VertexBufferObject (Vertices)
        int ebo = GL45C.glCreateBuffers(); // ElementBufferObject (Indices)
        indexCount = indices.capacity();

        GL45C.glNamedBufferData(vbo, vertices, GL45C.GL_STATIC_DRAW);
        GL45C.glNamedBufferData(ebo, indices, GL45C.GL_STATIC_DRAW);

        GL45C.glEnableVertexArrayAttrib(vao, 0);
        GL45C.glEnableVertexArrayAttrib(vao, 1);
        GL45C.glEnableVertexArrayAttrib(vao, 2);
        GL45C.glEnableVertexArrayAttrib(vao, 3);
        GL45C.glEnableVertexArrayAttrib(vao, 4);
        GL45C.glEnableVertexArrayAttrib(vao, 5);

        GL45C.glVertexArrayAttribBinding(vao, 0, 0);
        GL45C.glVertexArrayAttribFormat(vao, 0, 3, GL11C.GL_FLOAT, false, 0);

        GL45C.glVertexArrayAttribBinding(vao, 1, 0);
        GL45C.glVertexArrayAttribFormat(vao, 1, 2, GL11C.GL_FLOAT, false, 12);

        GL45C.glVertexArrayAttribBinding(vao, 2, 0);
        GL45C.glVertexArrayAttribFormat(vao, 2, 3, GL11C.GL_FLOAT, false, 20);

        GL45C.glVertexArrayAttribBinding(vao, 3, 0);
        GL45C.glVertexArrayAttribFormat(vao, 3, 3, GL11C.GL_FLOAT, false, 32);

        GL45C.glVertexArrayAttribBinding(vao, 4, 0);
        GL45C.glVertexArrayAttribFormat(vao, 4, 4, GL11C.GL_FLOAT, false, 44);

        GL45C.glVertexArrayAttribBinding(vao, 5, 0);
        GL45C.glVertexArrayAttribFormat(vao, 5, 4, GL11C.GL_FLOAT, false, 60);

        GL45C.glVertexArrayVertexBuffer(vao, 0, vbo, 0, 19 * 4);
        GL45C.glVertexArrayElementBuffer(vao, ebo);
    }

    @Override
    public void render(Matrix4f projectionMatrix, Matrix4f viewMatrix) {
        shaderProgram.bind();

        shaderProgram.uniforms.get("gBones").uploadMat4fs(boneTransforms);
        shaderProgram.updateUniforms(GetTransform(), material, projectionMatrix, viewMatrix);

        GL30C.glBindVertexArray(this.vao);
        GL11C.glDrawElements(GL11C.GL_TRIANGLES, this.indexCount, GL11C.GL_UNSIGNED_INT, 0);
    }

    @Override
    public void update() {
        boneTransforms((float) (((double) System.currentTimeMillis() - (double) TIMER) / 1000.0));
    }

    AINodeAnim FindNodeAnim(AIAnimation pAnimation, String NodeName) {
        for (int i = 0; i < pAnimation.mNumChannels(); i++) {
            AINodeAnim pNodeAnim = AINodeAnim.create(pAnimation.mChannels().get(i));

            if (pNodeAnim.mNodeName().dataString().equals(NodeName)) return pNodeAnim;
        }

        return null;
    }

    void CalcInterpolatedPosition(Vector3f Out, float AnimationTime, AINodeAnim pNodeAnim) {
        if (pNodeAnim.mNumPositionKeys() == 1) {
            Out.set(AssimpUtils.from(pNodeAnim.mPositionKeys().get(0).mValue()));
            return;
        }

        int PositionIndex = FindPosition(AnimationTime, pNodeAnim);
        int NextPositionIndex = (PositionIndex + 1);
        assert (NextPositionIndex < pNodeAnim.mNumPositionKeys());
        float DeltaTime = (float) (pNodeAnim.mPositionKeys().get(NextPositionIndex).mTime() - pNodeAnim.mPositionKeys().get(PositionIndex).mTime());
        float Factor = (AnimationTime - (float) pNodeAnim.mPositionKeys().get(PositionIndex).mTime()) / DeltaTime;
        assert (Factor >= 0.0f && Factor <= 1.0f);
        Vector3f Start = AssimpUtils.from(pNodeAnim.mPositionKeys().get(PositionIndex).mValue());
        Vector3f End = AssimpUtils.from(pNodeAnim.mPositionKeys().get(NextPositionIndex).mValue());
        Vector3f Delta = End.sub(Start);
        Out.set(Start.add(Delta.mul(Factor)));
    }


    void CalcInterpolatedRotation(Quaternionf Out, float AnimationTime, AINodeAnim pNodeAnim) {
        if (pNodeAnim.mNumRotationKeys() == 1) {
            Out.set(AssimpUtils.from(pNodeAnim.mRotationKeys().get(0).mValue()));
            return;
        }

        int RotationIndex = FindRotation(AnimationTime, pNodeAnim);
        int NextRotationIndex = (RotationIndex + 1);
        assert (NextRotationIndex < pNodeAnim.mNumRotationKeys());
        float DeltaTime = (float) (pNodeAnim.mRotationKeys().get(NextRotationIndex).mTime() - pNodeAnim.mRotationKeys().get(RotationIndex).mTime());
        float Factor = (AnimationTime - (float) pNodeAnim.mRotationKeys().get(RotationIndex).mTime()) / DeltaTime;
        assert (Factor >= 0.0f && Factor <= 1.0f);
        Quaternionf StartRotationQ = AssimpUtils.from(pNodeAnim.mRotationKeys().get(RotationIndex).mValue());
        Quaternionf EndRotationQ = AssimpUtils.from(pNodeAnim.mRotationKeys().get(NextRotationIndex).mValue());
        Out.set(StartRotationQ.slerp(EndRotationQ, Factor));
    }


    Vector3f CalcInterpolatedScaling(Vector3f Out, float AnimationTime, AINodeAnim pNodeAnim) {
        if (pNodeAnim.mNumScalingKeys() == 1) {
            return AssimpUtils.from(pNodeAnim.mScalingKeys().get(0).mValue());
        }

        int ScalingIndex = FindScaling(AnimationTime, pNodeAnim);
        int NextScalingIndex = (ScalingIndex + 1);
        assert (NextScalingIndex < pNodeAnim.mNumScalingKeys());
        float DeltaTime = (float) (pNodeAnim.mScalingKeys().get(NextScalingIndex).mTime() - pNodeAnim.mScalingKeys().get(ScalingIndex).mTime());
        float Factor = (AnimationTime - (float) pNodeAnim.mScalingKeys().get(ScalingIndex).mTime()) / DeltaTime;
        assert (Factor >= 0.0f && Factor <= 1.0f);
        Vector3f Start = AssimpUtils.from(pNodeAnim.mScalingKeys().get(ScalingIndex).mValue());
        Vector3f End = AssimpUtils.from(pNodeAnim.mScalingKeys().get(NextScalingIndex).mValue());
        Vector3f Delta = End.sub(Start);
        return Out.set(Start.add(Delta.mul(Factor)));
    }

    int FindPosition(float AnimationTime, AINodeAnim pNodeAnim) {
        for (int i = 0; i < pNodeAnim.mNumPositionKeys() - 1; i++) {
            if (AnimationTime < (float) pNodeAnim.mPositionKeys().get(i + 1).mTime()) {
                return i;
            }
        }

        return 0;
    }


    int FindRotation(float AnimationTime, AINodeAnim pNodeAnim) {
        assert (pNodeAnim.mNumRotationKeys() > 0);

        for (int i = 0; i < pNodeAnim.mNumRotationKeys() - 1; i++) {
            if (AnimationTime < (float) pNodeAnim.mRotationKeys().get(i + 1).mTime()) {
                return i;
            }
        }

        return 0;
    }


    int FindScaling(float AnimationTime, AINodeAnim pNodeAnim) {
        assert (pNodeAnim.mNumScalingKeys() > 0);

        for (int i = 0; i < pNodeAnim.mNumScalingKeys() - 1; i++) {
            if (AnimationTime < (float) pNodeAnim.mScalingKeys().get(i + 1).mTime()) {
                return i;
            }
        }

        return 0;
    }

    protected void readNodeHierarchy(float AnimationTime, AINode pNode, Matrix4f ParentTransform) {
        String name = pNode.mName().dataString();
        Matrix4f NodeTransformation = AssimpUtils.from(pNode.mTransformation());
        AINodeAnim pNodeAnim = FindNodeAnim(animation, name);

        if (pNodeAnim != null) {
            // Interpolate scaling and generate scaling transformation matrix
            Vector3f Scaling = new Vector3f(1, 1, 1);
            Scaling = CalcInterpolatedScaling(Scaling, AnimationTime, pNodeAnim);
            Matrix4f ScalingM = new Matrix4f().identity().scale(Scaling.x(), Scaling.y(), Scaling.z());

            // Interpolate rotation and generate rotation transformation matrix
            Quaternionf RotationQ = new Quaternionf(0, 0, 0, 0);
            CalcInterpolatedRotation(RotationQ, AnimationTime, pNodeAnim);
            Matrix4f RotationM = RotationQ.get(new Matrix4f());

            // Interpolate translation and generate translation transformation matrix
            Vector3f Translation = new Vector3f(0, 0, 0);
            CalcInterpolatedPosition(Translation, AnimationTime, pNodeAnim);
            Matrix4f TranslationM = new Matrix4f().identity().translate(Translation.x(), Translation.y(), Translation.z());

            // Combine the above transformations
            NodeTransformation = new Matrix4f(TranslationM).mul(new Matrix4f(RotationM)).mul(new Matrix4f(ScalingM));
        }

        Matrix4f GlobalTransformation = new Matrix4f(ParentTransform).mul(NodeTransformation);

        Bone bone;

        if ((bone = findBone(name)) != null) {
            bone.finalTransformation = new Matrix4f(globalInverseTransform).mul(new Matrix4f(GlobalTransformation)).mul(bone.offsetMatrix);
        }

        for (int i = 0; i < pNode.mNumChildren(); i++) {
            readNodeHierarchy(AnimationTime, AINode.create(pNode.mChildren().get(i)), GlobalTransformation);
        }
    }

    private Bone findBone(String name) {
        for (Bone bone : bones) if (bone.name.equals(name)) return bone;

        return null;
    }

    public void boneTransforms(float timeInSeconds) {
        Matrix4f Identity = new Matrix4f().identity();

        float TicksPerSecond = (float) (animation.mTicksPerSecond() != 0 ? animation.mTicksPerSecond() : 25.0f);
        float TimeInTicks = timeInSeconds * TicksPerSecond;
        float AnimationTime = (TimeInTicks % (float) animation.mDuration());

        readNodeHierarchy(AnimationTime, root, Identity);

        for (short i = 0; i < bones.length; i++) {
            boneTransforms[i] = bones[i].finalTransformation;
        }
    }
}