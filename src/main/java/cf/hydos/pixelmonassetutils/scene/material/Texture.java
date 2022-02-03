package cf.hydos.pixelmonassetutils.scene.material;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.stb.STBImage.*;

public class Texture {

    public final String name;
    public final int id;

    public Texture(ByteBuffer imageFileBytes, String name) {
        this.name = name;
        this.id = glGenTextures();

        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        IntBuffer numComponents = BufferUtils.createIntBuffer(1);

        if (!stbi_info_from_memory(imageFileBytes, w, h, numComponents)) {
            throw new RuntimeException("Failed to read image information: " + stbi_failure_reason());
        }

        ByteBuffer buffer = stbi_load_from_memory(imageFileBytes, w, h, numComponents, 3);
        if (buffer == null) {
            throw new RuntimeException("Failed to load image: " + stbi_failure_reason());
        }

        glBindTexture(GL_TEXTURE_2D, this.id);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB8, w.get(0), h.get(0), 0, GL_RGB, GL_UNSIGNED_BYTE, buffer);
    }

    public void bind(int slot) {
        assert (slot >= 0 && slot <= 31);
        glActiveTexture(GL_TEXTURE0 + slot);
        glBindTexture(GL_TEXTURE_2D, this.id);
    }
}
