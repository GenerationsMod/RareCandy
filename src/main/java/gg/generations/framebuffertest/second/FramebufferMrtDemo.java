//package gg.generations.framebuffertest.second;
//
//import gg.generations.rarecandy.renderer.launch.OpenGL;
//import gg.generations.rarecandy.renderer.textures.ITexture;
//import gg.generations.rarecandy.tools.AppBase;
//import imgui.ImGui;
//import imgui.type.ImBoolean;
//import imgui.type.ImInt;
//import org.joml.Vector4f;
//
//import static org.lwjgl.opengl.GL11.*;
//import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
//import static org.lwjgl.opengl.GL30.glBindFramebuffer;
//
//public final class FramebufferMrtDemo extends AppBase {
//    private final Vector4f offscreenClear = new Vector4f(0.08f, 0.08f, 0.08f, 1.0f);
//
//    private FrameBuffer framebuffer;
//    private FullscreenTriangle fullscreenTriangle;
//    private ScreenTextureShader screenShader;
//
//    private SimpleTriangle simpleTriangle;
//    private MrtFlatColorShader mrtShader;
//
//    private ImInt previewAttachment = new ImInt(0);
//    private ImBoolean drawTriangle = new ImBoolean(true);
//
//    public FramebufferMrtDemo() {
//        super("Framebuffer MRT Demo", 1280, 720, new OpenGL());
//    }
//
//    @Override
//    protected void initGL() {
//        framebuffer = createFramebuffer(width, height);
//
//        fullscreenTriangle = new FullscreenTriangle();
//        screenShader = new ScreenTextureShader();
//
//        simpleTriangle = new SimpleTriangle();
//        mrtShader = new MrtFlatColorShader();
//    }
//
//    private static FrameBuffer createFramebuffer(int width, int height) {
//        FramebufferSpec spec = FramebufferSpec.builder(width, height)
//                .colorTexture(ITexture.Type.RGBA8)
//                .colorTexture(ITexture.Type.RGBA8)
//                .depthTexture(ITexture.Type.DEPTH24)
//                .build();
//
//        return new FrameBuffer(spec, new DefaultAttachmentAllocator());
//    }
//
//    @Override
//    protected void onResize(int width, int height) {
//        if (framebuffer != null && width > 0 && height > 0) {
//            framebuffer.resize(width, height);
//        }
//    }
//
//    @Override
//    public Vector4f clearColor() {
//        return new Vector4f(0.02f, 0.02f, 0.02f, 1.0f);
//    }
//
//    @Override
//    protected void renderGui() {
//        ImGui.begin("Framebuffer MRT Demo");
//
//        ImGui.text("Window: " + width + " x " + height);
//
//        if (framebuffer != null) {
//            ImGui.text("Framebuffer: " + framebuffer.width() + " x " + framebuffer.height());
//            ImGui.text("Color attachments: " + framebuffer.colorAttachments().size());
//            ImGui.text("Depth attachment present: " + (framebuffer.depthAttachment() != null));
//        }
//
//        float[] clear = {offscreenClear.x, offscreenClear.y, offscreenClear.z, offscreenClear.w};
//        if (ImGui.colorEdit4("Offscreen Clear", clear)) {
//            offscreenClear.set(clear[0], clear[1], clear[2], clear[3]);
//        }
//
//        if (ImGui.checkbox("Draw Triangle", drawTriangle)) {
//        }
//
//        String[] previewOptions = {"Attachment 0", "Attachment 1"};
//        if (ImGui.combo("Preview Attachment", previewAttachment, previewOptions)) {
//        }
//
//        ImGui.text("Expected:");
//        ImGui.text("Attachment 0 = red-tinted output");
//        ImGui.text("Attachment 1 = green-tinted output");
//
//        ImGui.end();
//    }
//
//    @Override
//    protected void render() {
//        renderOffscreen();
//        presentFramebuffer();
//    }
//
//    private void renderOffscreen() {
//        framebuffer.bind();
//        glViewport(0, 0, framebuffer.width(), framebuffer.height());
//
//        glEnable(GL_DEPTH_TEST);
//        glClearColor(offscreenClear.x, offscreenClear.y, offscreenClear.z, offscreenClear.w);
//        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//
//        if (drawTriangle.get()) {
//            mrtShader.use();
//            mrtShader.setColors(
//                    1.0f, 0.15f, 0.15f, 1.0f,
//                    0.15f, 1.0f, 0.15f, 1.0f
//            );
//            simpleTriangle.draw();
//        }
//    }
//
//    private void presentFramebuffer() {
//        glBindFramebuffer(GL_FRAMEBUFFER, 0);
//        glViewport(0, 0, width, height);
//
//        glDisable(GL_DEPTH_TEST);
//
//        screenShader.use();
//        framebuffer.colorTexture(previewAttachment.get()).bind(0);
//        screenShader.setTextureUnit(0);
//        fullscreenTriangle.draw();
//    }
//
//    public static void main(String[] args) {
//        new FramebufferMrtDemo().run();
//    }
//}