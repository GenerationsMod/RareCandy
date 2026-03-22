package gg.generations.rarecandy.renderer.textures.framebuffer;

import gg.generations.rarecandy.renderer.textures.ITexture;

public record FramebufferAttachment(int attachmentPoint, AttachmentSpec spec,
                                    AttachmentResource resource) implements AutoCloseable {

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
