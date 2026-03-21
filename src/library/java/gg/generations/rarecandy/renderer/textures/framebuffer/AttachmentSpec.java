package gg.generations.rarecandy.renderer.textures.framebuffer;

import gg.generations.rarecandy.renderer.textures.ITexture;

public record AttachmentSpec(
        Kind kind,
        Storage storage,
        ITexture.Type format,
        boolean sampled
) {
    public AttachmentSpec {
        if (kind == null) throw new IllegalArgumentException("kind cannot be null");
        if (storage == null) throw new IllegalArgumentException("storage cannot be null");
        if (format == null) throw new IllegalArgumentException("format cannot be null");
        if (storage == Storage.RENDERBUFFER && sampled) {
            throw new IllegalArgumentException("Renderbuffer attachments cannot be sampled");
        }

        switch (kind) {
            case COLOR -> {
                if (!format.isColorRenderable()) {
                    throw new IllegalArgumentException("Format " + format + " is not color-renderable");
                }
            }
            case DEPTH -> {
                if (!format.isDepthLike() || format.isDepthStencil()) {
                    throw new IllegalArgumentException("Format " + format + " is not a pure depth format");
                }
            }
            case STENCIL -> {
                if (!format.isStencilLike() || format.isDepthStencil()) {
                    throw new IllegalArgumentException("Format " + format + " is not a pure stencil format");
                }
            }
            case DEPTH_STENCIL -> {
                if (!format.isDepthStencil()) {
                    throw new IllegalArgumentException("Format " + format + " is not a depth-stencil format");
                }
            }
        }
    }

    public enum Kind {
        COLOR,
        DEPTH,
        STENCIL,
        DEPTH_STENCIL
    }

    public enum Storage {
        TEXTURE,
        RENDERBUFFER
    }
}
