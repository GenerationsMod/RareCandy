package com.pixelmongenerations.inception.core;

import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

public class ComputeShader {

    public ComputeShader() {
        try(MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer pWorkGroupCountX = stack.ints(1);
            IntBuffer pWorkGroupCountY = stack.ints(1);
            IntBuffer pWorkGroupCountZ = stack.ints(1);
            GL30C.glGetIntegeri_v(GL43C.GL_MAX_COMPUTE_WORK_GROUP_COUNT, 0, pWorkGroupCountX);
            GL30C.glGetIntegeri_v(GL43C.GL_MAX_COMPUTE_WORK_GROUP_COUNT, 1, pWorkGroupCountY);
            GL30C.glGetIntegeri_v(GL43C.GL_MAX_COMPUTE_WORK_GROUP_COUNT, 2, pWorkGroupCountZ);

            System.out.printf("Max Total work groups x:%s, y:%s, z:%s%n", pWorkGroupCountX.get(0), pWorkGroupCountY.get(0), pWorkGroupCountZ.get(0));
        }
    }
}
