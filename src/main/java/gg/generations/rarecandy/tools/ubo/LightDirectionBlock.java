package gg.generations.rarecandy.tools.ubo;

import gg.generations.rarecandy.renderer.ubo.UniformBlockUploader;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class LightDirectionBlock extends UniformBlockUploader {
    public Vector3f LightDirection0 = new Vector3f();
    public Vector3f LightDirection1 = new Vector3f();

    public LightDirectionBlock() {
        super(32, 2);
        update();
    }

    public void update() {
        initalize();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long address = stack.nmalloc(32);
            LightDirection0.getToAddress(address);
            LightDirection1.getToAddress(address + 16);
            upload(0, 32, address);
        }
    }
}
