package gg.generations.rarecandy.renderer.textures.framebuffer;

import gg.generations.rarecandy.renderer.textures.ITexture;

public final class FramebufferAttachment implements AutoCloseable {
    private final int attachmentPoint;
    private final AttachmentSpec spec;
    private final AttachmentResource resource;

    public FramebufferAttachment(int attachmentPoint, AttachmentSpec spec, AttachmentResource resource) {
        this.attachmentPoint = attachmentPoint;
        this.spec = spec;
        this.resource = resource;
    }

    public int attachmentPoint() {
        return attachmentPoint;
    }

    public AttachmentSpec spec() {
        return spec;
    }

    public AttachmentResource resource() {
        return resource;
    }

    public void attach(int framebufferId) {
        resource.attach(framebufferId, attachmentPoint);
    }

    public ITexture textureOrNull() {
        return resource.textureOrNull();
    }

    @Override
    public void close() {
        resource.close();
    }
}
