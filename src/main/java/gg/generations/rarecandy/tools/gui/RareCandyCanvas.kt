package gg.generations.rarecandy.tools.gui

import gg.generations.rarecandy.pokeutils.BlendType
import gg.generations.rarecandy.pokeutils.CullType
import gg.generations.rarecandy.pokeutils.resource.ResourceReader
import gg.generations.rarecandy.pokeutils.util.ExceptionThrowingRunnable
import gg.generations.rarecandy.renderer.components.DummyVAO
import gg.generations.rarecandy.renderer.components.MultiRenderObject
import gg.generations.rarecandy.renderer.loading.Names
import gg.generations.rarecandy.renderer.pipeline.traditional.TraditionalPipeline
import gg.generations.rarecandy.renderer.rendering.ObjectInstance
import gg.generations.rarecandy.renderer.rendering.RareCandy
import gg.generations.rarecandy.renderer.rendering.RenderStage
import gg.generations.rarecandy.renderer.rendering.StateManager
import gg.generations.rarecandy.renderer.storage.AnimatedObjectInstance
import gg.generations.rarecandy.renderer.textures.framebuffer.FrameBuffer
import gg.generations.rarecandy.renderer.textures.ITexture
import gg.generations.rarecandy.renderer.textures.framebuffer.TextureSpec
import gg.generations.rarecandy.renderer.ubo.UniformBlockUploader
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11C
import org.lwjgl.system.MemoryUtil
import java.io.IOException
import java.nio.file.Path
import java.util.function.Consumer
import kotlin.Boolean
import kotlin.Double
import kotlin.Exception
import kotlin.Int
import kotlin.Long
import kotlin.RuntimeException
import kotlin.String
import kotlin.Throws
import kotlin.collections.MutableList
import kotlin.math.max

class RareCandyCanvas(private val handler: PokeUtilsGui) {
    private var time = 0.0

    @JvmField
    val camera: Camera = Camera()

    @JvmField
    var scaleModifier = 1.0f
    val modelTranslation: Vector3f = Vector3f()
    var modelYaw = 0.0f
    var startTime: Double = System.currentTimeMillis().toDouble()
    @JvmField
    var currentAnimation: String? = null

    private var renderer = RareCandy()

    private var gridRenderer: ScreenSpaceGridRenderer? = null

    private var vao: DummyVAO? = null

    @JvmField
    var loadedModel: BaseMultiRenderObject? = null
    @JvmField
    var loadedModelInstance: AnimatedObjectInstance? = null
    lateinit var fogUploader: FogUploader
    private var rendering = false

    private val manager = StateManager(
        BlendType.Regular::enable, { BlendType.Regular.disable() },
        CullType.Forward::enable, CullType.Forward::disable,
        { GL11.glEnable(GL11.GL_DEPTH_TEST) }, { GL11.glDisable(GL11.GL_DEPTH_TEST) }
    )

    var gbuffer: FrameBuffer? = null

    fun resize(width: Int, height: Int) {
        var width = width
        var height = height
        width = max(1, width)
        height = max(1, height)

        val aspect = width.toFloat() / height

        camera.setProjectionMatrix { matrix -> matrix.perspective(Math.toRadians(100.0).toFloat(), aspect, 0.1f, 1000.0f) }

        gbuffer?.resize(width, height)
        RenderPasses.chain?.resize(width, height)
    }

    @JvmOverloads
    @Throws(Exception::class)
    fun openFile(
        pkFile: ResourceReader,
        name: String,
        runnable: ExceptionThrowingRunnable = ExceptionThrowingRunnable {},
        resetAnimation: Boolean = true
    ) {
        currentAnimation = null
        rendering = false

        ModelTracker.update(renderer, pkFile)
        if(resetAnimation) AnimationState.setAnimation("idle")
        runnable.run()
        rendering = true
    }

    fun initGL() {
        camera.setProjectionMatrix(Consumer { projection: Matrix4f? ->
            projection!!.perspective(
                Math.toRadians(100.0).toFloat(), handler.width.toFloat() / handler.height, 0.1f, 1000.0f
            )
        })
        GL.createCapabilities(true)
        GuiPipelines.onInitialize(this, handler.settings)

        fogUploader = FogUploader(handler.settings!!.fog)
        gridRenderer = ScreenSpaceGridRenderer()
        gbuffer = FrameBuffer.builder(handler.width, handler.height) {
            color(TextureSpec.texture2D(ITexture.Type.RGBA8).withFilters(GL11C.GL_NEAREST, GL11C.GL_NEAREST))
            color(TextureSpec.texture2D(ITexture.Type.RGBA16F).withFilters(GL11C.GL_NEAREST, GL11C.GL_NEAREST))
            color(TextureSpec.texture2D(ITexture.Type.RGBA16F).withFilters(GL11C.GL_NEAREST, GL11C.GL_NEAREST))
            color(TextureSpec.texture2D(ITexture.Type.R8).withFilters(GL11C.GL_NEAREST, GL11C.GL_NEAREST))
            depthTexture(TextureSpec.depth2D(ITexture.Type.DEPTH24, false))
        }.build()

        vao = DummyVAO()
    }


