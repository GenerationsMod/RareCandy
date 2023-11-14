package gg.generations.rarecandy.renderer.loading;

import gg.generations.rarecandy.pokeutils.reader.TextureReference;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL40;
import org.lwjgl.system.MemoryUtil;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.util.function.Supplier;

public class Texture implements ITexture, Supplier<ITexture> {

    public final String name;
    public int id;

    private boolean initalized = false;

    private final BufferedImage image;

    public Texture(TextureReference reference) {
        name = reference.name();
        image = reference.data();

    }

    public void bind(int slot) {
        assert (slot >= 0 && slot <= 31);
        GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + slot);
        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, this.id);
    }

    @Override
    public void close() throws IOException {
        GL40.glDeleteTextures(id);
    }

    @Override
    public ITexture get() {
        if(!initalized) {
            id = GL11C.glGenTextures();

            var buffer = TextureReference.read(image);

            GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, this.id);
            GL11C.glTexImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, buffer);

            GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL11C.GL_REPEAT);
            GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL11C.GL_REPEAT);

            GL11C.glTexParameterf(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST);
            GL11C.glTexParameterf(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST);

            MemoryUtil.memFree(buffer);

            initalized = true;
        }

        return this;
    }
}
