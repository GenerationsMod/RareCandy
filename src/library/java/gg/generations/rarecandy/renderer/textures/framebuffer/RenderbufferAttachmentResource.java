package gg.generations.rarecandy.renderer.textures.framebuffer;

import org.lwjgl.opengl.GL30;

public final class RenderbufferAttachmentResource implements AttachmentResource {
    private final int id;
    private final int width;
    private final int height;
    private final AttachmentSpec spec;
    private final boolean owned;

    public RenderbufferAttachmentResource(int id, int width, int height, AttachmentSpec spec, boolean owned) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.spec = spec;
        this.owned = owned;
    }

    public int id() {
        return id;
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
    public AttachmentSpec spec() {
        return spec;
    }

    @Override
    public void attach(int framebufferId, int attachmentPoint) {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferId);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, attachmentPoint, GL30.GL_RENDERBUFFER, id);
    }

    @Override
    public void close() {
        if (owned && id != 0) {
            GL30.glDeleteRenderbuffers(id);
        }
    }
}