    private val size = Vector3f()


    /** 0 albedo, 1 normal, 2 emission, 3 selection.  */
    var gbufferDebugIndex: Int = 1

    fun render() {
        ModelTracker.update(time)

        if (loadedModelInstance != null) {
            loadedModelInstance!!.modelMatrix().identity()
                .translate(modelTranslation)
                .rotateY(modelYaw)
                .scale(this.scaleModifier)
            loadedModelInstance!!.normalMatrix().identity().rotateY(modelYaw)
            loadedModelInstance!!.use()
        }

        time = (System.currentTimeMillis() - startTime) / 1000f

        if (runnable != null) runnable!!.pre()

        renderer.update(time)

        vao!!.bind()

        DefferedPass.start(gbuffer, handler.clearColor())

        GuiPipelines.G_BUFFER.useProgram()
        GuiPipelines.G_BUFFER.bindGlobal()

        renderer.render(GuiPipelines.G_BUFFER, manager)
        gridRenderer!!.render(handler.settings, manager)

        gbuffer!!.unbind()
        manager.reset()

        RenderPasses.chain?.render(gbuffer!!, handler.width, handler.height)

        renderer.end()

        if (runnable != null) runnable!!.post()
    }


    fun stopRenderingAfterFailure() {
        rendering = false
    }

    init {
        resize(handler.width, handler.height)
    }

    @Throws(IOException::class)
    fun takeScreenshot(isPortrait: Boolean) {
//        var path = root.resolve(fileName);
//        if(Files.notExists(path)) Files.createDirectories(path);
//
//        var temp = Path.of((path + "\\" + (isPortrait ? "portrait" : "profile") + "-" + (loadedModel.variantNameToId.get(loadedModelInstance.variant()) != null ? loadedModelInstance.variant() : "default") + ".png").replace("\\", "/"));
//        if(framebuffer.captureScreenshot(temp, isPortrait)) {
//            LoggerUtil.print("Screenshot saved to " + temp);
//        } else {
//            LoggerUtil.print("Failed to save screenshot to " + temp);
//        }
    }

    fun close() {
        fogUploader.close()
    }

    val width: Int
        get() = handler.width

    val height: Int
        get() = handler.height

    class CycleVariants(private val canvas: RareCandyCanvas, private val isPortrait: Boolean) {
        private var list: MutableList<String>
        private var index: Int

        init {
            cycling = true
            list = canvas.loadedModel?.availableVariants?.toMutableList() ?: mutableListOf()

            index = 0
            runnable = this
        }

        fun pre() {
            if (index >= list.size) {
                runnable = null
                cycling = false
            } else {
                canvas.loadedModelInstance!!.setVariant(index)
            }
        }

        fun post() {
            try {
                canvas.takeScreenshot(isPortrait)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
            index += 1
        }
    }

    class BaseMultiRenderObject(names: Names) : MultiRenderObject(names) {
        override fun render(
            pipeline: TraditionalPipeline,
            stage: RenderStage,
            instances: MutableList<ObjectInstance>
        ) {
            drawBuffer?.get(stage)?.render()
        }

        override fun targetVertexStride(): Int {
            return 32
        }
    }

    companion object {
        private var runnable: CycleVariants? = null

        @JvmField
        var lightLevel: Float = 1f
        @JvmField
        var cycling: Boolean = false
        @JvmField
        var animate: Boolean = true
        val images: Path = Path.of("assets", "generations_core", "textures", "pokemon")
    }
}

class FogUploader(fog: PokeUtilsGui.Settings.Fog) :
    UniformBlockUploader(VEC4_SIZE + 2 * Float.SIZE_BYTES + Integer.BYTES + 4, 0) {
    private val pointer: Long = MemoryUtil.nmemAlloc((VEC4_SIZE + 2 * Float.SIZE_BYTES + Integer.BYTES).toLong())

    init {
        update(fog)
        fog.setListener(Consumer { fog: PokeUtilsGui.Settings.Fog? -> this.update(fog!!) })
    }

    private fun update(fog: PokeUtilsGui.Settings.Fog) {
        fog.color.getToAddress(pointer)
        MemoryUtil.memPutFloat(pointer + 16, fog.start.toFloat())
        MemoryUtil.memPutFloat(pointer + 20, fog.end.toFloat())
        MemoryUtil.memPutInt(pointer + 24, 0)

        upload(0, 28, pointer)
    }
}


