package gg.generations.rarecandy.renderer.textures;

import org.lwjgl.opengl.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A DSA (GL 4.5) framebuffer object with owned or externally-supplied attachments.
 *
 * <h2>Sizing</h2>
 * {@link #width()}/{@link #height()} are the <i>allocation</i> size: owned textures are allocated
 * with their base level at this size, and external attachments must match it exactly.
 * {@link #renderWidth()}/{@link #renderHeight()} are the <i>effective</i> render area, which is the
 * minimum over all attachments of their level-adjusted size. These differ only when an attachment is
 * bound at a mip level &gt; 0. Viewport and blit rectangles use the render size.
 *
 * <h2>Lifetime</h2>
 * {@link #close()} is idempotent. After close, every method that touches GL throws
 * {@link IllegalStateException}. If construction or {@link #resize} fails partway, the framebuffer
 * closes itself before rethrowing, so no GL objects leak.
 *
 * <p>Not thread-safe; all methods must be called on the thread owning the GL context.
 */
public final class FrameBuffer implements AutoCloseable {
    // framebufferId is final — the GL object is never recreated, only its attachments are.
    private final int framebufferId;

    // Allocation size; mutable to support resize().
    private int width;
    private int height;

    // Effective render area (min level-adjusted size over all attachments). Recomputed on resize.
    private int renderWidth;
    private int renderHeight;

    private boolean closed;

    private final List<TextureSpec> colorSpecs;
    private final List<TextureAttachment> colorAttachments = new ArrayList<>();

    // [GL_COLOR_ATTACHMENT0 .. GL_COLOR_ATTACHMENTn-1], rebuilt in allocateAll().
    // Plain int[]: LWJGL stack-allocates for the array overload, so there is no off-heap buffer to track.
    private int[] cachedDrawBuffers;

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

    // Scratch storage for the glClearNamedFramebuffer* calls. Single-threaded by contract.
    private final float[] scratchColorFloat = new float[4];
    private final int[] scratchColorInt = new int[4];
    private final int[] scratchStencil = new int[1];
    private final float[] scratchDepth = new float[1];

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
            throw new IllegalArgumentException(
                    "Framebuffer size must be > 0, got " + builder.width + "x" + builder.height);
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

        // Validate the whole configuration before any GL object exists, so a bad spec cannot leak.
        validateConfiguration();

        this.framebufferId = GL45C.glCreateFramebuffers();

        try {
            allocateAll();
        } catch (RuntimeException | Error ex) {
            closeSilently(ex);
            throw ex;
        }
    }

    public static Builder builder(int width, int height) {
        return new Builder(width, height);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    public int getFramebufferId() {
        ensureOpen();
        return framebufferId;
    }

    /** Allocation width. External attachments must match this exactly. */
    public int width() {
        return width;
    }

    /** Allocation height. External attachments must match this exactly. */
    public int height() {
        return height;
    }

    /** Effective render width — equals {@link #width()} unless an attachment is bound at mip level &gt; 0. */
    public int renderWidth() {
        return renderWidth;
    }

    /** Effective render height — equals {@link #height()} unless an attachment is bound at mip level &gt; 0. */
    public int renderHeight() {
        return renderHeight;
    }

    public boolean isClosed() {
        return closed;
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

    /** True if anything is attached that can hold depth (texture, renderbuffer, or a combined depth-stencil). */
    public boolean hasDepth() {
        return depthTexture != null || depthStencilTexture != null
                || depthRenderbufferId != 0 || depthStencilRenderbufferId != 0;
    }

    /** True if anything is attached that can hold stencil (texture, renderbuffer, or a combined depth-stencil). */
    public boolean hasStencil() {
        return stencilTexture != null || depthStencilTexture != null
                || stencilRenderbufferId != 0 || depthStencilRenderbufferId != 0;
    }

    public TextureAttachment getColorAttachment(int index) {
        checkColorIndex(index);
        return colorAttachments.get(index);
    }

    /**
     * Returns a snapshot of the current color attachments. The returned list is not a live view:
     * a later {@link #resize} replaces the attachments and invalidates the returned records.
     */
    public List<TextureAttachment> getColorAttachments() {
        return List.copyOf(colorAttachments);
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

    // ------------------------------------------------------------------------
    // Binding
    // ------------------------------------------------------------------------

    public void bindFramebuffer() {
        ensureOpen();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferId);
    }

    public void bindForDraw() {
        ensureOpen();
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, framebufferId);
    }

    public void bindForRead() {
        ensureOpen();
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, framebufferId);
    }

    public void unbind() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    public static void unbindDraw() {
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
    }

    public static void unbindRead() {
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);
    }

    /** Binds this framebuffer and sets the viewport to the effective render area. */
    public void bindAndSetViewport() {
        ensureOpen();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferId);
        GL11.glViewport(0, 0, renderWidth, renderHeight);
    }

    // ------------------------------------------------------------------------
    // Draw / read buffer selection
    // ------------------------------------------------------------------------

    /**
     * Enables exactly the listed color attachments, positionally: attachment {@code i} receives
     * fragment output {@code i}, every other output is discarded.
     *
     * <p>This is the only mapping OpenGL permits for {@code glDrawBuffers} — the i-th entry must be
     * {@code GL_COLOR_ATTACHMENTi} or {@code GL_NONE}, so the list cannot reorder or compact
     * attachments. Passing no arguments (or {@code null}) disables all color output. Duplicates are
     * harmless. To route fragment output 0 to some other attachment, use {@link #setSingleDrawBuffer}.
     */
    public void setDrawBuffers(int... attachments) {
        ensureOpen();

        if (attachments == null || attachments.length == 0) {
            GL45C.glNamedFramebufferDrawBuffer(framebufferId, GL11.GL_NONE);
            return;
        }

        int[] bufs = new int[colorAttachments.size()];
        Arrays.fill(bufs, GL11.GL_NONE);
        for (int attachment : attachments) {
            checkColorIndex(attachment);
            bufs[attachment] = GL30.GL_COLOR_ATTACHMENT0 + attachment;
        }
        GL45C.glNamedFramebufferDrawBuffers(framebufferId, bufs);
    }

    /**
     * Routes fragment output 0 to {@code attachment} and disables every other output.
     * Unlike {@link #setDrawBuffers}, the attachment index need not match the output index.
     */
    public void setSingleDrawBuffer(int attachment) {
        ensureOpen();
        checkColorIndex(attachment);
        GL45C.glNamedFramebufferDrawBuffer(framebufferId, GL30.GL_COLOR_ATTACHMENT0 + attachment);
    }

    /** Resets draw buffers to all color attachments in order (output i to attachment i). */
    public void setDrawAll() {
        ensureOpen();

        if (cachedDrawBuffers == null) {
            GL45C.glNamedFramebufferDrawBuffer(framebufferId, GL11.GL_NONE);
            return;
        }
        GL45C.glNamedFramebufferDrawBuffers(framebufferId, cachedDrawBuffers);
    }

    public void setReadBuffer(int attachment) {
        ensureOpen();
        checkColorIndex(attachment);
        GL45C.glNamedFramebufferReadBuffer(framebufferId, GL30.GL_COLOR_ATTACHMENT0 + attachment);
    }

    /** Disables both color writes and color reads for this framebuffer. */
    public void disableColorOutputs() {
        ensureOpen();
        GL45C.glNamedFramebufferDrawBuffer(framebufferId, GL11.GL_NONE);
        GL45C.glNamedFramebufferReadBuffer(framebufferId, GL11.GL_NONE);
    }

    // ------------------------------------------------------------------------
    // Clears
    // ------------------------------------------------------------------------

    /**
     * Clears a draw buffer with float values. {@code drawBuffer} is the <i>draw buffer index</i>,
     * which equals the attachment index under the default mapping and after {@link #setDrawBuffers},
     * but is always 0 after {@link #setSingleDrawBuffer}.
     *
     * <p>Use {@link #clearColorInt} for signed-integer formats and {@link #clearColorUInt} for
     * unsigned-integer formats; clearing an integer attachment with this method is undefined in GL.
     */
    public void clearColor(int drawBuffer, float r, float g, float b, float a) {
        ensureOpen();
        checkDrawBufferIndex(drawBuffer);
        scratchColorFloat[0] = r;
        scratchColorFloat[1] = g;
        scratchColorFloat[2] = b;
        scratchColorFloat[3] = a;
        GL45C.glClearNamedFramebufferfv(framebufferId, GL11.GL_COLOR, drawBuffer, scratchColorFloat);
    }

    /** Clears a draw buffer backed by a signed-integer format. See {@link #clearColor} for index semantics. */
    public void clearColorInt(int drawBuffer, int x, int y, int z, int w) {
        ensureOpen();
        checkDrawBufferIndex(drawBuffer);
        scratchColorInt[0] = x;
        scratchColorInt[1] = y;
        scratchColorInt[2] = z;
        scratchColorInt[3] = w;
        GL45C.glClearNamedFramebufferiv(framebufferId, GL11.GL_COLOR, drawBuffer, scratchColorInt);
    }

    /** Clears a draw buffer backed by an unsigned-integer format. See {@link #clearColor} for index semantics. */
    public void clearColorUInt(int drawBuffer, int x, int y, int z, int w) {
        ensureOpen();
        checkDrawBufferIndex(drawBuffer);
        scratchColorInt[0] = x;
        scratchColorInt[1] = y;
        scratchColorInt[2] = z;
        scratchColorInt[3] = w;
        GL45C.glClearNamedFramebufferuiv(framebufferId, GL11.GL_COLOR, drawBuffer, scratchColorInt);
    }

    public void clearDepth(float depth) {
        ensureOpen();
        if (!hasDepth()) {
            throw new IllegalStateException("Framebuffer has no depth attachment to clear");
        }
        this.scratchDepth[0] = depth;
        GL45C.glClearNamedFramebufferfv(framebufferId, GL11.GL_DEPTH, 0, this.scratchDepth);
    }

    public void clearStencil(int stencil) {
        ensureOpen();
        if (!hasStencil()) {
            throw new IllegalStateException("Framebuffer has no stencil attachment to clear");
        }
        this.scratchStencil[0] = stencil;
        GL45C.glClearNamedFramebufferiv(framebufferId, GL11.GL_STENCIL, 0, this.scratchStencil);
    }

    public void clearDepthStencil(float depth, int stencil) {
        ensureOpen();
        if (!hasDepth() && !hasStencil()) {
            throw new IllegalStateException("Framebuffer has no depth or stencil attachment to clear");
        }
        GL45C.glClearNamedFramebufferfi(framebufferId, GL30.GL_DEPTH_STENCIL, 0, depth, stencil);
    }

    // ------------------------------------------------------------------------
    // Blits
    // ------------------------------------------------------------------------

    /**
     * Blits the effective render area of this framebuffer to {@code target}.
     *
     * <p>{@code srcAttachment} and {@code dstAttachment} are only consulted when {@code mask}
     * includes {@code GL_COLOR_BUFFER_BIT}; a depth-only or stencil-only blit works on framebuffers
     * with no color attachments at all.
     *
     * <p><b>Side effect:</b> for a color blit, this framebuffer's read buffer is left pointing at
     * {@code srcAttachment} and {@code target}'s draw buffer at {@code dstAttachment}. Call
     * {@link #setDrawAll()} / {@link #setReadBuffer} afterward if you need the previous state back.
     */
    public void blitTo(FrameBuffer target, int srcAttachment, int dstAttachment, int mask, int filter) {
        ensureOpen();
        if (target == null) {
            throw new IllegalArgumentException("Target framebuffer cannot be null");
        }
        target.ensureOpen();
        validateBlitParams(mask, filter);

        if ((mask & GL11.GL_COLOR_BUFFER_BIT) != 0) {
            checkColorIndex(srcAttachment);
            target.checkColorIndex(dstAttachment);
            GL45C.glNamedFramebufferReadBuffer(this.framebufferId, GL30.GL_COLOR_ATTACHMENT0 + srcAttachment);
            GL45C.glNamedFramebufferDrawBuffer(target.framebufferId, GL30.GL_COLOR_ATTACHMENT0 + dstAttachment);
        }

        GL45C.glBlitNamedFramebuffer(
                this.framebufferId,
                target.framebufferId,
                0, 0, this.renderWidth, this.renderHeight,
                0, 0, target.renderWidth, target.renderHeight,
                mask,
                filter
        );
    }

    /**
     * Blits the effective render area of this framebuffer to the default framebuffer (screen).
     *
     * <p>{@code srcAttachment} is only consulted when {@code mask} includes
     * {@code GL_COLOR_BUFFER_BIT}.
     *
     * <p><b>Side effect:</b> for a color blit, this framebuffer's read buffer is left pointing at
     * {@code srcAttachment}. Call {@link #setReadBuffer} afterward if you need the previous state back.
     */
    public void blitToScreen(int srcAttachment, int targetWidth, int targetHeight, int mask, int filter) {
        ensureOpen();
        if (targetWidth <= 0 || targetHeight <= 0) {
            throw new IllegalArgumentException(
                    "Blit target size must be > 0, got " + targetWidth + "x" + targetHeight);
        }
        validateBlitParams(mask, filter);

        if ((mask & GL11.GL_COLOR_BUFFER_BIT) != 0) {
            checkColorIndex(srcAttachment);
            GL45C.glNamedFramebufferReadBuffer(this.framebufferId, GL30.GL_COLOR_ATTACHMENT0 + srcAttachment);
        }

        GL45C.glBlitNamedFramebuffer(
                this.framebufferId,
                0,
                0, 0, this.renderWidth, this.renderHeight,
                0, 0, targetWidth, targetHeight,
                mask,
                filter
        );
    }

    // ------------------------------------------------------------------------
    // Resize / completeness / lifetime
    // ------------------------------------------------------------------------

    /**
     * Reallocates every owned attachment at the new size. External attachments are not reallocated,
     * so their declared size must already match the new size.
     *
     * <p>Draw and read buffer selection is reset to the default (all attachments in order, read from
     * attachment 0). Previously returned {@link TextureAttachment} records are invalidated.
     *
     * <p>If reallocation fails, the framebuffer closes itself before rethrowing.
     *
     * @throws IllegalStateException if an external attachment's size does not match the new size
     */
    public void resize(int newWidth, int newHeight) {
        ensureOpen();
        if (newWidth <= 0 || newHeight <= 0) {
            throw new IllegalArgumentException("Framebuffer size must be > 0, got " + newWidth + "x" + newHeight);
        }
        if (this.width == newWidth && this.height == newHeight) {
            return;
        }

        int previousWidth = this.width;
        int previousHeight = this.height;
        this.width = newWidth;
        this.height = newHeight;

        try {
            validateConfiguration();
        } catch (RuntimeException ex) {
            // Nothing was destroyed yet — roll back and leave the framebuffer usable.
            this.width = previousWidth;
            this.height = previousHeight;
            computeRenderSize();
            throw ex;
        }

        destroyAttachments(true);

        try {
            allocateAll();
        } catch (RuntimeException | Error ex) {
            closeSilently(ex);
            throw ex;
        }
    }

    /**
     * Throws {@link IllegalStateException} if the framebuffer is not complete.
     * @see #isComplete()
     */
    public void checkComplete() {
        ensureOpen();
        int status = GL45C.glCheckNamedFramebufferStatus(framebufferId, GL30.GL_FRAMEBUFFER);
        if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException(
                    "Framebuffer " + framebufferId + " (" + width + "x" + height + ") is not complete: "
                            + framebufferStatusName(status));
        }
    }

    /**
     * Returns {@code true} if the framebuffer is complete, {@code false} otherwise (including after
     * {@link #close()}). Useful for debug overlays or conditional logic without try/catch.
     */
    public boolean isComplete() {
        return !closed
                && GL45C.glCheckNamedFramebufferStatus(framebufferId, GL30.GL_FRAMEBUFFER) == GL30.GL_FRAMEBUFFER_COMPLETE;
    }

    /** Deletes every owned attachment and the framebuffer itself. Idempotent. */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        // No need to detach first: deleting the framebuffer drops every attachment with it.
        destroyAttachments(false);
        GL45C.glDeleteFramebuffers(framebufferId);
    }

    // ------------------------------------------------------------------------
    // Allocation
    // ------------------------------------------------------------------------

    private void allocateAll() {
        int colorIndex = 0;
        for (TextureSpec spec : colorSpecs) {
            TextureAttachment attachment = createTextureAttachment(spec);
            attachTexture(GL30.GL_COLOR_ATTACHMENT0 + colorIndex, spec, attachment.id());
            colorAttachments.add(attachment);
            colorIndex++;
        }

        if (depthTextureSpec != null) {
            depthTexture = createTextureAttachment(depthTextureSpec);
            attachTexture(GL30.GL_DEPTH_ATTACHMENT, depthTextureSpec, depthTexture.id());
        }

        if (stencilTextureSpec != null) {
            stencilTexture = createTextureAttachment(stencilTextureSpec);
            attachTexture(GL30.GL_STENCIL_ATTACHMENT, stencilTextureSpec, stencilTexture.id());
        }

        if (depthStencilTextureSpec != null) {
            depthStencilTexture = createTextureAttachment(depthStencilTextureSpec);
            attachTexture(GL30.GL_DEPTH_STENCIL_ATTACHMENT, depthStencilTextureSpec, depthStencilTexture.id());
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
            cachedDrawBuffers = null;
            GL45C.glNamedFramebufferDrawBuffer(framebufferId, GL11.GL_NONE);
            GL45C.glNamedFramebufferReadBuffer(framebufferId, GL11.GL_NONE);
        } else {
            cachedDrawBuffers = new int[colorAttachments.size()];
            for (int i = 0; i < cachedDrawBuffers.length; i++) {
                cachedDrawBuffers[i] = GL30.GL_COLOR_ATTACHMENT0 + i;
            }
            GL45C.glNamedFramebufferDrawBuffers(framebufferId, cachedDrawBuffers);
            GL45C.glNamedFramebufferReadBuffer(framebufferId, GL30.GL_COLOR_ATTACHMENT0);
        }

        checkComplete();
    }

    private TextureAttachment createTextureAttachment(TextureSpec spec) {
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
                specWidth(spec),
                specHeight(spec),
                spec.type,
                spec.computeAccess,
                spec.target.glTarget,
                spec.target.multisample,
                spec.owned
        );
    }

    private void attachTexture(int attachmentPoint, TextureSpec spec, int textureId) {
        if (spec.attachKind == TextureAttachKind.WHOLE_TEXTURE) {
            GL45C.glNamedFramebufferTexture(framebufferId, attachmentPoint, textureId, spec.level);
        } else {
            GL45C.glNamedFramebufferTextureLayer(framebufferId, attachmentPoint, textureId, spec.level, spec.layer);
        }
    }

    private int createRenderbuffer(RenderbufferSpec spec) {
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
        if (spec.target.multisample) {
            // Multisample textures reject every sampler parameter.
            return;
        }

        GL45C.glTextureParameteri(textureId, GL11.GL_TEXTURE_MIN_FILTER, spec.minFilter);
        GL45C.glTextureParameteri(textureId, GL11.GL_TEXTURE_MAG_FILTER, spec.magFilter);
        GL45C.glTextureParameteri(textureId, GL11.GL_TEXTURE_WRAP_S, spec.wrapS);
        GL45C.glTextureParameteri(textureId, GL11.GL_TEXTURE_WRAP_T, spec.wrapT);

        if (spec.target == TextureTarget.TEXTURE_2D_ARRAY || spec.target == TextureTarget.TEXTURE_CUBE_MAP) {
            GL45C.glTextureParameteri(textureId, GL12.GL_TEXTURE_WRAP_R, spec.wrapR);
        }

        if (spec.compareMode) {
            // GL_COMPARE_R_TO_TEXTURE is the GL 1.4 name for GL 3.0's GL_COMPARE_REF_TO_TEXTURE; same value.
            GL45C.glTextureParameteri(textureId, GL14.GL_TEXTURE_COMPARE_MODE, GL14.GL_COMPARE_R_TO_TEXTURE);
            GL45C.glTextureParameteri(textureId, GL14.GL_TEXTURE_COMPARE_FUNC, spec.compareFunc);
        }
    }

    // ------------------------------------------------------------------------
    // Validation
    // ------------------------------------------------------------------------

    /**
     * Validates the whole configuration against the current {@link #width}/{@link #height} and
     * recomputes the effective render size. Touches no GL objects, so it is safe to call before the
     * framebuffer exists and before destroying anything on resize.
     */
    private void validateConfiguration() {
        if (colorSpecs.isEmpty()
                && depthTextureSpec == null
                && stencilTextureSpec == null
                && depthStencilTextureSpec == null
                && depthRenderbufferSpec == null
                && stencilRenderbufferSpec == null
                && depthStencilRenderbufferSpec == null) {
            throw new IllegalArgumentException("Framebuffer must have at least one attachment");
        }

        int maxColorAttachments = GL11.glGetInteger(GL30.GL_MAX_COLOR_ATTACHMENTS);
        int maxDrawBuffers = GL11.glGetInteger(GL20.GL_MAX_DRAW_BUFFERS);
        int limit = Math.min(maxColorAttachments, maxDrawBuffers);
        if (colorSpecs.size() > limit) {
            throw new IllegalArgumentException(
                    "Framebuffer requests " + colorSpecs.size() + " color attachments but this context supports "
                            + limit + " (GL_MAX_COLOR_ATTACHMENTS=" + maxColorAttachments
                            + ", GL_MAX_DRAW_BUFFERS=" + maxDrawBuffers + ")");
        }

        validateAttachmentSlots();

        for (int i = 0; i < colorSpecs.size(); i++) {
            validateTextureSpec(colorSpecs.get(i), AttachmentRole.COLOR, "color texture " + i);
        }
        validateTextureSpec(depthTextureSpec, AttachmentRole.DEPTH, "depth texture");
        validateTextureSpec(stencilTextureSpec, AttachmentRole.STENCIL, "stencil texture");
        validateTextureSpec(depthStencilTextureSpec, AttachmentRole.DEPTH_STENCIL, "depth-stencil texture");

        validateRenderbufferSpec(depthRenderbufferSpec, "depth renderbuffer");
        validateRenderbufferSpec(stencilRenderbufferSpec, "stencil renderbuffer");
        validateRenderbufferSpec(depthStencilRenderbufferSpec, "depth-stencil renderbuffer");

        validateSampleConsistency();

        computeRenderSize();
    }

    /**
     * OpenGL has exactly one depth attachment point and one stencil attachment point, and a combined
     * depth-stencil attachment claims both. Two specs targeting the same point would silently
     * overwrite each other, leaving the loser allocated but invisible.
     */
    private void validateAttachmentSlots() {
        List<String> depthClaims = new ArrayList<>();
        if (depthTextureSpec != null) depthClaims.add("depthTexture");
        if (depthRenderbufferSpec != null) depthClaims.add("depthRenderbuffer");
        if (depthStencilTextureSpec != null) depthClaims.add("depthStencilTexture");
        if (depthStencilRenderbufferSpec != null) depthClaims.add("depthStencilRenderbuffer");
        if (depthClaims.size() > 1) {
            throw new IllegalArgumentException("Multiple specs claim the depth attachment point: " + depthClaims);
        }

        List<String> stencilClaims = new ArrayList<>();
        if (stencilTextureSpec != null) stencilClaims.add("stencilTexture");
        if (stencilRenderbufferSpec != null) stencilClaims.add("stencilRenderbuffer");
        if (depthStencilTextureSpec != null) stencilClaims.add("depthStencilTexture");
        if (depthStencilRenderbufferSpec != null) stencilClaims.add("depthStencilRenderbuffer");
        if (stencilClaims.size() > 1) {
            throw new IllegalArgumentException("Multiple specs claim the stencil attachment point: " + stencilClaims);
        }
    }

    /** All attachments must agree on sample count, or the framebuffer is INCOMPLETE_MULTISAMPLE. */
    private void validateSampleConsistency() {
        Map<String, Integer> samplesByAttachment = new LinkedHashMap<>();
        for (int i = 0; i < colorSpecs.size(); i++) {
            samplesByAttachment.put("color texture " + i, colorSpecs.get(i).samples);
        }
        if (depthTextureSpec != null) samplesByAttachment.put("depth texture", depthTextureSpec.samples);
        if (stencilTextureSpec != null) samplesByAttachment.put("stencil texture", stencilTextureSpec.samples);
        if (depthStencilTextureSpec != null) samplesByAttachment.put("depth-stencil texture", depthStencilTextureSpec.samples);
        if (depthRenderbufferSpec != null) samplesByAttachment.put("depth renderbuffer", depthRenderbufferSpec.samples);
        if (stencilRenderbufferSpec != null) samplesByAttachment.put("stencil renderbuffer", stencilRenderbufferSpec.samples);
        if (depthStencilRenderbufferSpec != null) samplesByAttachment.put("depth-stencil renderbuffer", depthStencilRenderbufferSpec.samples);

        String referenceName = null;
        int referenceSamples = -1;
        for (Map.Entry<String, Integer> entry : samplesByAttachment.entrySet()) {
            if (referenceName == null) {
                referenceName = entry.getKey();
                referenceSamples = entry.getValue();
            } else if (entry.getValue() != referenceSamples) {
                throw new IllegalArgumentException(
                        "All attachments must have the same sample count: " + referenceName + " has "
                                + referenceSamples + " but " + entry.getKey() + " has " + entry.getValue());
            }
        }
    }

    /**
     * The effective render area is the minimum over all attachments of their size, which for an owned
     * texture bound at mip level N is the level-N size rather than the allocation size.
     */
    private void computeRenderSize() {
        int w = Integer.MAX_VALUE;
        int h = Integer.MAX_VALUE;

        for (TextureSpec spec : colorSpecs) {
            w = Math.min(w, specWidth(spec));
            h = Math.min(h, specHeight(spec));
        }
        for (TextureSpec spec : new TextureSpec[]{depthTextureSpec, stencilTextureSpec, depthStencilTextureSpec}) {
            if (spec != null) {
                w = Math.min(w, specWidth(spec));
                h = Math.min(h, specHeight(spec));
            }
        }
        for (RenderbufferSpec spec : new RenderbufferSpec[]{depthRenderbufferSpec, stencilRenderbufferSpec, depthStencilRenderbufferSpec}) {
            if (spec != null) {
                w = Math.min(w, spec.owned ? width : spec.externalWidth);
                h = Math.min(h, spec.owned ? height : spec.externalHeight);
            }
        }

        renderWidth = w == Integer.MAX_VALUE ? width : w;
        renderHeight = h == Integer.MAX_VALUE ? height : h;
    }

    private int specWidth(TextureSpec spec) {
        return spec.owned ? Math.max(1, width >> spec.level) : spec.externalWidth;
    }

    private int specHeight(TextureSpec spec) {
        return spec.owned ? Math.max(1, height >> spec.level) : spec.externalHeight;
    }

    private void validateTextureSpec(TextureSpec spec, AttachmentRole role, String what) {
        if (spec == null) {
            return;
        }
        if (spec.type == null) {
            throw new IllegalArgumentException(what + ": texture type cannot be null");
        }
        if (spec.target == null) {
            throw new IllegalArgumentException(what + ": texture target cannot be null");
        }
        if (spec.attachKind == null) {
            throw new IllegalArgumentException(what + ": attach kind cannot be null");
        }
        if (spec.levels <= 0) {
            throw new IllegalArgumentException(what + ": texture levels must be > 0, got " + spec.levels);
        }
        if (spec.level < 0 || spec.level >= spec.levels) {
            throw new IllegalArgumentException(
                    what + ": attach level " + spec.level + " is out of range for a texture with "
                            + spec.levels + " level(s)");
        }
        if (spec.target.multisample) {
            if (spec.samples <= 0) {
                throw new IllegalArgumentException(what + ": multisample texture must have samples > 0");
            }
            if (spec.levels != 1 || spec.level != 0) {
                throw new IllegalArgumentException(what + ": multisample textures have exactly one level");
            }
        } else if (spec.samples != 1) {
            throw new IllegalArgumentException(what + ": non-multisample texture must use samples=1, got " + spec.samples);
        }

        if (spec.target == TextureTarget.TEXTURE_2D_ARRAY || spec.target == TextureTarget.TEXTURE_2D_MULTISAMPLE_ARRAY) {
            if (spec.layers <= 0) {
                throw new IllegalArgumentException(what + ": array texture must have layers > 0, got " + spec.layers);
            }
        }
        if (spec.target == TextureTarget.TEXTURE_CUBE_MAP && spec.layers != 6) {
            throw new IllegalArgumentException(what + ": cube map texture must use layers=6, got " + spec.layers);
        }

        if (spec.attachKind == TextureAttachKind.LAYER) {
            if (!spec.target.layered) {
                throw new IllegalArgumentException(what + ": target " + spec.target + " does not support layer attachment");
            }
            int layerCount = spec.target == TextureTarget.TEXTURE_CUBE_MAP ? 6 : spec.layers;
            if (spec.layer < 0 || spec.layer >= layerCount) {
                throw new IllegalArgumentException(
                        what + ": layer " + spec.layer + " is out of range [0, " + (layerCount - 1) + "]");
            }
        }

        if (!spec.owned && spec.existingId == 0) {
            throw new IllegalArgumentException(what + ": external texture id must be non-zero");
        }

        int attachmentWidth = specWidth(spec);
        int attachmentHeight = specHeight(spec);
        if (spec.target == TextureTarget.TEXTURE_CUBE_MAP && attachmentWidth != attachmentHeight) {
            throw new IllegalArgumentException(
                    what + ": cube map faces must be square, got " + attachmentWidth + "x" + attachmentHeight);
        }
        if (!spec.owned && (spec.externalWidth != width || spec.externalHeight != height)) {
            throw new IllegalStateException(
                    "Cannot use framebuffer size " + width + "x" + height + " because external " + what
                            + " is " + spec.externalWidth + "x" + spec.externalHeight);
        }

        switch (role) {
            case COLOR -> {
                if (!spec.type.isColorRenderable()) {
                    throw new IllegalArgumentException(what + ": color attachment must use a color-renderable format: " + spec.type);
                }
                if (spec.type.isDepthLike() || spec.type.isStencilLike()) {
                    throw new IllegalArgumentException(what + ": color attachment cannot use a depth/stencil-like format: " + spec.type);
                }
            }
            case DEPTH -> {
                if (!spec.type.isDepthLike() || spec.type.isDepthStencil() || spec.type.isStencilLike()) {
                    throw new IllegalArgumentException(what + ": depth attachment must use a pure depth format: " + spec.type);
                }
            }
            case STENCIL -> {
                if (!spec.type.isStencilLike() || spec.type.isDepthLike()) {
                    throw new IllegalArgumentException(what + ": stencil attachment must use a pure stencil format: " + spec.type);
                }
            }
            case DEPTH_STENCIL -> {
                if (!spec.type.isDepthStencil()) {
                    throw new IllegalArgumentException(what + ": depth-stencil attachment must use a depth-stencil format: " + spec.type);
                }
            }
        }
    }

    private void validateRenderbufferSpec(RenderbufferSpec spec, String what) {
        if (spec == null) {
            return;
        }
        if (spec.format == null) {
            throw new IllegalArgumentException(what + ": renderbuffer format cannot be null");
        }
        if (spec.samples <= 0) {
            throw new IllegalArgumentException(what + ": renderbuffer samples must be > 0, got " + spec.samples);
        }
        if (!spec.owned && spec.existingId == 0) {
            throw new IllegalArgumentException(what + ": external renderbuffer id must be non-zero");
        }
        if (!spec.owned && (spec.externalWidth != width || spec.externalHeight != height)) {
            throw new IllegalStateException(
                    "Cannot use framebuffer size " + width + "x" + height + " because external " + what
                            + " is " + spec.externalWidth + "x" + spec.externalHeight);
        }
    }

    private static void validateBlitParams(int mask, int filter) {
        int validBits = GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT;
        if (mask == 0 || (mask & ~validBits) != 0) {
            throw new IllegalArgumentException(
                    "Blit mask must be a non-zero combination of GL_COLOR/DEPTH/STENCIL_BUFFER_BIT, got 0x"
                            + Integer.toHexString(mask));
        }
        if (filter != GL11.GL_NEAREST && filter != GL11.GL_LINEAR) {
            throw new IllegalArgumentException("Blit filter must be GL_NEAREST or GL_LINEAR");
        }
        if (filter == GL11.GL_LINEAR && (mask & (GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT)) != 0) {
            throw new IllegalArgumentException("Blits including depth or stencil must use GL_NEAREST");
        }
    }

    private void checkColorIndex(int index) {
        if (index < 0 || index >= colorAttachments.size()) {
            throw new IndexOutOfBoundsException(
                    "Invalid color attachment index " + index + " for framebuffer with "
                            + colorAttachments.size() + " color attachments");
        }
    }

    private void checkDrawBufferIndex(int drawBuffer) {
        if (drawBuffer < 0 || drawBuffer >= colorAttachments.size()) {
            throw new IndexOutOfBoundsException(
                    "Invalid draw buffer index " + drawBuffer + " for framebuffer with "
                            + colorAttachments.size() + " color attachments");
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Framebuffer has already been closed");
        }
    }

    // ------------------------------------------------------------------------
    // Teardown
    // ------------------------------------------------------------------------

    /**
     * @param detach when true, each attachment point is reset to 0 before its object is deleted.
     *               Deleting a texture only auto-detaches it from the <i>currently bound</i>
     *               framebuffer, and this class never binds for allocation, so an explicit detach is
     *               what keeps the FBO from holding names that GL may recycle. Skipped on close,
     *               where the framebuffer itself is about to disappear.
     */
    private void destroyAttachments(boolean detach) {
        for (int i = 0; i < colorAttachments.size(); i++) {
            if (detach) {
                GL45C.glNamedFramebufferTexture(framebufferId, GL30.GL_COLOR_ATTACHMENT0 + i, 0, 0);
            }
            colorAttachments.get(i).close();
        }
        colorAttachments.clear();
        cachedDrawBuffers = null;

        if (depthTexture != null) {
            if (detach) {
                GL45C.glNamedFramebufferTexture(framebufferId, GL30.GL_DEPTH_ATTACHMENT, 0, 0);
            }
            depthTexture.close();
            depthTexture = null;
        }
        if (stencilTexture != null) {
            if (detach) {
                GL45C.glNamedFramebufferTexture(framebufferId, GL30.GL_STENCIL_ATTACHMENT, 0, 0);
            }
            stencilTexture.close();
            stencilTexture = null;
        }
        if (depthStencilTexture != null) {
            if (detach) {
                GL45C.glNamedFramebufferTexture(framebufferId, GL30.GL_DEPTH_STENCIL_ATTACHMENT, 0, 0);
            }
            depthStencilTexture.close();
            depthStencilTexture = null;
        }

        // A non-zero id implies its spec is non-null; external ids are validated non-zero.
        if (depthRenderbufferId != 0) {
            if (detach) {
                GL45C.glNamedFramebufferRenderbuffer(framebufferId, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, 0);
            }
            if (depthRenderbufferSpec.owned) {
                GL45C.glDeleteRenderbuffers(depthRenderbufferId);
            }
            depthRenderbufferId = 0;
        }

        if (stencilRenderbufferId != 0) {
            if (detach) {
                GL45C.glNamedFramebufferRenderbuffer(framebufferId, GL30.GL_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, 0);
            }
            if (stencilRenderbufferSpec.owned) {
                GL45C.glDeleteRenderbuffers(stencilRenderbufferId);
            }
            stencilRenderbufferId = 0;
        }

        if (depthStencilRenderbufferId != 0) {
            if (detach) {
                GL45C.glNamedFramebufferRenderbuffer(framebufferId, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, 0);
            }
            if (depthStencilRenderbufferSpec.owned) {
                GL45C.glDeleteRenderbuffers(depthStencilRenderbufferId);
            }
            depthStencilRenderbufferId = 0;
        }
    }

    /** Closes without masking the failure that triggered it. */
    private void closeSilently(Throwable primary) {
        try {
            close();
        } catch (RuntimeException | Error ex) {
            primary.addSuppressed(ex);
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

    // ------------------------------------------------------------------------
    // Nested types
    // ------------------------------------------------------------------------

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

    /**
     * Describes one texture attachment.
     *
     * <p>For external (non-owned) specs, {@code externalWidth}/{@code externalHeight} are the
     * dimensions of the attached image <i>at {@code level}</i>, and must equal the framebuffer's
     * allocation size.
     */
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

    /**
     * A live attachment. {@code width}/{@code height} are the dimensions of the attached image at its
     * mip level, not the framebuffer's allocation size.
     *
     * <p>Owned attachments are deleted by the framebuffer — do not close one yourself, and do not
     * hold onto a record across {@link #resize}.
     */
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