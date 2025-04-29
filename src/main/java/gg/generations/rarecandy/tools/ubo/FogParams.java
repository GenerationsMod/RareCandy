package gg.generations.rarecandy.tools.ubo;

import gg.generations.rarecandy.renderer.ubo.UniformBlockUploader;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import static gg.generations.rarecandy.tools.gui.RareCandyCanvas.viewMatrix;

public class FogParams extends UniformBlockUploader {
    public int FogShape = 0;
    public float FogStart = 1;
    public float FogEnd = 5;
    public Vector4f FogColor = new Vector4f(0, 0, 0, 0);

    public FogParams() {
        super(32, 3);
        update();
    }

    public void update() {
        initalize();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            long address = stack.nmalloc(32);
            MemoryUtil.memPutInt(address, FogShape);
            MemoryUtil.memPutFloat(address + 4, FogStart);
            MemoryUtil.memPutFloat(address + 8, FogEnd);
            FogColor.getToAddress(address + 16);
            upload(0, 32, address);
        }
    }
}
