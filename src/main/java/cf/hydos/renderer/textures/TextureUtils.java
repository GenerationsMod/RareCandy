package cf.hydos.renderer.textures;

import cf.hydos.renderer.utils.MyFile;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

public class TextureUtils {

    protected static TextureData decodeTextureFile(MyFile file) {
        int width = 0;
        int height = 0;
        ByteBuffer pixels = null;
        try {
            try (MemoryStack stack = MemoryStack.stackPush()) {

                var pWidth = stack.mallocInt(1);
                var pHeight = stack.mallocInt(1);
                var pChannels = stack.mallocInt(1);
                pixels = STBImage.stbi_load_from_memory(IOUtils.ioResourceToByteBuffer(file.getInputStream(), 8 * 1024), pWidth, pHeight, pChannels, 4);
                if (pixels == null) {
                    throw new RuntimeException("Failed to load image properly!");
                }

                width = pWidth.get(0);
                height = pHeight.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Tried to load texture " + file.getName() + " , didn't work");
            System.exit(-1);
        }
        return new TextureData(pixels, width, height);
    }

    protected static int loadTextureToOpenGL(TextureData data, TextureBuilder builder) {
        int texID = GL11.glGenTextures();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, data.getWidth(), data.getHeight(), 0, GL12.GL_BGRA,
                GL11.GL_UNSIGNED_BYTE, data.getBuffer());
        if (builder.isMipmap()) {
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
            if (builder.isAnisotropic() && GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
                GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0);
                GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT,
                        4.0f);
            }
        } else if (builder.isNearest()) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        } else {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        }
        if (builder.isClampEdges()) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        } else {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        return texID;
    }

}
