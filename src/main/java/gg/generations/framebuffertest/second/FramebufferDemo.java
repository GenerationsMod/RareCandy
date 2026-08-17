package gg.generations.framebuffertest.second;

import gg.generations.rarecandy.renderer.launch.OpenGL;
import gg.generations.rarecandy.renderer.textures.ITexture;
import gg.generations.rarecandy.renderer.textures.framebuffer.DefaultAttachmentAllocator;
import gg.generations.rarecandy.renderer.textures.framebuffer.FrameBuffer;
import gg.generations.rarecandy.renderer.textures.framebuffer.FramebufferSpec;
import gg.generations.rarecandy.tools.AppBase;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

public final class FramebufferDemo extends AppBase {
    private static final float NEAR_PLANE = 0.1f;
    private static final float FAR_PLANE = 10.0f;

    private final Vector4f offscreenClear = new Vector4f(0.08f, 0.08f, 0.08f, 1.0f);

    private FrameBuffer framebuffer;
    private FullscreenTriangle fullscreenTriangle;
    private ScreenTextureShader screenShader;
    private DepthTextureShader depthShader;
    private FlatColorShader flatColorShader;
    private SimpleTriangle simpleTriangle;

    private final ImInt previewMode = new ImInt(0); // 0 = color, 1 = depth
    private final ImBoolean drawFrontTriangle = new ImBoolean(true);
    private final ImBoolean drawBackTriangle = new ImBoolean(true);

    private final Matrix4f projection = new Matrix4f();
    private final Matrix4f view = new Matrix4f();
    private final Matrix4f modelFront = new Matrix4f();
    private final Matrix4f modelBack = new Matrix4f();
    private final Matrix4f mvp = new Matrix4f();

    public FramebufferDemo() {
        super("Framebuffer Final Demo", 1280, 720, new OpenGL());
    }

    @Override
    protected void initGL() {
        framebuffer = createFramebuffer(getWidth(), getHeight());

        fullscreenTriangle = new FullscreenTriangle();
        screenShader = new ScreenTextureShader();
        depthShader = new DepthTextureShader();
        flatColorShader = new FlatColorShader();
        simpleTriangle = new SimpleTriangle();

        rebuildMatrices(getWidth(), getHeight());
    }

    private static FrameBuffer createFramebuffer(int width, int height) {
        FramebufferSpec spec = FramebufferSpec.builder(width, height)
                .colorTexture(ITexture.Type.RGBA8)
                .depthTexture(ITexture.Type.DEPTH32F)
                .build();

        return new FrameBuffer(spec, new DefaultAttachmentAllocator());
    }

    @Override
    protected void onResize(int width, int height) {
        if (width <= 0 || height <= 0) return;

        if (framebuffer != null) {
            framebuffer.resize(width, height);
        }

        rebuildMatrices(width, height);
    }

    private void rebuildMatrices(int width, int height) {
        float aspect = (float) width / (float) height;

        projection.identity().perspective((float) Math.toRadians(60.0), aspect, NEAR_PLANE, FAR_PLANE);
        view.identity().lookAt(
                0.0f, 0.0f, 2.5f,
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f
        );

        modelFront.identity().translate(0.0f, 0.0f, 0.0f);
        modelBack.identity().translate(0.15f, -0.10f, -0.8f);
    }

    @Override
    public Vector4f clearColor() {
        return new Vector4f(0.02f, 0.02f, 0.02f, 1.0f);
    }

    @Override
    protected void renderGui() {
        ImGui.begin("Framebuffer Final Demo");

        ImGui.text("Window: " + getWidth() + " x " + getHeight());

        if (framebuffer != null) {
            ImGui.text("Framebuffer: " + framebuffer.width() + " x " + framebuffer.height());
            ImGui.text("Color attachment present: " + (framebuffer.colorAttachments().size() > 0));
            ImGui.text("Depth texture present: " + (framebuffer.depthTexture() != null));
            if (framebuffer.depthTexture() != null) {
                ImGui.text("Depth type: " + framebuffer.depthTexture().type().name());
            }
        }

        float[] clear = {offscreenClear.x, offscreenClear.y, offscreenClear.z, offscreenClear.w};
        if (ImGui.colorEdit4("Offscreen Clear", clear)) {
            offscreenClear.set(clear[0], clear[1], clear[2], clear[3]);
        }

        ImGui.checkbox("Draw Front Triangle", drawFrontTriangle);
        ImGui.checkbox("Draw Back Triangle", drawBackTriangle);

        String[] previewOptions = {"Color", "Depth"};
        ImGui.combo("Preview", previewMode, previewOptions, previewOptions.length);

        ImGui.text("Front triangle should occlude the back triangle in color view.");
        ImGui.text("Depth preview should show front triangle as nearer than back triangle.");

        ImGui.end();
    }

    @Override
    protected void render() {
        renderOffscreen();
        presentFramebuffer();
    }

    private void renderOffscreen() {
        framebuffer.bind();
        glViewport(0, 0, framebuffer.width(), framebuffer.height());

        glEnable(GL_DEPTH_TEST);
        glDepthMask(true);
        glClearColor(offscreenClear.x, offscreenClear.y, offscreenClear.z, offscreenClear.w);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        flatColorShader.use();

        if (drawBackTriangle.get()) {
            projection.mul(view, mvp).mul(modelBack);
            flatColorShader.setMvp(mvp);
            flatColorShader.setColor(0.15f, 0.30f, 1.0f, 1.0f);
            simpleTriangle.draw();
        }

        if (drawFrontTriangle.get()) {
            projection.mul(view, mvp).mul(modelFront);
            flatColorShader.setMvp(mvp);
            flatColorShader.setColor(0.10f, 1.0f, 0.20f, 1.0f);
            simpleTriangle.draw();
        }
    }

    private void presentFramebuffer() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, getWidth(), getHeight());

        glDisable(GL_DEPTH_TEST);

        if (previewMode.get() == 0) {
            screenShader.use();
            framebuffer.colorTexture(0).bind(0);
            screenShader.setTextureUnit(0);
        } else {
            depthShader.use();
            framebuffer.depthTexture().bind(0);
            depthShader.setTextureUnit(0);
            depthShader.setNearFar(NEAR_PLANE, FAR_PLANE);
        }

        fullscreenTriangle.draw();
    }

    @Override
    protected void cleanupGL() {
        closeQuietly(simpleTriangle);
        closeQuietly(fullscreenTriangle);
        closeQuietly(flatColorShader);
        closeQuietly(depthShader);
        closeQuietly(screenShader);
        closeQuietly(framebuffer);
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to close resource: " + closeable.getClass().getName(), e);
        }
    }

    public static void main(String[] args) {
        new FramebufferDemo().run();
    }
}