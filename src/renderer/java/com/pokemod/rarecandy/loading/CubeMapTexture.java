package com.pokemod.rarecandy.loading;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class CubeMapTexture {
    private static final Map<Integer, Integer> MC_TO_GL = Map.of(
            0, 1, // Pos X
            1, 3, // Neg X
            2, 4, // Pos Y
            3, 5, // Neg Y
            4, 0, // Pos Z
            5, 2 // Neg Z
    );
    public final int id;

    public CubeMapTexture(String path) {
        this.id = GL11C.glGenTextures();
        GL11C.glBindTexture(GL20C.GL_TEXTURE_CUBE_MAP, id);

        try (var stack = MemoryStack.stackPush()) {
            for (int i = 0; i < 6; i++) {
                var fileBytes = readResource(Paths.get(path + MC_TO_GL.getOrDefault(i, i) + ".png"));
                var width = stack.mallocInt(1);
                var height = stack.mallocInt(1);
                var channels = stack.mallocInt(1);
                var rgbaBytes = STBImage.stbi_load_from_memory(fileBytes, width, height, channels, 4);
                if (rgbaBytes == null) throw new RuntimeException("Failed to load image.");

                GL11C.glTexImage2D(GL13C.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL11C.GL_RGBA, width.get(0), height.get(0), 0, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, rgbaBytes);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        GL11C.glTexParameterf(GL20C.GL_TEXTURE_CUBE_MAP, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_LINEAR);
        GL11C.glTexParameterf(GL20C.GL_TEXTURE_CUBE_MAP, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_LINEAR);
        GL11C.glTexParameteri(GL20C.GL_TEXTURE_CUBE_MAP, GL11C.GL_TEXTURE_WRAP_S, GL13C.GL_CLAMP_TO_EDGE);
        GL11C.glTexParameteri(GL20C.GL_TEXTURE_CUBE_MAP, GL11C.GL_TEXTURE_WRAP_T, GL13C.GL_CLAMP_TO_EDGE);
        GL11C.glTexParameteri(GL20C.GL_TEXTURE_CUBE_MAP, GL13C.GL_TEXTURE_WRAP_R, GL13C.GL_CLAMP_TO_EDGE);
        GL11C.glBindTexture(GL20C.GL_TEXTURE_CUBE_MAP, 0);
    }

    public static ByteBuffer readResource(Path path) throws IOException {
        return readResource(Files.readAllBytes(path));
    }

    public static ByteBuffer readResource(byte[] bytes) {
        var nativeBuffer = MemoryUtil.memAlloc(bytes.length);
        nativeBuffer.put(bytes);
        return nativeBuffer.rewind();
    }
}
