package gg.generations.rarecandy.renderer.textures.framebuffer;

import gg.generations.rarecandy.renderer.textures.ITexture;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public final class TextureAttachmentResource implements AttachmentResource {
    private final ITexture texture;
    private final AttachmentSpec spec;
    private final boolean owned;

    public TextureAttachmentResource(ITexture texture, AttachmentSpec spec, boolean owned) {
        if (texture == null) throw new IllegalArgumentException("texture cannot be null");
        if (spec == null) throw new IllegalArgumentException("spec cannot be null");
        this.texture = texture;
        this.spec = spec;
        this.owned = owned;
    }

    @Override
    public int width() {
        return texture.width();
    }

    @Override
    public int height() {
        return texture.height();
    }

    @Override
    public AttachmentSpec spec() {
        return spec;
    }

    @Override
    public void attach(int framebufferId, int attachmentPoint) {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferId);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, attachmentPoint, GL11.GL_TEXTURE_2D, texture.id(), 0);
    }

    @Override
    public ITexture textureOrNull() {
        return texture;
    }

    @Override
    public void close() {
        if (owned) {
            try {
                texture.close();
            } catch (Exception e) {
                throw new RuntimeException("Failed to close texture attachment", e);
            }
        }
    }
}
