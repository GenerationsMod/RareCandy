package gg.generations.rarecandy.renderer.textures;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;

import java.io.IOException;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_BASE_LEVEL;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_MAX_LEVEL;
import static org.lwjgl.opengl.GL42.glTexStorage2D;

public final class BlankTexture implements ITexture {
    private final Texture.Type type;
    private final int width;
    private final int height;
    private final ComputeAccess access;
    private final int id;
    private boolean resident;

    public BlankTexture(Texture.Type type, int width, int height, ComputeAccess access) {
        this.type = type;
        this.width = width;
        this.height = height;
        this.access = access;

        id = GL11.glGenTextures();
        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, id);
        glTexStorage2D(GL11C.GL_TEXTURE_2D, 1, type.internalFormat, width, height);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST); // or GL_LINEAR
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST); // or GL_LINEAR

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, 0);
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
        if (resident) {
            SamplerCache.unresidentForTexture(id);
            ImageHandleCache.unresidentForTexture(id);
            resident = false;
        }
        GL11.glDeleteTextures(id);
    }

    public ComputeAccess access() {
        return access;
    }

    @Override
    public long getSamplerHandle(SamplerDesc sampler) {
        BindlessSupport.require();
        long h = SamplerCache.getOrCreateHandle(id, sampler);
        resident = true;
        return h;
    }

    @Override
    public long getImageHandle(int level, boolean layered, ComputeAccess acc) {
        BindlessSupport.require();
        long h = ImageHandleCache.getOrCreate(id, level, layered, type.internalFormat, acc);
        resident = true;
        return h;
    }
}
