package cf.hydos.engine.rendering;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.system.MemoryUtil.NULL;

@Deprecated
public class Window {
    static final IntBuffer w = BufferUtils.createIntBuffer(1);
    static final IntBuffer h = BufferUtils.createIntBuffer(1);
    public static int WIDTH = 0;
    public static int HEIGHT = 0;
    public static Mouse Mouse;
    protected static long m_window;
    protected static GLFWErrorCallback m_errorCallback;
    protected static GLFWWindowSizeCallback m_resizeCallBack;

    public static void CreateWindow(int width, int height, String title) {
        try {
            if (!glfwInit()) throw new Exception("GLFW Initialization failed.");
            glfwSetErrorCallback(m_errorCallback = GLFWErrorCallback.createPrint(System.err));
            glfwDefaultWindowHints();
            glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
            glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);
            glfwWindowHint(GLFW_REFRESH_RATE, GLFW_DONT_CARE);
            glfwWindowHint(GLFW_DOUBLEBUFFER, GL_TRUE);

            Window.WIDTH = width;
            Window.HEIGHT = height;
            m_window = glfwCreateWindow(width, height, title, NULL, NULL);
            if (m_window == 0) throw new Exception("GLFW Window creation failed.");
            GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            // Center our window
            glfwSetWindowPos(m_window, (videoMode.width() - WIDTH) / 2, (videoMode.height() - HEIGHT) / 2);

            Mouse = new Mouse();
            Mouse.Create(m_window);

            glfwSetWindowSizeCallback(m_window, m_resizeCallBack = new GLFWWindowSizeCallback() {
                @Override
                public void invoke(long arg0, int arg1, int arg2) {
                    Window.WIDTH = arg1;
                    Window.HEIGHT = arg2;
                }
            });
            glfwMakeContextCurrent(m_window);
            glfwSwapInterval(0);
            glfwShowWindow(m_window);
            GL.createCapabilities();
            int vao = GL30.glGenVertexArrays();
            GL30.glBindVertexArray(vao);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void Update() {
        glfwPollEvents();
    }

    public static void Render() {
        glfwSwapBuffers(m_window);
    }

    public static void Dispose() {
        glfwDestroyWindow(m_window);
    }

    public static boolean IsCloseRequested() {
        return glfwWindowShouldClose(m_window);//Display.isCloseRequested();
    }

    public static int GetWidth() {
        glfwGetWindowSize(m_window, w, h);
        WIDTH = w.get(0);
        HEIGHT = h.get(0);
        return w.get(0);
    }

    public static int GetHeight() {
        glfwGetWindowSize(m_window, w, h);
        WIDTH = w.get(0);
        HEIGHT = h.get(0);
        return h.get(0);
    }

    public static class Mouse {
        public GLFWCursorPosCallback callback;
        public GLFWScrollCallback scallback;
        public double x;
        public double y;
        public double dx;
        public double dy;

        public double wheel;

        public void Create(long window) {
            glfwSetCursorPosCallback(window, callback = new GLFWCursorPosCallback() {

                @Override
                public void invoke(long window, double xpos, double ypos) {
                    // Add delta of x and y mouse coordinates
                    dx += (int) xpos - x;
                    dy += (int) xpos - y;
                    // Set new positions of x and y
                    x = (int) xpos;
                    y = (int) ypos;
                }
            });
            glfwSetScrollCallback(m_window, scallback = new GLFWScrollCallback() {

                @Override
                public void invoke(long arg0, double arg1, double arg2) {
                    wheel += arg2;
                }
            });
        }
    }
}
