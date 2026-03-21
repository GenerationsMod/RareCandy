package gg.generations.rarecandy.renderer.textures.framebuffer;

import gg.generations.rarecandy.renderer.textures.ITexture;

public interface AttachmentResource extends AutoCloseable {
    int width();
    int height();
    AttachmentSpec spec();
    void attach(int framebufferId, int attachmentPoint);

    default ITexture textureOrNull() {
        return null;
    }

    @Override
    void close();
}
