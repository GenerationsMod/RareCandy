package gg.generations.rarecandy.renderer.textures.framebuffer;

import org.lwjgl.opengl.GL30;

public final class DefaultAttachmentAllocator implements AttachmentAllocator {
    @Override
    public AttachmentResource allocate(AttachmentSpec spec, int width, int height) {
        return switch (spec.storage()) {
            case TEXTURE -> new TextureAttachmentResource(TextureFactory.createTexture2D(width, height, spec.format()), spec, true);
            case RENDERBUFFER -> allocateRenderbuffer(spec, width, height);
        };
    }

    private AttachmentResource allocateRenderbuffer(AttachmentSpec spec, int width, int height) {
        int id = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, id);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, spec.format().internalFormat, width, height);
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
        return new RenderbufferAttachmentResource(id, width, height, spec, true);
    }
}
