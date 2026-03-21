package gg.generations.rarecandy.renderer.textures.framebuffer;

public interface AttachmentAllocator {
    AttachmentResource allocate(AttachmentSpec spec, int width, int height);
}
