package gg.generations.rarecandy.renderer.textures.framebuffer;

import gg.generations.rarecandy.renderer.textures.ITexture;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FrameBuffer implements AutoCloseable {
    private final int id;
    private final AttachmentAllocator allocator;
    private FramebufferSpec spec;

    private final List<FramebufferAttachment> colorAttachments = new ArrayList<>();
    private FramebufferAttachment depthAttachment;
    private FramebufferAttachment stencilAttachment;
    private FramebufferAttachment depthStencilAttachment;

    public FrameBuffer(FramebufferSpec spec) {
        this(spec, new DefaultAttachmentAllocator());
    }

    public FrameBuffer(FramebufferSpec spec, AttachmentAllocator allocator) {
        if (spec == null) throw new IllegalArgumentException("spec cannot be null");
        if (allocator == null) throw new IllegalArgumentException("allocator cannot be null");
        this.id = GL30.glGenFramebuffers();
        this.spec = spec;
        this.allocator = allocator;
        rebuild();
    }

    public int id() {
        return id;
    }

    public int width() {
        return spec.width();
    }

    public int height() {
        return spec.height();
    }

    public FramebufferSpec spec() {
        return spec;
    }

    public void bind() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, id);
    }

    public void bindDraw() {
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, id);
    }

    public void bindRead() {
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, id);
    }

    public static void unbind() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    public List<FramebufferAttachment> colorAttachments() {
        return Collections.unmodifiableList(colorAttachments);
    }

    public FramebufferAttachment depthAttachment() {
        return depthAttachment;
    }

    public FramebufferAttachment stencilAttachment() {
        return stencilAttachment;
    }

    public FramebufferAttachment depthStencilAttachment() {
        return depthStencilAttachment;
    }

    public ITexture colorTexture(int index) {
        if (index < 0 || index >= colorAttachments.size()) {
            throw new IndexOutOfBoundsException("No color attachment at index " + index);
        }
        ITexture texture = colorAttachments.get(index).textureOrNull();
        if (texture == null) {
            throw new IllegalStateException("Color attachment " + index + " is not texture-backed");
        }
        return texture;
    }

    public ITexture depthTexture() {
        return depthAttachment == null ? null : depthAttachment.textureOrNull();
    }

    public ITexture stencilTexture() {
        return stencilAttachment == null ? null : stencilAttachment.textureOrNull();
    }

    public ITexture depthStencilTexture() {
        return depthStencilAttachment == null ? null : depthStencilAttachment.textureOrNull();
    }

    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        if (width == spec.width() && height == spec.height()) return;
        spec = spec.resized(width, height);
        rebuild();
    }

    public void validate() {
        FramebufferValidator.assertComplete(id);
    }

    private void rebuild() {
        closeAttachments();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, id);

        for (int i = 0; i < spec.colorAttachments().size(); i++) {
            AttachmentSpec attachmentSpec = spec.colorAttachments().get(i);
            FramebufferAttachment attachment = new FramebufferAttachment(
                    GL30.GL_COLOR_ATTACHMENT0 + i,
                    attachmentSpec,
                    allocator.allocate(attachmentSpec, spec.width(), spec.height())
            );
            attachment.attach(id);
            colorAttachments.add(attachment);
        }

        if (spec.depthAttachment() != null) {
            depthAttachment = new FramebufferAttachment(
                    GL30.GL_DEPTH_ATTACHMENT,
                    spec.depthAttachment(),
                    allocator.allocate(spec.depthAttachment(), spec.width(), spec.height())
            );
            depthAttachment.attach(id);
        }

        if (spec.stencilAttachment() != null) {
            stencilAttachment = new FramebufferAttachment(
                    GL30.GL_STENCIL_ATTACHMENT,
                    spec.stencilAttachment(),
                    allocator.allocate(spec.stencilAttachment(), spec.width(), spec.height())
            );
            stencilAttachment.attach(id);
        }

        if (spec.depthStencilAttachment() != null) {
            depthStencilAttachment = new FramebufferAttachment(
                    GL30.GL_DEPTH_STENCIL_ATTACHMENT,
                    spec.depthStencilAttachment(),
                    allocator.allocate(spec.depthStencilAttachment(), spec.width(), spec.height())
            );
            depthStencilAttachment.attach(id);
        }

        FramebufferDrawBuffers.applyForColorAttachmentCount(colorAttachments.size());
        FramebufferValidator.assertComplete(id);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    private void closeAttachments() {
        for (FramebufferAttachment attachment : colorAttachments) {
            attachment.close();
        }
        colorAttachments.clear();

        if (depthAttachment != null) {
            depthAttachment.close();
            depthAttachment = null;
        }
        if (stencilAttachment != null) {
            stencilAttachment.close();
            stencilAttachment = null;
        }
        if (depthStencilAttachment != null) {
            depthStencilAttachment.close();
            depthStencilAttachment = null;
        }


    }

    @Override
    public void close() {
        closeAttachments();
        GL30.glDeleteFramebuffers(id);
    }
}
