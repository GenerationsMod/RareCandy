package gg.generations.rarecandy.renderer.launch;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.awt.GLData;

import java.util.Objects;

public final class OpenGL {
    public final int majorVersion;
    public final int minorVersion;

    public OpenGL() {
        var caps = GL.createCapabilities();
        GL.setCapabilities(null);
        this.majorVersion = 4;
        this.minorVersion = 6;
    }

    public void glfwCreate() {
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_CORE_PROFILE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_VERSION_MAJOR, majorVersion);
        GLFW.glfwWindowHint(GLFW.GLFW_VERSION_MINOR, minorVersion);
    }

    public GLData awtCreate() {
        var data = new GLData();
        data.api = GLData.API.GL;
        data.profile = GLData.Profile.CORE;
        data.forwardCompatible = false;
        data.majorVersion = majorVersion;
        data.minorVersion = minorVersion;
        return data;
    }

    @Override
    public int hashCode() {
        return Objects.hash(majorVersion, minorVersion);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (OpenGL) obj;
        return this.majorVersion == that.majorVersion &&
               this.minorVersion == that.minorVersion;
    }

    @Override
    public String toString() {
        return "OpenGL " + majorVersion + "." + minorVersion;
    }
}
