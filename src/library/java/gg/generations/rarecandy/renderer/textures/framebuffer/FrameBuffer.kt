package gg.generations.rarecandy.renderer.textures.framebuffer

import gg.generations.rarecandy.renderer.textures.ITexture
import gg.generations.rarecandy.renderer.textures.framebuffer.TextureSpec.Companion.texture2D
import org.lwjgl.opengl.*
import java.lang.AutoCloseable
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * A DSA (GL 4.5) framebuffer object with owned or externally-supplied attachments.
 * 
 * <h2>Sizing</h2>
 * [.width]/[.height] are the *allocation* size: owned textures are allocated
 * with their base level at this size, and external attachments must match it exactly.
 * [.renderWidth]/[.renderHeight] are the *effective* render area, which is the
 * minimum over all attachments of their level-adjusted size. These differ only when an attachment is
 * bound at a mip level &gt; 0. Viewport and blit rectangles use the render size.
 * 
 * <h2>Lifetime</h2>
 * [.close] is idempotent. After close, every method that touches GL throws
 * [IllegalStateException]. If construction or [.resize] fails partway, the framebuffer
 * closes itself before rethrowing, so no GL objects leak.
 * 
 * 
 * Not thread-safe; all methods must be called on the thread owning the GL context.
 */
class FrameBuffer(builder: Builder) : AutoCloseable {
    // framebufferId is final — the GL object is never recreated, only its attachments are.
    private val framebufferId: Int

    // Allocation size; mutable to support resize().
    private var width: Int
    private var height: Int

    // Effective render area (min level-adjusted size over all attachments). Recomputed on resize.
    private var renderWidth = 0
    private var renderHeight = 0

    var isClosed: Boolean = false
        private set

    private val colorSpecs: List<TextureSpec>
    private val colorAttachments: MutableList<TextureAttachment?> = ArrayList<TextureAttachment?>()

    // [GL_COLOR_ATTACHMENT0 .. GL_COLOR_ATTACHMENTn-1], rebuilt in allocateAll().
    // Plain int[]: LWJGL stack-allocates for the array overload, so there is no off-heap buffer to track.
    private var cachedDrawBuffers: IntArray? = null

    private val depthTextureSpec: TextureSpec?
    private var depthTexture: TextureAttachment? = null

    private val stencilTextureSpec: TextureSpec?
    private var stencilTexture: TextureAttachment? = null

    private val depthStencilTextureSpec: TextureSpec?
    private var depthStencilTexture: TextureAttachment? = null

    private val depthRenderbufferSpec: RenderbufferSpec?
    private var depthRenderbufferId = 0

    private val stencilRenderbufferSpec: RenderbufferSpec?
    private var stencilRenderbufferId = 0

    private val depthStencilRenderbufferSpec: RenderbufferSpec?
    private var depthStencilRenderbufferId = 0

    // Scratch storage for the glClearNamedFramebuffer* calls. Single-threaded by contract.
    private val scratchColorFloat = FloatArray(4)
    private val scratchColorInt = IntArray(4)
    private val scratchStencil = IntArray(1)
    private val scratchDepth = FloatArray(1)

    constructor(width: Int, height: Int) : this(
        builder(width, height)
            .color(texture2D(ITexture.Type.RGBA8))
            .depthStencilRenderbuffer(RenderbufferSpec.depth24Stencil8())
    )

    constructor(width: Int, height: Int, colorType: ITexture.Type) : this(
        builder(width, height)
            .color(texture2D(colorType))
            .depthStencilRenderbuffer(RenderbufferSpec.depth24Stencil8())
    )

    init {
        require(!(builder.width <= 0 || builder.height <= 0)) { "Framebuffer size must be > 0, got ${builder.width}x${builder.height}" }

        this.width = builder.width
        this.height = builder.height

        this.colorSpecs = builder.colorSpecs.toList()

        this.depthTextureSpec = builder.depthTextureSpec
        this.stencilTextureSpec = builder.stencilTextureSpec
        this.depthStencilTextureSpec = builder.depthStencilTextureSpec

        this.depthRenderbufferSpec = builder.depthRenderbufferSpec
        this.stencilRenderbufferSpec = builder.stencilRenderbufferSpec
        this.depthStencilRenderbufferSpec = builder.depthStencilRenderbufferSpec

        // Validate the whole configuration before any GL object exists, so a bad spec cannot leak.
        validateConfiguration()

        this.framebufferId = GL45C.glCreateFramebuffers()

        try {
            allocateAll()
        } catch (ex: RuntimeException) {
            closeSilently(ex)
            throw ex
        } catch (ex: Error) {
            closeSilently(ex)
            throw ex
        }
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    fun getFramebufferId(): Int {
        ensureOpen()
        return framebufferId
    }

    /** Allocation width. External attachments must match this exactly.  */
    fun width(): Int {
        return width
    }

    /** Allocation height. External attachments must match this exactly.  */
    fun height(): Int {
        return height
    }

    /** Effective render width — equals [.width] unless an attachment is bound at mip level &gt; 0.  */
    fun renderWidth(): Int {
        return renderWidth
    }

    /** Effective render height — equals [.height] unless an attachment is bound at mip level &gt; 0.  */
    fun renderHeight(): Int {
        return renderHeight
    }

    val colorAttachmentCount: Int
        get() = colorAttachments.size

    fun hasDepthTexture(): Boolean {
        return depthTexture != null
    }

    fun hasStencilTexture(): Boolean {
        return stencilTexture != null
    }

    fun hasDepthStencilTexture(): Boolean {
        return depthStencilTexture != null
    }

    fun hasDepthRenderbuffer(): Boolean {
        return depthRenderbufferId != 0
    }

    fun hasStencilRenderbuffer(): Boolean {
        return stencilRenderbufferId != 0
    }

    fun hasDepthStencilRenderbuffer(): Boolean {
        return depthStencilRenderbufferId != 0
    }

    /** True if anything is attached that can hold depth (texture, renderbuffer, or a combined depth-stencil).  */
    fun hasDepth(): Boolean {
        return depthTexture != null || depthStencilTexture != null || depthRenderbufferId != 0 || depthStencilRenderbufferId != 0
    }

    /** True if anything is attached that can hold stencil (texture, renderbuffer, or a combined depth-stencil).  */
    fun hasStencil(): Boolean {
        return stencilTexture != null || depthStencilTexture != null || stencilRenderbufferId != 0 || depthStencilRenderbufferId != 0
    }

    fun getColorAttachment(index: Int): TextureAttachment? {
        checkColorIndex(index)
        return colorAttachments[index]
    }

    fun getDepthTexture(): TextureAttachment {
        return checkNotNull(depthTexture) { "No depth texture attached" }
    }

    fun getStencilTexture(): TextureAttachment {
        checkNotNull(stencilTexture) { "No stencil texture attached" }
        return stencilTexture!!
    }

    fun getDepthStencilTexture(): TextureAttachment {
        checkNotNull(depthStencilTexture) { "No depth-stencil texture attached" }
        return depthStencilTexture!!
    }

    fun getDepthRenderbufferId(): Int {
        check(depthRenderbufferId != 0) { "No depth renderbuffer attached" }
        return depthRenderbufferId
    }

    fun getStencilRenderbufferId(): Int {
        check(stencilRenderbufferId != 0) { "No stencil renderbuffer attached" }
        return stencilRenderbufferId
    }

    fun getDepthStencilRenderbufferId(): Int {
        check(depthStencilRenderbufferId != 0) { "No depth-stencil renderbuffer attached" }
        return depthStencilRenderbufferId
    }

    // ------------------------------------------------------------------------
    // Binding
    // ------------------------------------------------------------------------
    fun bindFramebuffer() {
        ensureOpen()
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferId)
    }

