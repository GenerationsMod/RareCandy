package gg.generations.rarecandy.renderer.textures;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.opengl.ARBBindlessTexture.*;
import static org.lwjgl.opengl.GL12C.GL_FALSE;
import static org.lwjgl.opengl.GL12C.GL_TRUE;

/**
 * Cache for bindless image handles (image load/store).
 * Keys include texture id, mip level, layered flag, internal format, and access mode.
 */
public final class ImageHandleCache {
    private static final Map<Key, Long> HANDLES = new ConcurrentHashMap<>();

    private ImageHandleCache() {}

    public static long getOrCreate(int textureId, int level, boolean layered, int internalFormat, ITexture.ComputeAccess access) {
        Key k = new Key(textureId, level, layered, internalFormat, access);
        return HANDLES.computeIfAbsent(k, key -> {
            long handle = glGetImageHandleARB(textureId, level, layered, 0, internalFormat);
            glMakeImageHandleResidentARB(handle, access.getValue());
            return handle;
        });
    }

    public static void unresidentForTexture(int textureId) {
        HANDLES.entrySet().removeIf(e -> {
            Key k = e.getKey();
            if (k.textureId == textureId) {
                glMakeImageHandleNonResidentARB(e.getValue());
                return true;
            }
            return false;
        });
    }

    private record Key(int textureId, int level, boolean layered, int internalFormat, ITexture.ComputeAccess access) {}
}