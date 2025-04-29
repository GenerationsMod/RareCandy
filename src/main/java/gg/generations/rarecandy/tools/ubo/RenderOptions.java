package gg.generations.rarecandy.tools.ubo;

import gg.generations.rarecandy.renderer.ubo.UniformBlockUploader;
import gg.generations.rarecandy.tools.gui.RareCandyCanvas;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import static gg.generations.rarecandy.tools.gui.GuiPipelines.pingpong;


public class RenderOptions extends UniformBlockUploader {
    public static final Vector4f colorMOdulator = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f tint = new Vector3f(1,1, 1);

    public RenderOptions() {
        super(52, 5);
        update();
    }

    public void update() {
        initalize();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long address = stack.nmalloc(52);
            colorMOdulator.getToAddress(address);
            tint.getToAddress(address + 16);
            MemoryUtil.memPutInt(address + 32 + 4, (int) (RareCandyCanvas.getLightLevel() * 15));
            MemoryUtil.memPutInt(address + 48, (int) pingpong(RareCandyCanvas.getTime() % 1d));
            upload(0, 52, address);
        }
    }

}
