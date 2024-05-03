package gg.generations.rarecandy.renderer.loading;

import gg.generations.rarecandy.pokeutils.reader.TextureDetails;
import gg.generations.rarecandy.pokeutils.reader.TextureReference;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL40;
import org.lwjgl.system.MemoryUtil;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class Texture implements ITexture {

    public final String name;
    public int id;

    private boolean initalized = false;

    private final TextureDetails image;

    public Texture(TextureReference reference) {
        name = reference.name();
        image = reference.data();

    }

    public void bind(int slot) {
        initalized();

        assert (slot >= 0 && slot <= 31);
        GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + slot);
        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, this.id);
    }

    @Override
    public void close() throws IOException {
        GL40.glDeleteTextures(id);
    }

    public void initalized() {
        if(!initalized) {
            id = GL11C.glGenTextures();

            var buffer = MemoryUtil.memAlloc(image.data().length).put(image.data()).flip();

            GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, this.id);
            GL11C.glTexImage2D(GL11C.GL_TEXTURE_2D, 0, image.type().getTexture(), image.width(), image.height(), 0, image.type().getPixelFormat(), GL11C.GL_UNSIGNED_BYTE, buffer);

            GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, image.wrapS().value());
            GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, image.wrapT().value());

            GL11C.glTexParameterf(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, image.minFilter().getValue());
            GL11C.glTexParameterf(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, image.maxFilter().getValue());

            MemoryUtil.memFree(buffer);

            initalized = true;
        }
    }
}
