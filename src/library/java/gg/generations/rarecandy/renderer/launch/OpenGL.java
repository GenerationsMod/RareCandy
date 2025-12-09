package gg.generations.rarecandy.renderer.launch;

import java.util.Objects;

public final class OpenGL {
    public final int majorVersion;
    public final int minorVersion;

    public OpenGL() {
        this.majorVersion = 4;
        this.minorVersion = 5;
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
