package gg.generations.rarecandy.renderer.textures.framebuffer;

import gg.generations.rarecandy.renderer.textures.ITexture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FramebufferSpec {
    private final int width;
    private final int height;
    private final List<AttachmentSpec> colorAttachments;
    private final AttachmentSpec depthAttachment;
    private final AttachmentSpec stencilAttachment;
    private final AttachmentSpec depthStencilAttachment;

    public FramebufferSpec(
            int width,
            int height,
            List<AttachmentSpec> colorAttachments,
            AttachmentSpec depthAttachment,
            AttachmentSpec stencilAttachment,
            AttachmentSpec depthStencilAttachment
    ) {
        if (width <= 0) throw new IllegalArgumentException("width must be > 0");
        if (height <= 0) throw new IllegalArgumentException("height must be > 0");

        this.width = width;
        this.height = height;
        this.colorAttachments = Collections.unmodifiableList(new ArrayList<>(colorAttachments == null ? List.of() : colorAttachments));
        this.depthAttachment = depthAttachment;
        this.stencilAttachment = stencilAttachment;
        this.depthStencilAttachment = depthStencilAttachment;

        validate();
    }

    private void validate() {
        for (AttachmentSpec spec : colorAttachments) {
            if (spec.kind() != AttachmentSpec.Kind.COLOR) {
                throw new IllegalArgumentException("Color attachment list contains non-color spec: " + spec.kind());
            }
        }

        if (depthAttachment != null && depthAttachment.kind() != AttachmentSpec.Kind.DEPTH) {
            throw new IllegalArgumentException("depthAttachment must be DEPTH");
        }
        if (stencilAttachment != null && stencilAttachment.kind() != AttachmentSpec.Kind.STENCIL) {
            throw new IllegalArgumentException("stencilAttachment must be STENCIL");
        }
        if (depthStencilAttachment != null && depthStencilAttachment.kind() != AttachmentSpec.Kind.DEPTH_STENCIL) {
            throw new IllegalArgumentException("depthStencilAttachment must be DEPTH_STENCIL");
        }

        if (depthStencilAttachment != null && depthAttachment != null) {
            throw new IllegalArgumentException("Cannot have both depth and depth-stencil attachments");
        }
        if (depthStencilAttachment != null && stencilAttachment != null) {
            throw new IllegalArgumentException("Cannot have both stencil and depth-stencil attachments");
        }
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public List<AttachmentSpec> colorAttachments() {
        return colorAttachments;
    }

    public AttachmentSpec depthAttachment() {
        return depthAttachment;
    }

    public AttachmentSpec stencilAttachment() {
        return stencilAttachment;
    }

    public AttachmentSpec depthStencilAttachment() {
        return depthStencilAttachment;
    }

    public FramebufferSpec resized(int width, int height) {
        return new FramebufferSpec(width, height, colorAttachments, depthAttachment, stencilAttachment, depthStencilAttachment);
    }

    public static Builder builder(int width, int height) {
        return new Builder(width, height);
    }

    public static final class Builder {
        private final int width;
        private final int height;
        private final List<AttachmentSpec> colorAttachments = new ArrayList<>();
        private AttachmentSpec depthAttachment;
        private AttachmentSpec stencilAttachment;
        private AttachmentSpec depthStencilAttachment;

        private Builder(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public Builder color(AttachmentSpec spec) {
            if (spec.kind() != AttachmentSpec.Kind.COLOR) {
                throw new IllegalArgumentException("Only COLOR specs may be added as color attachments");
            }
            colorAttachments.add(spec);
            return this;
        }

        public Builder depth(AttachmentSpec spec) {
            this.depthAttachment = spec;
            return this;
        }

        public Builder stencil(AttachmentSpec spec) {
            this.stencilAttachment = spec;
            return this;
        }

        public Builder depthStencil(AttachmentSpec spec) {
            this.depthStencilAttachment = spec;
            return this;
        }

        public Builder colorTexture(ITexture.Type type) {
            return color(new AttachmentSpec(
                    AttachmentSpec.Kind.COLOR,
                    AttachmentSpec.Storage.TEXTURE,
                    type,
                    true
            ));
        }

        public Builder depthTexture(ITexture.Type type) {
            return depth(new AttachmentSpec(
                    AttachmentSpec.Kind.DEPTH,
                    AttachmentSpec.Storage.TEXTURE,
                    type,
                    true
            ));
        }

        public Builder depthRenderbuffer(ITexture.Type type) {
            return depth(new AttachmentSpec(
                    AttachmentSpec.Kind.DEPTH,
                    AttachmentSpec.Storage.RENDERBUFFER,
                    type,
                    false
            ));
        }

        public FramebufferSpec build() {
            return new FramebufferSpec(width, height, colorAttachments, depthAttachment, stencilAttachment, depthStencilAttachment);
        }
    }
}