    fun bindForDraw() {
        ensureOpen()
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, framebufferId)
    }

    fun bindForRead() {
        ensureOpen()
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, framebufferId)
    }

    fun unbind() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)
    }

    /** Binds this framebuffer and sets the viewport to the effective render area.  */
    fun bindAndSetViewport() {
        ensureOpen()
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferId)
        GL11.glViewport(0, 0, renderWidth, renderHeight)
    }

    // ------------------------------------------------------------------------
    // Draw / read buffer selection
    // ------------------------------------------------------------------------
    /**
     * Enables exactly the listed color attachments, positionally: attachment `i` receives
     * fragment output `i`, every other output is discarded.
     * 
     * 
     * This is the only mapping OpenGL permits for `glDrawBuffers` — the i-th entry must be
     * `GL_COLOR_ATTACHMENTi` or `GL_NONE`, so the list cannot reorder or compact
     * attachments. Passing no arguments (or `null`) disables all color output. Duplicates are
     * harmless. To route fragment output 0 to some other attachment, use [.setSingleDrawBuffer].
     */
    fun setDrawBuffers(vararg attachments: Int) {
        ensureOpen()

        if (attachments == null || attachments.size == 0) {
            GL45C.glNamedFramebufferDrawBuffer(framebufferId, GL11.GL_NONE)
            return
        }

        val bufs = IntArray(colorAttachments.size)
        Arrays.fill(bufs, GL11.GL_NONE)
        for (attachment in attachments) {
            checkColorIndex(attachment)
            bufs[attachment] = GL30.GL_COLOR_ATTACHMENT0 + attachment
        }
        GL45C.glNamedFramebufferDrawBuffers(framebufferId, bufs)
    }

    /**
     * Routes fragment output 0 to `attachment` and disables every other output.
     * Unlike [.setDrawBuffers], the attachment index need not match the output index.
     */
    fun setSingleDrawBuffer(attachment: Int) {
        ensureOpen()
        checkColorIndex(attachment)
        GL45C.glNamedFramebufferDrawBuffer(framebufferId, GL30.GL_COLOR_ATTACHMENT0 + attachment)
    }

    /** Resets draw buffers to all color attachments in order (output i to attachment i).  */
    fun setDrawAll() {
        ensureOpen()

        if (cachedDrawBuffers == null) {
            GL45C.glNamedFramebufferDrawBuffer(framebufferId, GL11.GL_NONE)
            return
        }
        GL45C.glNamedFramebufferDrawBuffers(framebufferId, cachedDrawBuffers)
    }

    fun setReadBuffer(attachment: Int) {
        ensureOpen()
        checkColorIndex(attachment)
        GL45C.glNamedFramebufferReadBuffer(framebufferId, GL30.GL_COLOR_ATTACHMENT0 + attachment)
    }

    /** Disables both color writes and color reads for this framebuffer.  */
    fun disableColorOutputs() {
        ensureOpen()
        GL45C.glNamedFramebufferDrawBuffer(framebufferId, GL11.GL_NONE)
        GL45C.glNamedFramebufferReadBuffer(framebufferId, GL11.GL_NONE)
    }

    // ------------------------------------------------------------------------
    // Clears
    // ------------------------------------------------------------------------
    /**
     * Clears a draw buffer with float values. `drawBuffer` is the *draw buffer index*,
     * which equals the attachment index under the default mapping and after [.setDrawBuffers],
     * but is always 0 after [.setSingleDrawBuffer].
     * 
     * 
     * Use [.clearColorInt] for signed-integer formats and [.clearColorUInt] for
     * unsigned-integer formats; clearing an integer attachment with this method is undefined in GL.
     */
    fun clearColor(drawBuffer: Int, r: Float, g: Float, b: Float, a: Float) {
        ensureOpen()
        checkDrawBufferIndex(drawBuffer)
        scratchColorFloat[0] = r
        scratchColorFloat[1] = g
        scratchColorFloat[2] = b
        scratchColorFloat[3] = a
        GL45C.glClearNamedFramebufferfv(framebufferId, GL11.GL_COLOR, drawBuffer, scratchColorFloat)
    }

    /** Clears a draw buffer backed by a signed-integer format. See [.clearColor] for index semantics.  */
    fun clearColorInt(drawBuffer: Int, x: Int, y: Int, z: Int, w: Int) {
        ensureOpen()
        checkDrawBufferIndex(drawBuffer)
        scratchColorInt[0] = x
        scratchColorInt[1] = y
        scratchColorInt[2] = z
        scratchColorInt[3] = w
        GL45C.glClearNamedFramebufferiv(framebufferId, GL11.GL_COLOR, drawBuffer, scratchColorInt)
    }

    /** Clears a draw buffer backed by an unsigned-integer format. See [.clearColor] for index semantics.  */
    fun clearColorUInt(drawBuffer: Int, x: Int, y: Int, z: Int, w: Int) {
        ensureOpen()
        checkDrawBufferIndex(drawBuffer)
        scratchColorInt[0] = x
        scratchColorInt[1] = y
        scratchColorInt[2] = z
        scratchColorInt[3] = w
        GL45C.glClearNamedFramebufferuiv(framebufferId, GL11.GL_COLOR, drawBuffer, scratchColorInt)
    }

    fun clearDepth(depth: Float) {
        ensureOpen()
        check(hasDepth()) { "Framebuffer has no depth attachment to clear" }
        this.scratchDepth[0] = depth
        GL45C.glClearNamedFramebufferfv(framebufferId, GL11.GL_DEPTH, 0, this.scratchDepth)
    }

    fun clearStencil(stencil: Int) {
        ensureOpen()
        check(hasStencil()) { "Framebuffer has no stencil attachment to clear" }
        this.scratchStencil[0] = stencil
        GL45C.glClearNamedFramebufferiv(framebufferId, GL11.GL_STENCIL, 0, this.scratchStencil)
    }

    fun clearDepthStencil(depth: Float, stencil: Int) {
        ensureOpen()
        check(!(!hasDepth() && !hasStencil())) { "Framebuffer has no depth or stencil attachment to clear" }
        GL45C.glClearNamedFramebufferfi(framebufferId, GL30.GL_DEPTH_STENCIL, 0, depth, stencil)
    }

    // ------------------------------------------------------------------------
    // Blits
    // ------------------------------------------------------------------------
    /**
     * Blits the effective render area of this framebuffer to `target`.
     * 
     * 
     * `srcAttachment` and `dstAttachment` are only consulted when `mask`
     * includes `GL_COLOR_BUFFER_BIT`; a depth-only or stencil-only blit works on framebuffers
     * with no color attachments at all.
     * 
     * 
     * **Side effect:** for a color blit, this framebuffer's read buffer is left pointing at
     * `srcAttachment` and `target`'s draw buffer at `dstAttachment`. Call
     * [.setDrawAll] / [.setReadBuffer] afterward if you need the previous state back.
     */
    fun blitTo(target: FrameBuffer, srcAttachment: Int, dstAttachment: Int, mask: Int, filter: Int) {
        ensureOpen()
        requireNotNull(target) { "Target framebuffer cannot be null" }
        target.ensureOpen()
        validateBlitParams(mask, filter)

        if ((mask and GL11.GL_COLOR_BUFFER_BIT) != 0) {
            checkColorIndex(srcAttachment)
            target.checkColorIndex(dstAttachment)
            GL45C.glNamedFramebufferReadBuffer(this.framebufferId, GL30.GL_COLOR_ATTACHMENT0 + srcAttachment)
            GL45C.glNamedFramebufferDrawBuffer(target.framebufferId, GL30.GL_COLOR_ATTACHMENT0 + dstAttachment)
        }

        GL45C.glBlitNamedFramebuffer(
            this.framebufferId,
            target.framebufferId,
            0, 0, this.renderWidth, this.renderHeight,
            0, 0, target.renderWidth, target.renderHeight,
            mask,
            filter
        )
    }

    /**
     * Blits the effective render area of this framebuffer to the default framebuffer (screen).
     * 
     * 
     * `srcAttachment` is only consulted when `mask` includes
     * `GL_COLOR_BUFFER_BIT`.
     * 
     * 
     * **Side effect:** for a color blit, this framebuffer's read buffer is left pointing at
     * `srcAttachment`. Call [.setReadBuffer] afterward if you need the previous state back.
     */
    fun blitToScreen(srcAttachment: Int, targetWidth: Int, targetHeight: Int, mask: Int, filter: Int) {
        ensureOpen()
        require(!(targetWidth <= 0 || targetHeight <= 0)) { "Blit target size must be > 0, got " + targetWidth + "x" + targetHeight }
        validateBlitParams(mask, filter)

        if ((mask and GL11.GL_COLOR_BUFFER_BIT) != 0) {
            checkColorIndex(srcAttachment)
            GL45C.glNamedFramebufferReadBuffer(this.framebufferId, GL30.GL_COLOR_ATTACHMENT0 + srcAttachment)
        }

        GL45C.glBlitNamedFramebuffer(
            this.framebufferId,
            0,
            0, 0, this.renderWidth, this.renderHeight,
            0, 0, targetWidth, targetHeight,
            mask,
            filter
        )
    }

    // ------------------------------------------------------------------------
    // Resize / completeness / lifetime
    // ------------------------------------------------------------------------
    /**
     * Reallocates every owned attachment at the new size. External attachments are not reallocated,
     * so their declared size must already match the new size.
     * 
     * 
     * Draw and read buffer selection is reset to the default (all attachments in order, read from
     * attachment 0). Previously returned [TextureAttachment] records are invalidated.
     * 
     * 
     * If reallocation fails, the framebuffer closes itself before rethrowing.
     * 
     * @throws IllegalStateException if an external attachment's size does not match the new size
     */
    fun resize(newWidth: Int, newHeight: Int) {
        ensureOpen()
        require(!(newWidth <= 0 || newHeight <= 0)) { "Framebuffer size must be > 0, got " + newWidth + "x" + newHeight }
        if (this.width == newWidth && this.height == newHeight) {
            return
        }

        val previousWidth = this.width
        val previousHeight = this.height
        this.width = newWidth
        this.height = newHeight

        try {
            validateConfiguration()
        } catch (ex: RuntimeException) {
            // Nothing was destroyed yet — roll back and leave the framebuffer usable.
            this.width = previousWidth
            this.height = previousHeight
            computeRenderSize()
            throw ex
        }

        destroyAttachments(true)

        try {
            allocateAll()
        } catch (ex: RuntimeException) {
            closeSilently(ex)
            throw ex
        } catch (ex: Error) {
            closeSilently(ex)
            throw ex
        }
    }

    /**
     * Throws [IllegalStateException] if the framebuffer is not complete.
     * @see .isComplete
     */
    fun checkComplete() {
        ensureOpen()
        val status = GL45C.glCheckNamedFramebufferStatus(framebufferId, GL30.GL_FRAMEBUFFER)
        check(status == GL30.GL_FRAMEBUFFER_COMPLETE) {
            ("Framebuffer " + framebufferId + " (" + width + "x" + height + ") is not complete: "
                    + framebufferStatusName(status))
        }
    }

    val isComplete: Boolean
        /**
         * Returns `true` if the framebuffer is complete, `false` otherwise (including after
         * [.close]). Useful for debug overlays or conditional logic without try/catch.
         */
        get() = !this.isClosed
                && GL45C.glCheckNamedFramebufferStatus(
            framebufferId,
            GL30.GL_FRAMEBUFFER
        ) == GL30.GL_FRAMEBUFFER_COMPLETE

    /** Deletes every owned attachment and the framebuffer itself. Idempotent.  */
    override fun close() {
        if (this.isClosed) {
            return
        }
        this.isClosed = true
        // No need to detach first: deleting the framebuffer drops every attachment with it.
        destroyAttachments(false)
        GL45C.glDeleteFramebuffers(framebufferId)
    }

    // ------------------------------------------------------------------------
    // Allocation
    // ------------------------------------------------------------------------
    private fun allocateAll() {
        var colorIndex = 0
        for (spec in colorSpecs) {
            val attachment = createTextureAttachment(spec)
            attachTexture(GL30.GL_COLOR_ATTACHMENT0 + colorIndex, spec, attachment.id)
            colorAttachments.add(attachment)
            colorIndex++
        }

        if (depthTextureSpec != null) {
            depthTexture = createTextureAttachment(depthTextureSpec)
            attachTexture(GL30.GL_DEPTH_ATTACHMENT, depthTextureSpec, depthTexture!!.id)
        }

        if (stencilTextureSpec != null) {
            stencilTexture = createTextureAttachment(stencilTextureSpec)
            attachTexture(GL30.GL_STENCIL_ATTACHMENT, stencilTextureSpec, stencilTexture!!.id)
        }

        if (depthStencilTextureSpec != null) {
            depthStencilTexture = createTextureAttachment(depthStencilTextureSpec)
            attachTexture(GL30.GL_DEPTH_STENCIL_ATTACHMENT, depthStencilTextureSpec, depthStencilTexture!!.id)
        }

        if (depthRenderbufferSpec != null) {
            depthRenderbufferId = createRenderbuffer(depthRenderbufferSpec)
            GL45C.glNamedFramebufferRenderbuffer(
                framebufferId,
                GL30.GL_DEPTH_ATTACHMENT,
                GL30.GL_RENDERBUFFER,
                depthRenderbufferId
            )
        }

        if (stencilRenderbufferSpec != null) {
            stencilRenderbufferId = createRenderbuffer(stencilRenderbufferSpec)
            GL45C.glNamedFramebufferRenderbuffer(
                framebufferId,
                GL30.GL_STENCIL_ATTACHMENT,
                GL30.GL_RENDERBUFFER,
                stencilRenderbufferId
            )
        }

        if (depthStencilRenderbufferSpec != null) {
            depthStencilRenderbufferId = createRenderbuffer(depthStencilRenderbufferSpec)
            GL45C.glNamedFramebufferRenderbuffer(
                framebufferId,
                GL30.GL_DEPTH_STENCIL_ATTACHMENT,
                GL30.GL_RENDERBUFFER,
                depthStencilRenderbufferId
            )
        }

        if (colorAttachments.isEmpty()) {
            cachedDrawBuffers = null
            GL45C.glNamedFramebufferDrawBuffer(framebufferId, GL11.GL_NONE)
            GL45C.glNamedFramebufferReadBuffer(framebufferId, GL11.GL_NONE)
        } else {
            cachedDrawBuffers = IntArray(colorAttachments.size)
            for (i in cachedDrawBuffers!!.indices) {
                cachedDrawBuffers!![i] = GL30.GL_COLOR_ATTACHMENT0 + i
            }
            GL45C.glNamedFramebufferDrawBuffers(framebufferId, cachedDrawBuffers)
            GL45C.glNamedFramebufferReadBuffer(framebufferId, GL30.GL_COLOR_ATTACHMENT0)
        }

        checkComplete()
    }

    private fun createTextureAttachment(spec: TextureSpec): TextureAttachment {
        val textureId: Int
        if (spec.owned) {
            textureId = GL45C.glCreateTextures(spec.target!!.glTarget)
            allocateTextureStorage(textureId, spec)
            configureTextureParameters(textureId, spec)
        } else {
            textureId = spec.existingId
        }

        return TextureAttachment(
            textureId,
            specWidth(spec),
            specHeight(spec),
            spec.type!!,
            spec.computeAccess!!,
            spec.target!!.glTarget,
            spec.target.multisample,
            spec.owned
        )
    }

    private fun attachTexture(attachmentPoint: Int, spec: TextureSpec, textureId: Int) {
        if (spec.attachKind == TextureAttachKind.WHOLE_TEXTURE) {
            GL45C.glNamedFramebufferTexture(framebufferId, attachmentPoint, textureId, spec.level)
        } else {
            GL45C.glNamedFramebufferTextureLayer(framebufferId, attachmentPoint, textureId, spec.level, spec.layer)
        }
    }

    private fun createRenderbuffer(spec: RenderbufferSpec): Int {
        if (!spec.owned) {
            return spec.existingId
        }

        val renderbufferId = GL45C.glCreateRenderbuffers()
        if (spec.samples > 1) {
            GL45C.glNamedRenderbufferStorageMultisample(
                renderbufferId,
                spec.samples,
                spec.format.internalFormat,
                width,
                height
            )
        } else {
            GL45C.glNamedRenderbufferStorage(renderbufferId, spec.format.internalFormat, width, height)
        }
        return renderbufferId
    }

    private fun allocateTextureStorage(textureId: Int, spec: TextureSpec) {
        when (spec.target) {
            TextureTarget.TEXTURE_2D -> GL45C.glTextureStorage2D(
                textureId,
                spec.levels,
                spec.type!!.internalFormat,
                width,
                height
            )

            TextureTarget.TEXTURE_2D_ARRAY -> GL45C.glTextureStorage3D(
                textureId,
                spec.levels,
                spec.type!!.internalFormat,
                width,
                height,
                spec.layers
            )

            TextureTarget.TEXTURE_CUBE_MAP -> GL45C.glTextureStorage2D(
                textureId,
                spec.levels,
                spec.type!!.internalFormat,
                width,
                height
            )

            TextureTarget.TEXTURE_2D_MULTISAMPLE -> GL45C.glTextureStorage2DMultisample(
                textureId,
                spec.samples,
                spec.type!!.internalFormat,
                width,
                height,
                spec.fixedSampleLocations
            )

            TextureTarget.TEXTURE_2D_MULTISAMPLE_ARRAY -> GL45C.glTextureStorage3DMultisample(
                textureId,
                spec.samples,
                spec.type!!.internalFormat,
                width,
                height,
                spec.layers,
                spec.fixedSampleLocations
            )

            else -> {}
        }
    }

    private fun configureTextureParameters(textureId: Int, spec: TextureSpec) {
        if (spec.target!!.multisample) {
            // Multisample textures reject every sampler parameter.
            return
        }

        GL45C.glTextureParameteri(textureId, GL11.GL_TEXTURE_MIN_FILTER, spec.minFilter)
        GL45C.glTextureParameteri(textureId, GL11.GL_TEXTURE_MAG_FILTER, spec.magFilter)
        GL45C.glTextureParameteri(textureId, GL11.GL_TEXTURE_WRAP_S, spec.wrapS)
        GL45C.glTextureParameteri(textureId, GL11.GL_TEXTURE_WRAP_T, spec.wrapT)

        if (spec.target == TextureTarget.TEXTURE_2D_ARRAY || spec.target == TextureTarget.TEXTURE_CUBE_MAP) {
            GL45C.glTextureParameteri(textureId, GL12.GL_TEXTURE_WRAP_R, spec.wrapR)
        }

        if (spec.compareMode) {
            // GL_COMPARE_R_TO_TEXTURE is the GL 1.4 name for GL 3.0's GL_COMPARE_REF_TO_TEXTURE; same value.
            GL45C.glTextureParameteri(textureId, GL14.GL_TEXTURE_COMPARE_MODE, GL14.GL_COMPARE_R_TO_TEXTURE)
            GL45C.glTextureParameteri(textureId, GL14.GL_TEXTURE_COMPARE_FUNC, spec.compareFunc)
        }
    }

    // ------------------------------------------------------------------------
    // Validation
    // ------------------------------------------------------------------------
    /**
     * Validates the whole configuration against the current [.width]/[.height] and
     * recomputes the effective render size. Touches no GL objects, so it is safe to call before the
     * framebuffer exists and before destroying anything on resize.
     */
    private fun validateConfiguration() {
        require(
            !(colorSpecs.isEmpty()
                    && depthTextureSpec == null && stencilTextureSpec == null && depthStencilTextureSpec == null && depthRenderbufferSpec == null && stencilRenderbufferSpec == null && depthStencilRenderbufferSpec == null)
        ) { "Framebuffer must have at least one attachment" }

        val maxColorAttachments = GL11.glGetInteger(GL30.GL_MAX_COLOR_ATTACHMENTS)
        val maxDrawBuffers = GL11.glGetInteger(GL20.GL_MAX_DRAW_BUFFERS)
        val limit = min(maxColorAttachments, maxDrawBuffers)
        require(colorSpecs.size <= limit) {
            ("Framebuffer requests " + colorSpecs.size + " color attachments but this context supports "
                    + limit + " (GL_MAX_COLOR_ATTACHMENTS=" + maxColorAttachments
                    + ", GL_MAX_DRAW_BUFFERS=" + maxDrawBuffers + ")")
        }

        validateAttachmentSlots()

        for (i in colorSpecs.indices) {
            validateTextureSpec(colorSpecs.get(i), AttachmentRole.COLOR, "color texture " + i)
        }
        validateTextureSpec(depthTextureSpec, AttachmentRole.DEPTH, "depth texture")
        validateTextureSpec(stencilTextureSpec, AttachmentRole.STENCIL, "stencil texture")
        validateTextureSpec(depthStencilTextureSpec, AttachmentRole.DEPTH_STENCIL, "depth-stencil texture")

        validateRenderbufferSpec(depthRenderbufferSpec, "depth renderbuffer")
        validateRenderbufferSpec(stencilRenderbufferSpec, "stencil renderbuffer")
        validateRenderbufferSpec(depthStencilRenderbufferSpec, "depth-stencil renderbuffer")

        validateSampleConsistency()

        computeRenderSize()
    }

    /**
     * OpenGL has exactly one depth attachment point and one stencil attachment point, and a combined
     * depth-stencil attachment claims both. Two specs targeting the same point would silently
     * overwrite each other, leaving the loser allocated but invisible.
     */
    private fun validateAttachmentSlots() {
        val depthClaims: MutableList<String?> = ArrayList<String?>()
        if (depthTextureSpec != null) depthClaims.add("depthTexture")
        if (depthRenderbufferSpec != null) depthClaims.add("depthRenderbuffer")
        if (depthStencilTextureSpec != null) depthClaims.add("depthStencilTexture")
        if (depthStencilRenderbufferSpec != null) depthClaims.add("depthStencilRenderbuffer")
        require(depthClaims.size <= 1) { "Multiple specs claim the depth attachment point: " + depthClaims }

        val stencilClaims: MutableList<String?> = ArrayList<String?>()
        if (stencilTextureSpec != null) stencilClaims.add("stencilTexture")
        if (stencilRenderbufferSpec != null) stencilClaims.add("stencilRenderbuffer")
        if (depthStencilTextureSpec != null) stencilClaims.add("depthStencilTexture")
        if (depthStencilRenderbufferSpec != null) stencilClaims.add("depthStencilRenderbuffer")
        require(stencilClaims.size <= 1) { "Multiple specs claim the stencil attachment point: " + stencilClaims }
    }

    /** All attachments must agree on sample count, or the framebuffer is INCOMPLETE_MULTISAMPLE.  */
    private fun validateSampleConsistency() {
        val samplesByAttachment: MutableMap<String?, Int?> = LinkedHashMap<String?, Int?>()
        for (i in colorSpecs.indices) {
            samplesByAttachment.put("color texture " + i, colorSpecs.get(i).samples)
        }
        if (depthTextureSpec != null) samplesByAttachment.put("depth texture", depthTextureSpec.samples)
        if (stencilTextureSpec != null) samplesByAttachment.put("stencil texture", stencilTextureSpec.samples)
        if (depthStencilTextureSpec != null) samplesByAttachment.put(
            "depth-stencil texture",
            depthStencilTextureSpec.samples
        )
        if (depthRenderbufferSpec != null) samplesByAttachment.put("depth renderbuffer", depthRenderbufferSpec.samples)
        if (stencilRenderbufferSpec != null) samplesByAttachment.put(
            "stencil renderbuffer",
            stencilRenderbufferSpec.samples
        )
        if (depthStencilRenderbufferSpec != null) samplesByAttachment.put(
            "depth-stencil renderbuffer",
            depthStencilRenderbufferSpec.samples
        )

        var referenceName: String? = null
        var referenceSamples = -1
        for (entry in samplesByAttachment.entries) {
            if (referenceName == null) {
                referenceName = entry.key
                referenceSamples = entry.value!!
            } else require(entry.value == referenceSamples) {
                ("All attachments must have the same sample count: " + referenceName + " has "
                        + referenceSamples + " but " + entry.key + " has " + entry.value)
            }
        }
    }

    /**
     * The effective render area is the minimum over all attachments of their size, which for an owned
     * texture bound at mip level N is the level-N size rather than the allocation size.
     */
    private fun computeRenderSize() {
        var w = Int.MAX_VALUE
        var h = Int.MAX_VALUE

        for (spec in colorSpecs) {
            w = min(w, specWidth(spec))
            h = min(h, specHeight(spec))
        }
        for (spec in arrayOf<TextureSpec?>(depthTextureSpec, stencilTextureSpec, depthStencilTextureSpec)) {
            if (spec != null) {
                w = min(w, specWidth(spec))
                h = min(h, specHeight(spec))
            }
        }
        for (spec in arrayOf<RenderbufferSpec?>(
            depthRenderbufferSpec,
            stencilRenderbufferSpec,
            depthStencilRenderbufferSpec
        )) {
            if (spec != null) {
                w = min(w, if (spec.owned) width else spec.externalWidth)
                h = min(h, if (spec.owned) height else spec.externalHeight)
            }
        }

        renderWidth = if (w == Int.MAX_VALUE) width else w
        renderHeight = if (h == Int.MAX_VALUE) height else h
    }

    private fun specWidth(spec: TextureSpec): Int {
        return if (spec.owned) max(1, width shr spec.level) else spec.externalWidth
    }

    private fun specHeight(spec: TextureSpec): Int {
        return if (spec.owned) max(1, height shr spec.level) else spec.externalHeight
    }

    private fun validateTextureSpec(spec: TextureSpec?, role: AttachmentRole, what: String?) {
        if (spec == null) {
            return
        }
        requireNotNull(spec.type) { what + ": texture type cannot be null" }
        requireNotNull(spec.target) { what + ": texture target cannot be null" }
        requireNotNull(spec.attachKind) { what + ": attach kind cannot be null" }
        require(spec.levels > 0) { what + ": texture levels must be > 0, got " + spec.levels }
        require(!(spec.level < 0 || spec.level >= spec.levels)) {
            (what + ": attach level " + spec.level + " is out of range for a texture with "
                    + spec.levels + " level(s)")
        }
        if (spec.target.multisample) {
            require(spec.samples > 0) { what + ": multisample texture must have samples > 0" }
            require(!(spec.levels != 1 || spec.level != 0)) { what + ": multisample textures have exactly one level" }
        } else require(spec.samples == 1) { what + ": non-multisample texture must use samples=1, got " + spec.samples }

        if (spec.target == TextureTarget.TEXTURE_2D_ARRAY || spec.target == TextureTarget.TEXTURE_2D_MULTISAMPLE_ARRAY) {
            require(spec.layers > 0) { what + ": array texture must have layers > 0, got " + spec.layers }
        }
        require(!(spec.target == TextureTarget.TEXTURE_CUBE_MAP && spec.layers != 6)) { what + ": cube map texture must use layers=6, got " + spec.layers }

        if (spec.attachKind == TextureAttachKind.LAYER) {
            require(spec.target.layered) { what + ": target " + spec.target + " does not support layer attachment" }
            val layerCount = if (spec.target == TextureTarget.TEXTURE_CUBE_MAP) 6 else spec.layers
            require(!(spec.layer < 0 || spec.layer >= layerCount)) { what + ": layer " + spec.layer + " is out of range [0, " + (layerCount - 1) + "]" }
        }

        require(!(!spec.owned && spec.existingId == 0)) { what + ": external texture id must be non-zero" }

        val attachmentWidth = specWidth(spec)
        val attachmentHeight = specHeight(spec)
        require(!(spec.target == TextureTarget.TEXTURE_CUBE_MAP && attachmentWidth != attachmentHeight)) { what + ": cube map faces must be square, got " + attachmentWidth + "x" + attachmentHeight }
        check(!(!spec.owned && (spec.externalWidth != width || spec.externalHeight != height))) {
            ("Cannot use framebuffer size " + width + "x" + height + " because external " + what
                    + " is " + spec.externalWidth + "x" + spec.externalHeight)
        }

        when (role) {
            AttachmentRole.COLOR -> {
                require(spec.type.isColorRenderable) { what + ": color attachment must use a color-renderable format: " + spec.type }
                require(!(spec.type.isDepthLike || spec.type.isStencilLike)) { what + ": color attachment cannot use a depth/stencil-like format: " + spec.type }
            }

            AttachmentRole.DEPTH -> {
                require(!(!spec.type.isDepthLike || spec.type.isDepthStencil || spec.type.isStencilLike)) { what + ": depth attachment must use a pure depth format: " + spec.type }
            }

            AttachmentRole.STENCIL -> {
                require(!(!spec.type.isStencilLike || spec.type.isDepthLike)) { what + ": stencil attachment must use a pure stencil format: " + spec.type }
            }

            AttachmentRole.DEPTH_STENCIL -> {
                require(spec.type.isDepthStencil) { what + ": depth-stencil attachment must use a depth-stencil format: " + spec.type }
            }
        }
    }

    private fun validateRenderbufferSpec(spec: RenderbufferSpec?, what: String?) {
        if (spec == null) {
            return
        }
        requireNotNull(spec.format) { what + ": renderbuffer format cannot be null" }
        require(spec.samples > 0) { what + ": renderbuffer samples must be > 0, got " + spec.samples }
        require(!(!spec.owned && spec.existingId == 0)) { what + ": external renderbuffer id must be non-zero" }
        check(!(!spec.owned && (spec.externalWidth != width || spec.externalHeight != height))) {
            ("Cannot use framebuffer size " + width + "x" + height + " because external " + what
                    + " is " + spec.externalWidth + "x" + spec.externalHeight)
        }
    }

    private fun checkColorIndex(index: Int) {
        if (index < 0 || index >= colorAttachments.size) {
            throw IndexOutOfBoundsException(
                ("Invalid color attachment index " + index + " for framebuffer with "
                        + colorAttachments.size + " color attachments")
            )
        }
    }

    private fun checkDrawBufferIndex(drawBuffer: Int) {
        if (drawBuffer < 0 || drawBuffer >= colorAttachments.size) {
            throw IndexOutOfBoundsException(
                ("Invalid draw buffer index " + drawBuffer + " for framebuffer with "
                        + colorAttachments.size + " color attachments")
            )
        }
    }

    private fun ensureOpen() {
        check(!this.isClosed) { "Framebuffer has already been closed" }
    }

    // ------------------------------------------------------------------------
    // Teardown
    // ------------------------------------------------------------------------
    /**
     * @param detach when true, each attachment point is reset to 0 before its object is deleted.
     * Deleting a texture only auto-detaches it from the *currently bound*
     * framebuffer, and this class never binds for allocation, so an explicit detach is
     * what keeps the FBO from holding names that GL may recycle. Skipped on close,
     * where the framebuffer itself is about to disappear.
     */
    private fun destroyAttachments(detach: Boolean) {
        for (i in colorAttachments.indices) {
            if (detach) {
                GL45C.glNamedFramebufferTexture(framebufferId, GL30.GL_COLOR_ATTACHMENT0 + i, 0, 0)
            }
            colorAttachments.get(i)!!.delete()
        }
        colorAttachments.clear()
        cachedDrawBuffers = null

        if (depthTexture != null) {
            if (detach) {
                GL45C.glNamedFramebufferTexture(framebufferId, GL30.GL_DEPTH_ATTACHMENT, 0, 0)
            }
            depthTexture!!.delete()
            depthTexture = null
        }
        if (stencilTexture != null) {
            if (detach) {
                GL45C.glNamedFramebufferTexture(framebufferId, GL30.GL_STENCIL_ATTACHMENT, 0, 0)
            }
            stencilTexture!!.delete()
            stencilTexture = null
        }
        if (depthStencilTexture != null) {
            if (detach) {
                GL45C.glNamedFramebufferTexture(framebufferId, GL30.GL_DEPTH_STENCIL_ATTACHMENT, 0, 0)
            }
            depthStencilTexture!!.delete()
            depthStencilTexture = null
        }

        // A non-zero id implies its spec is non-null; external ids are validated non-zero.
        if (depthRenderbufferId != 0) {
            if (detach) {
                GL45C.glNamedFramebufferRenderbuffer(framebufferId, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, 0)
            }
            if (depthRenderbufferSpec!!.owned) {
                GL45C.glDeleteRenderbuffers(depthRenderbufferId)
            }
            depthRenderbufferId = 0
        }

        if (stencilRenderbufferId != 0) {
            if (detach) {
                GL45C.glNamedFramebufferRenderbuffer(framebufferId, GL30.GL_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, 0)
            }
            if (stencilRenderbufferSpec!!.owned) {
                GL45C.glDeleteRenderbuffers(stencilRenderbufferId)
            }
            stencilRenderbufferId = 0
        }

        if (depthStencilRenderbufferId != 0) {
            if (detach) {
                GL45C.glNamedFramebufferRenderbuffer(
                    framebufferId,
                    GL30.GL_DEPTH_STENCIL_ATTACHMENT,
                    GL30.GL_RENDERBUFFER,
                    0
                )
            }
            if (depthStencilRenderbufferSpec!!.owned) {
                GL45C.glDeleteRenderbuffers(depthStencilRenderbufferId)
            }
            depthStencilRenderbufferId = 0
        }
    }

    /** Closes without masking the failure that triggered it.  */
    private fun closeSilently(primary: Throwable) {
        try {
            close()
        } catch (ex: RuntimeException) {
            primary.addSuppressed(ex)
        } catch (ex: Error) {
            primary.addSuppressed(ex)
        }
    }

    open class Builder internal constructor(internal val width: Int, internal val height: Int) {
        internal val colorSpecs: MutableList<TextureSpec> = mutableListOf<TextureSpec>()

        internal var depthTextureSpec: TextureSpec? = null
        internal var stencilTextureSpec: TextureSpec? = null
        internal var depthStencilTextureSpec: TextureSpec? = null

        internal var depthRenderbufferSpec: RenderbufferSpec? = null
        internal var stencilRenderbufferSpec: RenderbufferSpec? = null
        internal var depthStencilRenderbufferSpec: RenderbufferSpec? = null

        fun color(spec: TextureSpec): Builder {
            this.colorSpecs.add(spec)
            return this
        }

        fun depthTexture(spec: TextureSpec): Builder {
            this.depthTextureSpec = spec
            return this
        }

        fun stencilTexture(spec: TextureSpec): Builder {
            this.stencilTextureSpec = spec
            return this
        }

        fun depthStencilTexture(spec: TextureSpec): Builder {
            this.depthStencilTextureSpec = spec
            return this
        }

        fun depthRenderbuffer(spec: RenderbufferSpec): Builder {
            this.depthRenderbufferSpec = spec
            return this
        }

        fun stencilRenderbuffer(spec: RenderbufferSpec): Builder {
            this.stencilRenderbufferSpec = spec
            return this
        }

        fun depthStencilRenderbuffer(spec: RenderbufferSpec): Builder {
            this.depthStencilRenderbufferSpec = spec
            return this
        }

        fun build(): FrameBuffer {
            return FrameBuffer(this)
        }
    }

    companion object {
        fun builder(width: Int, height: Int): Builder {
            return Builder(width, height)
        }

        fun builder(width: Int, height: Int, block: Builder.() -> Unit): Builder {
            return Builder(width, height).also { block.invoke(it) }
        }

        @JvmStatic
        fun unbindDraw() {
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0)
        }

        fun unbindRead() {
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0)
        }

        private fun validateBlitParams(mask: Int, filter: Int) {
            val validBits = GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT or GL11.GL_STENCIL_BUFFER_BIT
            require(!(mask == 0 || (mask and validBits.inv()) != 0)) {
                ("Blit mask must be a non-zero combination of GL_COLOR/DEPTH/STENCIL_BUFFER_BIT, got 0x"
                        + Integer.toHexString(mask))
            }
            require(!(filter != GL11.GL_NEAREST && filter != GL11.GL_LINEAR)) { "Blit filter must be GL_NEAREST or GL_LINEAR" }
            require(!(filter == GL11.GL_LINEAR && (mask and (GL11.GL_DEPTH_BUFFER_BIT or GL11.GL_STENCIL_BUFFER_BIT)) != 0)) { "Blits including depth or stencil must use GL_NEAREST" }
        }

        fun framebufferStatusName(status: Int): String {
            return when (status) {
                GL30.GL_FRAMEBUFFER_COMPLETE -> "GL_FRAMEBUFFER_COMPLETE"
                GL30.GL_FRAMEBUFFER_UNDEFINED -> "GL_FRAMEBUFFER_UNDEFINED"
                GL30.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT"
                GL30.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT"
                GL30.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER -> "GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER"
                GL30.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER -> "GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER"
                GL30.GL_FRAMEBUFFER_UNSUPPORTED -> "GL_FRAMEBUFFER_UNSUPPORTED"
                GL32.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE -> "GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE"
                GL32.GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS -> "GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS"
                else -> "UNKNOWN_FRAMEBUFFER_STATUS_" + status
            }
        }

        @JvmStatic
        fun soloAlbedo(width: Int, height: Int): FrameBuffer {
            return builder(width, height)
                .color(texture2D(ITexture.Type.RGBA16F))
                .build()
        }
    }
}