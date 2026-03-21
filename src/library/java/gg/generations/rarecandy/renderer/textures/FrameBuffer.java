package gg.generations.rarecandy.renderer.textures;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FrameBuffer implements AutoCloseable {
    // framebufferId is final — the GL object is never recreated.
    // width/height are mutable to support resize().
    private final int framebufferId;

    private int width;
    private int height;

    private final List<TextureSpec> colorSpecs;
    private final List<TextureAttachment> colorAttachments = new ArrayList<>();

    // Cached draw-buffer list, rebuilt in allocateAll() / resize().
    // Avoids allocating a new IntBuffer every setDrawAll() call.
    private IntBuffer cachedDrawBuffers;

    private final TextureSpec depthTextureSpec;
    private TextureAttachment depthTexture;

    private final TextureSpec stencilTextureSpec;
    private TextureAttachment stencilTexture;

    private final TextureSpec depthStencilTextureSpec;
    private TextureAttachment depthStencilTexture;

    private final RenderbufferSpec depthRenderbufferSpec;
    private int depthRenderbufferId;

    private final RenderbufferSpec stencilRenderbufferSpec;
    private int stencilRenderbufferId;

    private final RenderbufferSpec depthStencilRenderbufferSpec;
    private int depthStencilRenderbufferId;

    public FrameBuffer(int width, int height) {
        this(builder(width, height)
                .color(TextureSpec.texture2D(ITexture.Type.RGBA8))
                .depthStencilRenderbuffer(RenderbufferSpec.depth24Stencil8()));
    }

    public FrameBuffer(int width, int height, ITexture.Type colorType) {
        this(builder(width, height)
                .color(TextureSpec.texture2D(colorType))
                .depthStencilRenderbuffer(RenderbufferSpec.depth24Stencil8()));
    }

    public FrameBuffer(Builder builder) {
        if (builder == null) {
            throw new IllegalArgumentException("Builder cannot be null");
        }
        if (builder.width <= 0 || builder.height <= 0) {
            throw new IllegalArgumentException("Framebuffer size must be > 0");
        }
        if (builder.colorSpecs.isEmpty()
                && builder.depthTextureSpec == null
                && builder.stencilTextureSpec == null
                && builder.depthStencilTextureSpec == null
                && builder.depthRenderbufferSpec == null
                && builder.stencilRenderbufferSpec == null
                && builder.depthStencilRenderbufferSpec == null) {
            throw new IllegalArgumentException("Framebuffer must have at least one attachment");
        }

        this.width = builder.width;
        this.height = builder.height;

        this.colorSpecs = List.copyOf(builder.colorSpecs);

        this.depthTextureSpec = builder.depthTextureSpec;
        this.stencilTextureSpec = builder.stencilTextureSpec;
        this.depthStencilTextureSpec = builder.depthStencilTextureSpec;

        this.depthRenderbufferSpec = builder.depthRenderbufferSpec;
        this.stencilRenderbufferSpec = builder.stencilRenderbufferSpec;
        this.depthStencilRenderbufferSpec = builder.depthStencilRenderbufferSpec;

        this.framebufferId = GL45C.glCreateFramebuffers();

        try {
            allocateAll();
        } catch (RuntimeException ex) {
            closeSilently();
            throw ex;
        }
    }

    public static Builder builder(int width, int height) {
        return new Builder(width, height);
    }

    public int getFramebufferId() {
        return framebufferId;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int getColorAttachmentCount() {
        return colorAttachments.size();
    }

    public boolean hasDepthTexture() {
        return depthTexture != null;
    }

    public boolean hasStencilTexture() {
        return stencilTexture != null;
    }

    public boolean hasDepthStencilTexture() {
        return depthStencilTexture != null;
    }

    public boolean hasDepthRenderbuffer() {
        return depthRenderbufferId != 0;
    }

    public boolean hasStencilRenderbuffer() {
        return stencilRenderbufferId != 0;
    }

    public boolean hasDepthStencilRenderbuffer() {
        return depthStencilRenderbufferId != 0;
    }

    public TextureAttachment getColorAttachment(int index) {
        checkColorIndex(index);
        return colorAttachments.get(index);
    }

    public List<TextureAttachment> getColorAttachments() {
        return Collections.unmodifiableList(colorAttachments);
    }

    public TextureAttachment getDepthTexture() {
        if (depthTexture == null) {
            throw new IllegalStateException("No depth texture attached");
        }
        return depthTexture;
    }

    public TextureAttachment getStencilTexture() {
        if (stencilTexture == null) {
            throw new IllegalStateException("No stencil texture attached");
        }
        return stencilTexture;
    }

    public TextureAttachment getDepthStencilTexture() {
        if (depthStencilTexture == null) {
            throw new IllegalStateException("No depth-stencil texture attached");
        }
        return depthStencilTexture;
    }

    public int getDepthRenderbufferId() {
        if (depthRenderbufferId == 0) {
            throw new IllegalStateException("No depth renderbuffer attached");
        }
        return depthRenderbufferId;
    }

    public int getStencilRenderbufferId() {
        if (stencilRenderbufferId == 0) {
            throw new IllegalStateException("No stencil renderbuffer attached");
        }
        return stencilRenderbufferId;
    }

    public int getDepthStencilRenderbufferId() {
        if (depthStencilRenderbufferId == 0) {
            throw new IllegalStateException("No depth-stencil renderbuffer attached");
        }
        return depthStencilRenderbufferId;
    }

    public void bindFramebuffer() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferId);
    }

    public void bindForDraw() {
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, framebufferId);
    }

    public void bindForRead() {
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, framebufferId);
    }

    public static void unbindFramebuffer() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    public static void unbindDraw() {
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
    }

    public static void unbindRead() {
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);
    }

    public void bindAndSetViewport() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferId);
        GL11.glViewport(0, 0, width, height);
    }

    public void setDrawBuffers(int... attachments) {
        if (attachments == null || attachments.length == 0) {
            GL45C.glNamedFramebufferDrawBuffer(framebufferId, GL11.GL_NONE);
            return;
        }

        if (attachments.length == 1) {
            checkColorIndex(attachments[0]);
            GL45C.glNamedFramebufferDrawBuffer(framebufferId, GL30.GL_COLOR_ATTACHMENT0 + attachments[0]);
            return;
        }

        IntBuffer drawBuffers = BufferUtils.createIntBuffer(attachments.length);
        for (int attachment : attachments) {
            checkColorIndex(attachment);
            drawBuffers.put(GL30.GL_COLOR_ATTACHMENT0 + attachment);
        }
        drawBuffers.flip();
        GL45C.glNamedFramebufferDrawBuffers(framebufferId, drawBuffers);
    }

    /**
     * Resets draw buffers to all color attachments in order.
     * Uses a pre-allocated buffer built during construction/resize.
     */
    public void setDrawAll() {
        if (colorAttachments.isEmpty()) {
            GL45C.glNamedFramebufferDrawBuffer(framebufferId, GL11.GL_NONE);
            return;
        }

        if (colorAttachments.size() == 1) {
            GL45C.glNamedFramebufferDrawBuffer(framebufferId, GL30.GL_COLOR_ATTACHMENT0);
            return;
        }

        // cachedDrawBuffers is built once in allocateAll(); rewind before use.
        cachedDrawBuffers.rewind();
        GL45C.glNamedFramebufferDrawBuffers(framebufferId, cachedDrawBuffers);
    }

    public void setReadBuffer(int attachment) {
        checkColorIndex(attachment);
        GL45C.glNamedFramebufferReadBuffer(framebufferId, GL30.GL_COLOR_ATTACHMENT0 + attachment);
    }

    public void disableColorOutputs() {
        GL45C.glNamedFramebufferDrawBuffer(framebufferId, GL11.GL_NONE);
        GL45C.glNamedFramebufferReadBuffer(framebufferId, GL11.GL_NONE);
    }

    public void clearColor(int attachment, float r, float g, float b, float a) {
        checkColorIndex(attachment);
        GL45C.glClearNamedFramebufferfv(framebufferId, GL11.GL_COLOR, attachment, new float[]{r, g, b, a});
    }

    public void clearColorInt(int attachment, int x, int y, int z, int w) {
        checkColorIndex(attachment);
        GL45C.glClearNamedFramebufferiv(framebufferId, GL11.GL_COLOR, attachment, new int[]{x, y, z, w});
    }

    public void clearColorUInt(int attachment, int x, int y, int z, int w) {
        checkColorIndex(attachment);
        GL45C.glClearNamedFramebufferuiv(framebufferId, GL11.GL_COLOR, attachment, new int[]{x, y, z, w});
    }

    public void clearDepth(float depth) {
        GL45C.glClearNamedFramebufferfv(framebufferId, GL11.GL_DEPTH, 0, new float[]{depth});
    }

    public void clearStencil(int stencil) {
        GL45C.glClearNamedFramebufferiv(framebufferId, GL11.GL_STENCIL, 0, new int[]{stencil});
    }

    public void clearDepthStencil(float depth, int stencil) {
        GL45C.glClearNamedFramebufferfi(framebufferId, GL30.GL_DEPTH_STENCIL, 0, depth, stencil);
    }

    /**
     * Blits from this framebuffer to {@code target}.
     *
     * <p><b>Side effect:</b> this framebuffer's read buffer is left pointing at
     * {@code srcAttachment}, and {@code target}'s draw buffer is left pointing at
     * {@code dstAttachment}. Call {@link #setDrawAll()} / {@link #setReadBuffer}
     * afterward if you need to restore the previous state.
     */
    public void blitTo(FrameBuffer target, int srcAttachment, int dstAttachment, int mask, int filter) {
        if (target == null) {
            throw new IllegalArgumentException("Target framebuffer cannot be null");
        }

        checkColorIndex(srcAttachment);
        target.checkColorIndex(dstAttachment);

        GL45C.glNamedFramebufferReadBuffer(this.framebufferId, GL30.GL_COLOR_ATTACHMENT0 + srcAttachment);
        GL45C.glNamedFramebufferDrawBuffer(target.framebufferId, GL30.GL_COLOR_ATTACHMENT0 + dstAttachment);

        GL45C.glBlitNamedFramebuffer(
                this.framebufferId,
                target.framebufferId,
                0, 0, this.width, this.height,
                0, 0, target.width, target.height,
                mask,
                filter
        );
    }

    /**
     * Blits from this framebuffer to the default framebuffer (screen).
     *
     * <p><b>Side effect:</b> this framebuffer's read buffer is left pointing at
     * {@code srcAttachment}. Call {@link #setReadBuffer} afterward if you need
     * to restore the previous state.
     */
    public void blitToScreen(int srcAttachment, int targetWidth, int targetHeight, int mask, int filter) {
        checkColorIndex(srcAttachment);
        GL45C.glNamedFramebufferReadBuffer(this.framebufferId, GL30.GL_COLOR_ATTACHMENT0 + srcAttachment);

        GL45C.glBlitNamedFramebuffer(
                this.framebufferId,
                0,
                0, 0, this.width, this.height,
                0, 0, targetWidth, targetHeight,
                mask,
                filter
        );
    }

    public void resize(int newWidth, int newHeight) {
        if (newWidth <= 0 || newHeight <= 0) {
            throw new IllegalArgumentException("Framebuffer size must be > 0");
        }
        if (this.width == newWidth && this.height == newHeight) {
            return;
        }

        validateExternalAttachmentResize(colorSpecs, newWidth, newHeight);
        validateExternalTextureResize(depthTextureSpec, newWidth, newHeight, "depth texture");
        validateExternalTextureResize(stencilTextureSpec, newWidth, newHeight, "stencil texture");
        validateExternalTextureResize(depthStencilTextureSpec, newWidth, newHeight, "depth-stencil texture");
        validateExternalRenderbufferResize(depthRenderbufferSpec, newWidth, newHeight, "depth renderbuffer");
        validateExternalRenderbufferResize(stencilRenderbufferSpec, newWidth, newHeight, "stencil renderbuffer");
        validateExternalRenderbufferResize(depthStencilRenderbufferSpec, newWidth, newHeight, "depth-stencil renderbuffer");

        destroyAttachments();

        this.width = newWidth;
        this.height = newHeight;

        allocateAll();
    }

    /**
     * Throws {@link IllegalStateException} if the framebuffer is not complete.
     * @see #isComplete()
     */
    public void checkComplete() {
        int status = GL45C.glCheckNamedFramebufferStatus(framebufferId, GL30.GL_FRAMEBUFFER);
        if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Framebuffer is not complete: " + framebufferStatusName(status));
        }
    }

    /**
     * Returns {@code true} if the framebuffer is complete, {@code false} otherwise.
     * Useful for debug overlays or conditional logic without try/catch.
     */
    public boolean isComplete() {
        return GL45C.glCheckNamedFramebufferStatus(framebufferId, GL30.GL_FRAMEBUFFER) == GL30.GL_FRAMEBUFFER_COMPLETE;
    }

    @Override
    public void close() {
        destroyAttachments();
        GL45C.glDeleteFramebuffers(framebufferId);
    }

    private void allocateAll() {
        int colorIndex = 0;

        for (TextureSpec spec : colorSpecs) {
            TextureAttachment attachment = createTextureAttachment(spec, AttachmentRole.COLOR);
            attachTexture(framebufferId, GL30.GL_COLOR_ATTACHMENT0 + colorIndex, spec, attachment.id());
            colorAttachments.add(attachment);
            colorIndex++;
        }

        if (depthTextureSpec != null) {
            depthTexture = createTextureAttachment(depthTextureSpec, AttachmentRole.DEPTH);
            attachTexture(framebufferId, GL30.GL_DEPTH_ATTACHMENT, depthTextureSpec, depthTexture.id());
        }

        if (stencilTextureSpec != null) {
            stencilTexture = createTextureAttachment(stencilTextureSpec, AttachmentRole.STENCIL);
            attachTexture(framebufferId, GL30.GL_STENCIL_ATTACHMENT, stencilTextureSpec, stencilTexture.id());
        }

        if (depthStencilTextureSpec != null) {
            depthStencilTexture = createTextureAttachment(depthStencilTextureSpec, AttachmentRole.DEPTH_STENCIL);
            attachTexture(framebufferId, GL30.GL_DEPTH_STENCIL_ATTACHMENT, depthStencilTextureSpec, depthStencilTexture.id());
        }

        if (depthRenderbufferSpec != null) {
            depthRenderbufferId = createRenderbuffer(depthRenderbufferSpec);
            GL45C.glNamedFramebufferRenderbuffer(framebufferId, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthRenderbufferId);
        }

        if (stencilRenderbufferSpec != null) {
            stencilRenderbufferId = createRenderbuffer(stencilRenderbufferSpec);
            GL45C.glNamedFramebufferRenderbuffer(framebufferId, GL30.GL_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, stencilRenderbufferId);
        }

        if (depthStencilRenderbufferSpec != null) {
            depthStencilRenderbufferId = createRenderbuffer(depthStencilRenderbufferSpec);
            GL45C.glNamedFramebufferRenderbuffer(framebufferId, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, depthStencilRenderbufferId);
        }

        if (colorAttachments.isEmpty()) {
            GL45C.glNamedFramebufferDrawBuffer(framebufferId, GL11.GL_NONE);
            GL45C.glNamedFramebufferReadBuffer(framebufferId, GL11.GL_NONE);
            cachedDrawBuffers = null;
        } else if (colorAttachments.size() == 1) {
            GL45C.glNamedFramebufferDrawBuffer(framebufferId, GL30.GL_COLOR_ATTACHMENT0);
            GL45C.glNamedFramebufferReadBuffer(framebufferId, GL30.GL_COLOR_ATTACHMENT0);
            cachedDrawBuffers = null;
        } else {
            // Build and cache the draw-buffer list once; reused by setDrawAll().
            cachedDrawBuffers = BufferUtils.createIntBuffer(colorAttachments.size());
            for (int i = 0; i < colorAttachments.size(); i++) {
                cachedDrawBuffers.put(GL30.GL_COLOR_ATTACHMENT0 + i);
            }
            cachedDrawBuffers.flip();
            GL45C.glNamedFramebufferDrawBuffers(framebufferId, cachedDrawBuffers);
            cachedDrawBuffers.rewind();
            GL45C.glNamedFramebufferReadBuffer(framebufferId, GL30.GL_COLOR_ATTACHMENT0);
        }

        checkComplete();
    }

    private TextureAttachment createTextureAttachment(TextureSpec spec, AttachmentRole role) {
        validateTextureSpec(spec, role);

        final int textureId;
        if (spec.owned) {
            textureId = GL45C.glCreateTextures(spec.target.glTarget);
            allocateTextureStorage(textureId, spec);
            configureTextureParameters(textureId, spec);
        } else {
            textureId = spec.existingId;
        }

        return new TextureAttachment(
                textureId,
                width,
                height,
                spec.type,
                spec.computeAccess,
                spec.target.glTarget,
                spec.target.multisample,
                spec.owned
        );
    }

    private void attachTexture(int framebufferId, int attachmentPoint, TextureSpec spec, int textureId) {
        if (spec.attachKind == TextureAttachKind.WHOLE_TEXTURE) {
            GL45C.glNamedFramebufferTexture(framebufferId, attachmentPoint, textureId, spec.level);
        } else {
            GL45C.glNamedFramebufferTextureLayer(framebufferId, attachmentPoint, textureId, spec.level, spec.layer);
        }
    }

    private int createRenderbuffer(RenderbufferSpec spec) {
        validateRenderbufferSpec(spec);

        if (!spec.owned) {
            return spec.existingId;
        }

        int renderbufferId = GL45C.glCreateRenderbuffers();
        if (spec.samples > 1) {
            GL45C.glNamedRenderbufferStorageMultisample(renderbufferId, spec.samples, spec.format.internalFormat, width, height);
        } else {
            GL45C.glNamedRenderbufferStorage(renderbufferId, spec.format.internalFormat, width, height);
        }
        return renderbufferId;
    }

    private void allocateTextureStorage(int textureId, TextureSpec spec) {
        switch (spec.target) {
            case TEXTURE_2D -> GL45C.glTextureStorage2D(textureId, spec.levels, spec.type.internalFormat, width, height);

            case TEXTURE_2D_ARRAY -> GL45C.glTextureStorage3D(textureId, spec.levels, spec.type.internalFormat, width, height, spec.layers);

            case TEXTURE_CUBE_MAP -> GL45C.glTextureStorage2D(textureId, spec.levels, spec.type.internalFormat, width, height);

            case TEXTURE_2D_MULTISAMPLE -> GL45C.glTextureStorage2DMultisample(
                    textureId,
                    spec.samples,
                    spec.type.internalFormat,
                    width,
                    height,
                    spec.fixedSampleLocations
            );

            case TEXTURE_2D_MULTISAMPLE_ARRAY -> GL45C.glTextureStorage3DMultisample(
                    textureId,
                    spec.samples,
                    spec.type.internalFormat,
                    width,
                    height,
                    spec.layers,
                    spec.fixedSampleLocations
            );
        }
    }

    private void configureTextureParameters(int textureId, TextureSpec spec) {
        if (!spec.target.multisample) {
            GL45C.glTextureParameteri(textureId, GL11.GL_TEXTURE_MIN_FILTER, spec.minFilter);
            GL45C.glTextureParameteri(textureId, GL11.GL_TEXTURE_MAG_FILTER, spec.magFilter);
            GL45C.glTextureParameteri(textureId, GL11.GL_TEXTURE_WRAP_S, spec.wrapS);
            GL45C.glTextureParameteri(textureId, GL11.GL_TEXTURE_WRAP_T, spec.wrapT);

            if (spec.target == TextureTarget.TEXTURE_2D_ARRAY || spec.target == TextureTarget.TEXTURE_CUBE_MAP) {
                GL45C.glTextureParameteri(textureId, GL12.GL_TEXTURE_WRAP_R, spec.wrapR);
            }

            if (spec.compareMode) {
                GL45C.glTextureParameteri(textureId, GL14.GL_TEXTURE_COMPARE_MODE, GL14.GL_COMPARE_R_TO_TEXTURE);
                GL45C.glTextureParameteri(textureId, GL14.GL_TEXTURE_COMPARE_FUNC, spec.compareFunc);
            }
        }
    }

    private void validateTextureSpec(TextureSpec spec, AttachmentRole role) {
        if (spec == null) {
            throw new IllegalArgumentException("Texture spec cannot be null");
        }
        if (spec.type == null) {
            throw new IllegalArgumentException("Texture type cannot be null");
        }
        if (spec.target == null) {
            throw new IllegalArgumentException("Texture target cannot be null");
        }
        if (spec.levels <= 0) {
            throw new IllegalArgumentException("Texture levels must be > 0");
        }
        if (spec.target.multisample && spec.samples <= 0) {
            throw new IllegalArgumentException("Multisample texture must have samples > 0");
        }
        if (!spec.target.multisample && spec.samples != 1) {
            throw new IllegalArgumentException("Non-multisample texture must use samples=1");
        }
        if (!spec.target.layered && spec.attachKind == TextureAttachKind.LAYER) {
            throw new IllegalArgumentException("Target " + spec.target + " does not support layer attachment");
        }
        if ((spec.target == TextureTarget.TEXTURE_2D_ARRAY || spec.target == TextureTarget.TEXTURE_2D_MULTISAMPLE_ARRAY) && spec.layers <= 0) {
            throw new IllegalArgumentException("Array texture must have layers > 0");
        }
        if (spec.target == TextureTarget.TEXTURE_CUBE_MAP) {
            if (spec.layers != 6) {
                throw new IllegalArgumentException("Cube map texture must use layers=6");
            }
            if (spec.attachKind == TextureAttachKind.LAYER && (spec.layer < 0 || spec.layer > 5)) {
                throw new IllegalArgumentException("Cube face layer must be in [0, 5]");
            }
        }

        switch (role) {
            case COLOR -> {
                if (!spec.type.isColorRenderable()) {
                    throw new IllegalArgumentException("Color attachment must use color-renderable format: " + spec.type);
                }
                if (spec.type.isDepthLike() || spec.type.isStencilLike()) {
                    throw new IllegalArgumentException("Color attachment cannot use depth/stencil-like format: " + spec.type);
                }
            }
            case DEPTH -> {
                if (!spec.type.isDepthLike() || spec.type.isDepthStencil() || spec.type.isStencilLike()) {
                    throw new IllegalArgumentException("Depth attachment must use pure depth format: " + spec.type);
                }
            }
            case STENCIL -> {
                if (!spec.type.isStencilLike() || spec.type.isDepthLike()) {
                    throw new IllegalArgumentException("Stencil attachment must use pure stencil format: " + spec.type);
                }
            }
            case DEPTH_STENCIL -> {
                if (!spec.type.isDepthStencil()) {
                    throw new IllegalArgumentException("Depth-stencil attachment must use depth-stencil format: " + spec.type);
                }
            }
        }
    }

    private void validateRenderbufferSpec(RenderbufferSpec spec) {
        if (spec == null) {
            throw new IllegalArgumentException("Renderbuffer spec cannot be null");
        }
        if (spec.format == null) {
            throw new IllegalArgumentException("Renderbuffer format cannot be null");
        }
        if (spec.samples <= 0) {
            throw new IllegalArgumentException("Renderbuffer samples must be > 0");
        }
    }

    private void validateExternalAttachmentResize(List<TextureSpec> specs, int newWidth, int newHeight) {
        for (TextureSpec spec : specs) {
            validateExternalTextureResize(spec, newWidth, newHeight, "color texture");
        }
    }

    private void validateExternalTextureResize(TextureSpec spec, int newWidth, int newHeight, String name) {
        if (spec != null && !spec.owned) {
            if (spec.externalWidth != newWidth || spec.externalHeight != newHeight) {
                throw new IllegalStateException(
                        "Cannot resize framebuffer to " + newWidth + "x" + newHeight +
                                " because external " + name + " is " + spec.externalWidth + "x" + spec.externalHeight
                );
            }
        }
    }

    private void validateExternalRenderbufferResize(RenderbufferSpec spec, int newWidth, int newHeight, String name) {
        if (spec != null && !spec.owned) {
            if (spec.externalWidth != newWidth || spec.externalHeight != newHeight) {
                throw new IllegalStateException(
                        "Cannot resize framebuffer to " + newWidth + "x" + newHeight +
                                " because external " + name + " is " + spec.externalWidth + "x" + spec.externalHeight
                );
            }
        }
    }

    private void destroyAttachments() {
        if (depthTexture != null) {
            closeTextureAttachment(depthTexture);
            depthTexture = null;
        }
        if (stencilTexture != null) {
            closeTextureAttachment(stencilTexture);
            stencilTexture = null;
        }
        if (depthStencilTexture != null) {
            closeTextureAttachment(depthStencilTexture);
            depthStencilTexture = null;
        }

        for (TextureAttachment attachment : colorAttachments) {
            closeTextureAttachment(attachment);
        }
        colorAttachments.clear();
        cachedDrawBuffers = null;

        // Guard on ID != 0 is sufficient; spec ownership was already checked
        // when the renderbuffer was created.
        if (depthRenderbufferId != 0) {
            if (depthRenderbufferSpec.owned) {
                GL45C.glDeleteRenderbuffers(depthRenderbufferId);
            }
            depthRenderbufferId = 0;
        }

        if (stencilRenderbufferId != 0) {
            if (stencilRenderbufferSpec.owned) {
                GL45C.glDeleteRenderbuffers(stencilRenderbufferId);
            }
            stencilRenderbufferId = 0;
        }

        if (depthStencilRenderbufferId != 0) {
            if (depthStencilRenderbufferSpec.owned) {
                GL45C.glDeleteRenderbuffers(depthStencilRenderbufferId);
            }
            depthStencilRenderbufferId = 0;
        }
    }

    private void closeTextureAttachment(TextureAttachment attachment) {
        attachment.close();
    }

    private void checkColorIndex(int index) {
        if (index < 0 || index >= colorAttachments.size()) {
            throw new IndexOutOfBoundsException(
                    "Invalid color attachment index " + index + " for framebuffer with " + colorAttachments.size() + " color attachments"
            );
        }
    }

    private void closeSilently() {
        try {
            close();
        } catch (Exception ignored) {
        }
    }

    public static String framebufferStatusName(int status) {
        return switch (status) {
            case GL30.GL_FRAMEBUFFER_COMPLETE -> "GL_FRAMEBUFFER_COMPLETE";
            case GL30.GL_FRAMEBUFFER_UNDEFINED -> "GL_FRAMEBUFFER_UNDEFINED";
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT";
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT";
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER -> "GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER";
            case GL30.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER -> "GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER";
            case GL30.GL_FRAMEBUFFER_UNSUPPORTED -> "GL_FRAMEBUFFER_UNSUPPORTED";
            case GL32.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE -> "GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE";
            case GL32.GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS -> "GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS";
            default -> "UNKNOWN_FRAMEBUFFER_STATUS_" + status;
        };
    }

    public enum AttachmentRole {
        COLOR,
        DEPTH,
        STENCIL,
        DEPTH_STENCIL
    }

    public enum TextureAttachKind {
        WHOLE_TEXTURE,
        LAYER
    }

    public enum TextureTarget {
        TEXTURE_2D(GL11.GL_TEXTURE_2D, false, false),
        TEXTURE_2D_ARRAY(GL30.GL_TEXTURE_2D_ARRAY, true, false),
        TEXTURE_CUBE_MAP(GL13.GL_TEXTURE_CUBE_MAP, true, false),
        TEXTURE_2D_MULTISAMPLE(GL32.GL_TEXTURE_2D_MULTISAMPLE, false, true),
        TEXTURE_2D_MULTISAMPLE_ARRAY(GL32.GL_TEXTURE_2D_MULTISAMPLE_ARRAY, true, true);

        public final int glTarget;
        public final boolean layered;
        public final boolean multisample;

        TextureTarget(int glTarget, boolean layered, boolean multisample) {
            this.glTarget = glTarget;
            this.layered = layered;
            this.multisample = multisample;
        }
    }

    public static final class Builder {
        private final int width;
        private final int height;

        private final List<TextureSpec> colorSpecs = new ArrayList<>();

        private TextureSpec depthTextureSpec;
        private TextureSpec stencilTextureSpec;
        private TextureSpec depthStencilTextureSpec;

        private RenderbufferSpec depthRenderbufferSpec;
        private RenderbufferSpec stencilRenderbufferSpec;
        private RenderbufferSpec depthStencilRenderbufferSpec;

        private Builder(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public Builder color(TextureSpec spec) {
            if (spec == null) {
                throw new IllegalArgumentException("Color spec cannot be null");
            }
            this.colorSpecs.add(spec);
            return this;
        }

        public Builder depthTexture(TextureSpec spec) {
            if (spec == null) {
                throw new IllegalArgumentException("Depth texture spec cannot be null");
            }
            this.depthTextureSpec = spec;
            return this;
        }

        public Builder stencilTexture(TextureSpec spec) {
            if (spec == null) {
                throw new IllegalArgumentException("Stencil texture spec cannot be null");
            }
            this.stencilTextureSpec = spec;
            return this;
        }

        public Builder depthStencilTexture(TextureSpec spec) {
            if (spec == null) {
                throw new IllegalArgumentException("Depth-stencil texture spec cannot be null");
            }
            this.depthStencilTextureSpec = spec;
            return this;
        }

        public Builder depthRenderbuffer(RenderbufferSpec spec) {
            if (spec == null) {
                throw new IllegalArgumentException("Depth renderbuffer spec cannot be null");
            }
            this.depthRenderbufferSpec = spec;
            return this;
        }

        public Builder stencilRenderbuffer(RenderbufferSpec spec) {
            if (spec == null) {
                throw new IllegalArgumentException("Stencil renderbuffer spec cannot be null");
            }
            this.stencilRenderbufferSpec = spec;
            return this;
        }

        public Builder depthStencilRenderbuffer(RenderbufferSpec spec) {
            if (spec == null) {
                throw new IllegalArgumentException("Depth-stencil renderbuffer spec cannot be null");
            }
            this.depthStencilRenderbufferSpec = spec;
            return this;
        }

        public FrameBuffer build() {
            return new FrameBuffer(this);
        }
    }

    public record TextureSpec(TextureTarget target, ITexture.Type type, int levels, int layers, int samples,
                              boolean fixedSampleLocations, int minFilter, int magFilter, int wrapS, int wrapT,
                              int wrapR, boolean compareMode, int compareFunc, ITexture.ComputeAccess computeAccess,
                              boolean owned, int existingId, int externalWidth, int externalHeight,
                              TextureAttachKind attachKind, int level, int layer) {

        public static TextureSpec texture2D(ITexture.Type type) {
                int filter = type.isInteger() ? GL11.GL_NEAREST : GL11.GL_LINEAR;
                return new TextureSpec(
                        TextureTarget.TEXTURE_2D,
                        type,
                        1,
                        1,
                        1,
                        true,
                        filter,
                        filter,
                        GL12.GL_CLAMP_TO_EDGE,
                        GL12.GL_CLAMP_TO_EDGE,
                        GL12.GL_CLAMP_TO_EDGE,
                        false,
                        GL11.GL_LEQUAL,
                        ITexture.ComputeAccess.READ_ONLY,
                        true,
                        0,
                        0,
                        0,
                        TextureAttachKind.WHOLE_TEXTURE,
                        0,
                        0
                );
            }

            public static TextureSpec texture2DMultisample(ITexture.Type type, int samples, boolean fixedSampleLocations) {
                return new TextureSpec(
                        TextureTarget.TEXTURE_2D_MULTISAMPLE,
                        type,
                        1,
                        1,
                        samples,
                        fixedSampleLocations,
                        GL11.GL_NEAREST,
                        GL11.GL_NEAREST,
                        GL12.GL_CLAMP_TO_EDGE,
                        GL12.GL_CLAMP_TO_EDGE,
                        GL12.GL_CLAMP_TO_EDGE,
                        false,
                        GL11.GL_LEQUAL,
                        ITexture.ComputeAccess.READ_ONLY,
                        true,
                        0,
                        0,
                        0,
                        TextureAttachKind.WHOLE_TEXTURE,
                        0,
                        0
                );
            }

            public static TextureSpec texture2DArray(ITexture.Type type, int layers) {
                int filter = type.isInteger() ? GL11.GL_NEAREST : GL11.GL_LINEAR;
                return new TextureSpec(
                        TextureTarget.TEXTURE_2D_ARRAY,
                        type,
                        1,
                        layers,
                        1,
                        true,
                        filter,
                        filter,
                        GL12.GL_CLAMP_TO_EDGE,
                        GL12.GL_CLAMP_TO_EDGE,
                        GL12.GL_CLAMP_TO_EDGE,
                        false,
                        GL11.GL_LEQUAL,
                        ITexture.ComputeAccess.READ_ONLY,
                        true,
                        0,
                        0,
                        0,
                        TextureAttachKind.WHOLE_TEXTURE,
                        0,
                        0
                );
            }

            public static TextureSpec texture2DArrayLayer(ITexture.Type type, int layers, int layer, int level) {
                int filter = type.isInteger() ? GL11.GL_NEAREST : GL11.GL_LINEAR;
                return new TextureSpec(
                        TextureTarget.TEXTURE_2D_ARRAY,
                        type,
                        Math.max(1, level + 1),
                        layers,
                        1,
                        true,
                        filter,
                        filter,
                        GL12.GL_CLAMP_TO_EDGE,
                        GL12.GL_CLAMP_TO_EDGE,
                        GL12.GL_CLAMP_TO_EDGE,
                        false,
                        GL11.GL_LEQUAL,
                        ITexture.ComputeAccess.READ_ONLY,
                        true,
                        0,
                        0,
                        0,
                        TextureAttachKind.LAYER,
                        level,
                        layer
                );
            }

            public static TextureSpec texture2DMultisampleArray(ITexture.Type type, int layers, int samples, boolean fixedSampleLocations) {
                return new TextureSpec(
                        TextureTarget.TEXTURE_2D_MULTISAMPLE_ARRAY,
                        type,
                        1,
                        layers,
                        samples,
                        fixedSampleLocations,
                        GL11.GL_NEAREST,
                        GL11.GL_NEAREST,
                        GL12.GL_CLAMP_TO_EDGE,
                        GL12.GL_CLAMP_TO_EDGE,
                        GL12.GL_CLAMP_TO_EDGE,
                        false,
                        GL11.GL_LEQUAL,
                        ITexture.ComputeAccess.READ_ONLY,
                        true,
                        0,
                        0,
                        0,
                        TextureAttachKind.WHOLE_TEXTURE,
                        0,
                        0
                );
            }

            public static TextureSpec texture2DMultisampleArrayLayer(ITexture.Type type, int layers, int samples, boolean fixedSampleLocations, int layer) {
                return new TextureSpec(
                        TextureTarget.TEXTURE_2D_MULTISAMPLE_ARRAY,
                        type,
                        1,
                        layers,
                        samples,
                        fixedSampleLocations,
                        GL11.GL_NEAREST,
                        GL11.GL_NEAREST,
                        GL12.GL_CLAMP_TO_EDGE,
                        GL12.GL_CLAMP_TO_EDGE,
                        GL12.GL_CLAMP_TO_EDGE,
                        false,
                        GL11.GL_LEQUAL,
                        ITexture.ComputeAccess.READ_ONLY,
                        true,
                        0,
                        0,
                        0,
                        TextureAttachKind.LAYER,
                        0,
                        layer
                );
            }

            public static TextureSpec cubeMap(ITexture.Type type) {
                int filter = type.isInteger() ? GL11.GL_NEAREST : GL11.GL_LINEAR;
                return new TextureSpec(
                        TextureTarget.TEXTURE_CUBE_MAP,
                        type,
                        1,
                        6,
                        1,
                        true,
                        filter,
                        filter,
                        GL12.GL_CLAMP_TO_EDGE,
                        GL12.GL_CLAMP_TO_EDGE,
                        GL12.GL_CLAMP_TO_EDGE,
                        false,
                        GL11.GL_LEQUAL,
                        ITexture.ComputeAccess.READ_ONLY,
                        true,
                        0,
                        0,
                        0,
                        TextureAttachKind.WHOLE_TEXTURE,
                        0,
                        0
                );
            }

            public static TextureSpec cubeFace(ITexture.Type type, int face, int level) {
                int filter = type.isInteger() ? GL11.GL_NEAREST : GL11.GL_LINEAR;
                return new TextureSpec(
                        TextureTarget.TEXTURE_CUBE_MAP,
                        type,
                        Math.max(1, level + 1),
                        6,
                        1,
                        true,
                        filter,
                        filter,
                        GL12.GL_CLAMP_TO_EDGE,
                        GL12.GL_CLAMP_TO_EDGE,
                        GL12.GL_CLAMP_TO_EDGE,
                        false,
                        GL11.GL_LEQUAL,
                        ITexture.ComputeAccess.READ_ONLY,
                        true,
                        0,
                        0,
                        0,
                        TextureAttachKind.LAYER,
                        level,
                        face
                );
            }

            public static TextureSpec external(TextureTarget target,
                                               ITexture.Type type,
                                               int existingId,
                                               int width,
                                               int height,
                                               int levels,
                                               int layers,
                                               int samples,
                                               TextureAttachKind attachKind,
                                               int level,
                                               int layer,
                                               ITexture.ComputeAccess access) {
                return new TextureSpec(
                        target,
                        type,
                        levels,
                        layers,
                        samples,
                        true,
                        GL11.GL_NEAREST,
                        GL11.GL_NEAREST,
                        GL12.GL_CLAMP_TO_EDGE,
                        GL12.GL_CLAMP_TO_EDGE,
                        GL12.GL_CLAMP_TO_EDGE,
                        false,
                        GL11.GL_LEQUAL,
                        access == null ? ITexture.ComputeAccess.READ_ONLY : access,
                        false,
                        existingId,
                        width,
                        height,
                        attachKind,
                        level,
                        layer
                );
            }

            public static TextureSpec depth2D(ITexture.Type type, boolean compareMode) {
                return new TextureSpec(
                        TextureTarget.TEXTURE_2D,
                        type,
                        1,
                        1,
                        1,
                        true,
                        GL11.GL_NEAREST,
                        GL11.GL_NEAREST,
                        GL12.GL_CLAMP_TO_EDGE,
                        GL12.GL_CLAMP_TO_EDGE,
                        GL12.GL_CLAMP_TO_EDGE,
                        compareMode,
                        GL11.GL_LEQUAL,
                        ITexture.ComputeAccess.READ_ONLY,
                        true,
                        0,
                        0,
                        0,
                        TextureAttachKind.WHOLE_TEXTURE,
                        0,
                        0
                );
            }

            public TextureSpec withLevels(int levels) {
                return new TextureSpec(target, type, levels, layers, samples, fixedSampleLocations, minFilter, magFilter,
                        wrapS, wrapT, wrapR, compareMode, compareFunc, computeAccess, owned, existingId, externalWidth,
                        externalHeight, attachKind, level, layer);
            }

            public TextureSpec withFilters(int minFilter, int magFilter) {
                return new TextureSpec(target, type, levels, layers, samples, fixedSampleLocations, minFilter, magFilter,
                        wrapS, wrapT, wrapR, compareMode, compareFunc, computeAccess, owned, existingId, externalWidth,
                        externalHeight, attachKind, level, layer);
            }

            public TextureSpec withWrap(int wrapS, int wrapT, int wrapR) {
                return new TextureSpec(target, type, levels, layers, samples, fixedSampleLocations, minFilter, magFilter,
                        wrapS, wrapT, wrapR, compareMode, compareFunc, computeAccess, owned, existingId, externalWidth,
                        externalHeight, attachKind, level, layer);
            }

            public TextureSpec withCompareMode(boolean compareMode, int compareFunc) {
                return new TextureSpec(target, type, levels, layers, samples, fixedSampleLocations, minFilter, magFilter,
                        wrapS, wrapT, wrapR, compareMode, compareFunc, computeAccess, owned, existingId, externalWidth,
                        externalHeight, attachKind, level, layer);
            }

            public TextureSpec withComputeAccess(ITexture.ComputeAccess access) {
                return new TextureSpec(target, type, levels, layers, samples, fixedSampleLocations, minFilter, magFilter,
                        wrapS, wrapT, wrapR, compareMode, compareFunc, access, owned, existingId, externalWidth,
                        externalHeight, attachKind, level, layer);
            }
        }

    public record RenderbufferSpec(ITexture.Type format, int samples, boolean owned, int existingId, int externalWidth,
                                   int externalHeight) {

        public static RenderbufferSpec depth24() {
                return new RenderbufferSpec(ITexture.Type.DEPTH24, 1, true, 0, 0, 0);
            }

            public static RenderbufferSpec depth24Stencil8() {
                return new RenderbufferSpec(ITexture.Type.DEPTH24_STENCIL8, 1, true, 0, 0, 0);
            }

            public static RenderbufferSpec depth32fStencil8() {
                return new RenderbufferSpec(ITexture.Type.DEPTH32F_STENCIL8, 1, true, 0, 0, 0);
            }

            public static RenderbufferSpec stencil8() {
                return new RenderbufferSpec(ITexture.Type.STENCIL8, 1, true, 0, 0, 0);
            }

            public static RenderbufferSpec multisample(ITexture.Type format, int samples) {
                return new RenderbufferSpec(format, samples, true, 0, 0, 0);
            }

            public static RenderbufferSpec external(ITexture.Type format, int existingId, int width, int height, int samples) {
                return new RenderbufferSpec(format, samples, false, existingId, width, height);
            }
        }

    public record TextureAttachment(int id, int width, int height, Type type, ComputeAccess computeAccess,
                                    int textureTarget, boolean multisample, boolean owned) implements ITexture {

        @Override
            public void bind(int slot) {
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + slot);
                GL11.glBindTexture(textureTarget, id);
            }

            @Override
            public ComputeAccess access() {
                return computeAccess;
            }

            public void bindImage(int unit, int level, boolean layered) {
                if (multisample) {
                    throw new IllegalStateException(
                            "OpenGL does not support image load/store for multisample textures (GL_TEXTURE_2D_MULTISAMPLE / GL_TEXTURE_2D_MULTISAMPLE_ARRAY)"
                    );
                }
                GL42.glBindImageTexture(unit, id, level, layered, 0, computeAccess.getValue(), type.internalFormat);
            }

            @Override
            public void close() {
                if (owned) {
                    GL45C.glDeleteTextures(id);
                }
            }
        }
}