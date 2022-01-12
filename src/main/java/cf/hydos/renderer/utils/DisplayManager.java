package cf.hydos.renderer.utils;

import cf.hydos.renderer.window.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class DisplayManager {

	private static final String TITLE = "ThinMatrix Animation Tutorial";
	private static final int WIDTH = 1280;
	private static final int HEIGHT = 720;
	private static final int FPS_CAP = 100;

	private static long lastFrameTime;
	private static float delta;

	public static void createDisplay() {
		new Window(TITLE, WIDTH, HEIGHT);
		GL11.glEnable(GL13.GL_MULTISAMPLE);
		GL11.glViewport(0, 0, WIDTH, HEIGHT);
		lastFrameTime = getCurrentTime();
	}

	public static void update() {
		long currentFrameTime = getCurrentTime();
		delta = (currentFrameTime - lastFrameTime) / 1000f;
		lastFrameTime = currentFrameTime;
	}

	public static float getFrameTime() {
		return delta;
	}

	public static void closeDisplay() {
		Window.getInstance().close();
	}

	private static long getCurrentTime() {
		return (long) (GLFW.glfwGetTime() * 1000);
	}
}
