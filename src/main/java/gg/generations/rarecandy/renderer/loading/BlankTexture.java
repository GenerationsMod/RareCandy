package gg.generations.rarecandy.renderer.loading;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;

import java.io.IOException;
import java.util.Objects;

import static org.lwjgl.opengl.GL42.glTexStorage2D;

public final class BlankTexture implements ITexture {
    private final Texture.Type type;
    private final int width;
    private final int height;
    private final ComputeAccess access;
    private final int id;

    public BlankTexture(Texture.Type type, int width, int height, ComputeAccess access) {
        this.type = type;
        this.width = width;
        this.height = height;
        this.access = access;

        id = GL11.glGenTextures();
        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, id);
        glTexStorage2D(GL11C.GL_TEXTURE_2D, 1, type.internalFormat, width, height);
        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, 0);
    }

    @Override
    public void bind(int slot) {
        assert (slot >= 0 && slot <= 31);
        GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + slot);
        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, this.id);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Type getType() {
        return type;
    }

    public Texture.Type type() {
        return type;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BlankTexture) obj;
        return Objects.equals(this.type, that.type) &&
                this.width == that.width &&
                this.height == that.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, width, height);
    }

    @Override
    public String toString() {
        return "BlankDetails[" +
                "type=" + type + ", " +
                "width=" + width + ", " +
                "height=" + height + ']';
    }

    @Override
    public void close() throws IOException {
        GL11.glDeleteTextures(id);
    }

    public ComputeAccess getAccess() {
        return access;
    }
}
