package cf.hydos.pixelmonassetutils.scene.material;

import cf.hydos.animationRendering.engine.rendering.resourceManagement.TextureResource;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.stb.STBImage.*;

public class GlbTexture extends TextureResource {

    public final String name;

    public GlbTexture(ByteBuffer compressedBuffer, String name) {
        super();
        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        IntBuffer comp = BufferUtils.createIntBuffer(1);
        if (!stbi_info_from_memory(compressedBuffer, w, h, comp))
            throw new RuntimeException("Failed to read image information: " + stbi_failure_reason());
        ByteBuffer buffer = stbi_load_from_memory(compressedBuffer, w, h, comp, 3);
        if (buffer == null)
            throw new RuntimeException("Failed to load image: " + stbi_failure_reason());

        this.name = name;
        glBindTexture(GL_TEXTURE_2D, GetId());

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB8, w.get(0), h.get(0), 0, GL_RGB, GL_UNSIGNED_BYTE, buffer);
    }
}