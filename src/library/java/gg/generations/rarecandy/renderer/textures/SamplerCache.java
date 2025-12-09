package gg.generations.rarecandy.renderer.textures;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.system.MemoryStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.opengl.ARBBindlessTexture.*;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_LOD_BIAS;
import static org.lwjgl.opengl.GL46.GL_TEXTURE_MAX_ANISOTROPY;
import static org.lwjgl.opengl.GL46C.glCreateSamplers;
import static org.lwjgl.opengl.GL46C.glSamplerParameterf;
import static org.lwjgl.opengl.GL46C.glSamplerParameteri;

/**
 * Global cache for GL sampler objects and bindless texture-sampler handles.
 */
public final class SamplerCache {
    private static final Map<SamplerDesc, Integer> SAMPLERS = new ConcurrentHashMap<>();
    private static final Map<Key, Long> HANDLES = new ConcurrentHashMap<>();

    private SamplerCache() {}

    public static int getOrCreateSampler(SamplerDesc desc) {
        return SAMPLERS.computeIfAbsent(desc, SamplerCache::createSampler);
    }

    public static long getOrCreateHandle(int textureId, SamplerDesc desc) {
        int sampler = getOrCreateSampler(desc);
        Key key = new Key(textureId, sampler);
        return HANDLES.computeIfAbsent(key, k -> {
            long handle = glGetTextureSamplerHandleARB(textureId, sampler);
            glMakeTextureHandleResidentARB(handle);
            return handle;
        });
    }

    public static void clear() {
        // does not unresident handles; call explicitly if needed
        HANDLES.clear();
        for (int sampler : SAMPLERS.values()) {
            org.lwjgl.opengl.GL46C.glDeleteSamplers(sampler);
        }
        SAMPLERS.clear();
    }

    private static int createSampler(SamplerDesc d) {
        try (MemoryStack ignored = MemoryStack.stackPush()) {
            int sampler = glCreateSamplers();
            glSamplerParameteri(sampler, GL11C.GL_TEXTURE_MIN_FILTER, d.minFilter);
            glSamplerParameteri(sampler, GL11C.GL_TEXTURE_MAG_FILTER, d.magFilter);
            glSamplerParameteri(sampler, GL11C.GL_TEXTURE_WRAP_S, d.wrapS);
            glSamplerParameteri(sampler, GL11C.GL_TEXTURE_WRAP_T, d.wrapT);
            glSamplerParameteri(sampler, GL12C.GL_TEXTURE_WRAP_R, d.wrapR);
            if (GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
                glSamplerParameterf(sampler, GL_TEXTURE_MAX_ANISOTROPY_EXT, Math.max(1.0f, d.anisotropy));
            }            glSamplerParameterf(sampler, GL_TEXTURE_LOD_BIAS, d.lodBias);
            glSamplerParameterf(sampler, GL12C.GL_TEXTURE_MIN_LOD, d.minLod);
            glSamplerParameterf(sampler, GL12C.GL_TEXTURE_MAX_LOD, d.maxLod);
            if (d.compareEnable) {
                glSamplerParameteri(sampler, GL33C.GL_TEXTURE_COMPARE_MODE, GL33C.GL_COMPARE_REF_TO_TEXTURE);
                glSamplerParameteri(sampler, GL33C.GL_TEXTURE_COMPARE_FUNC, d.compareFunc);
            } else {
                glSamplerParameteri(sampler, GL33C.GL_TEXTURE_COMPARE_MODE, GL11C.GL_NONE);
            }
            return sampler;
        }
    }

    /** Make all handles for the given texture non-resident and evict them. */
    public static void unresidentForTexture(int textureId) {
        HANDLES.entrySet().removeIf(e -> {
            if (e.getKey().textureId == textureId) {
                glMakeTextureHandleNonResidentARB(e.getValue());
                return true;
            }
            return false;
        });
    }

    private record Key(int textureId, int samplerId) {}
}