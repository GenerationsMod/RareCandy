package gg.generations.rarecandy.tools.ubo;

import gg.generations.rarecandy.renderer.ubo.UniformBlockUploader;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.util.function.Supplier;

public class SharedInfoBlock extends UniformBlockUploader {

    private final Matrix4f viewMatrix;
    private final Matrix4f projectionMatrix;
    private boolean dynamicVertexColor;

    public SharedInfoBlock(Matrix4f viewMatrix, Matrix4f projectionMatrix, boolean dynamicVertexColor) {
        super(144, 0); // 64 + 64 + 16 (bool + padding)
        this.viewMatrix = viewMatrix;
        this.projectionMatrix = projectionMatrix;
        this.dynamicVertexColor = dynamicVertexColor;
    }

    public void update() {
        initalize();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long address = stack.nmalloc(144);
            viewMatrix.getToAddress(address);
            projectionMatrix.getToAddress(address + 64);
            MemoryUtil.memPutInt(address + 128, dynamicVertexColor ? 1 : 0);
            upload(0, 144, address);
        }
    }

    public void setDynamicVertexColor(boolean dynamicVertexColor) {
        this.dynamicVertexColor = dynamicVertexColor;
        update();
    }
}