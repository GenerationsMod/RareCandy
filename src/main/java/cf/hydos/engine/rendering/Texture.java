package cf.hydos.engine.rendering;

import cf.hydos.engine.rendering.resources.TextureResource;
import cf.hydos.pixelmonassetutils.scene.material.GlbTexture;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class Texture {
    private static final HashMap<String, TextureResource> s_loadedTextures = new HashMap<>();
    private final TextureResource resource;
    private final String fileName;

    public Texture(String fileName) {
        this.fileName = fileName;
        TextureResource oldResource = s_loadedTextures.get(fileName);

        if (oldResource != null) {
            resource = oldResource;
            resource.AddReference();
        } else {
            resource = LoadTexture(fileName);
            s_loadedTextures.put(fileName, resource);
        }
    }

    public Texture(GlbTexture texture) {
        this.fileName = texture.name;
        this.resource = texture;
    }

    private static TextureResource LoadTexture(String fileName) {
        try {
            BufferedImage image = ImageIO.read(Objects.requireNonNull(Shader.class.getResourceAsStream(("/textures/" + fileName))));
            int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());

            ByteBuffer buffer = BufferUtils.createByteBuffer(image.getHeight() * image.getWidth() * 4);
            boolean hasAlpha = image.getColorModel().hasAlpha();

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int pixel = pixels[y * image.getWidth() + x];

                    buffer.put((byte) ((pixel >> 16) & 0xFF));
                    buffer.put((byte) ((pixel >> 8) & 0xFF));
                    buffer.put((byte) ((pixel) & 0xFF));
                    if (hasAlpha) buffer.put((byte) ((pixel >> 24) & 0xFF));
                    else buffer.put((byte) (0xFF));
                }
            }

            buffer.flip();

            TextureResource resource = new TextureResource();
            glBindTexture(GL_TEXTURE_2D, resource.GetId());

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

            return resource;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        return null;
    }

    @Override
    protected void finalize() {
        if (resource.RemoveReference() && !fileName.isEmpty()) {
            s_loadedTextures.remove(fileName);
        }
    }

    public void Bind() {
        Bind(0);
    }

    public void Bind(int samplerSlot) {
        assert (samplerSlot >= 0 && samplerSlot <= 31);
        glActiveTexture(GL_TEXTURE0 + samplerSlot);
        glBindTexture(GL_TEXTURE_2D, resource.GetId());
    }

    public int GetID() {
        return resource.GetId();
    }
}
