package gg.generations.rarecandy.renderer.textures.framebuffer

import gg.generations.rarecandy.renderer.textures.ITexture
import gg.generations.rarecandy.renderer.textures.ITexture.ComputeAccess
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import kotlin.math.max
import kotlin.math.min

/**
 * Describes one texture attachment.
 * 
 * 
 * For external (non-owned) specs, `externalWidth`/`externalHeight` are the
 * dimensions of the attached image *at `level`*, and must equal the framebuffer's
 * allocation size.
 */
@JvmRecord
data class TextureSpec(
    val target: TextureTarget?, val type: ITexture.Type?, val levels: Int, val layers: Int, val samples: Int,
    val fixedSampleLocations: Boolean, val minFilter: Int, val magFilter: Int, val wrapS: Int, val wrapT: Int,
    val wrapR: Int, val compareMode: Boolean, val compareFunc: Int, val computeAccess: ComputeAccess?,
    val owned: Boolean, val existingId: Int, val externalWidth: Int, val externalHeight: Int,
    val attachKind: TextureAttachKind?, val level: Int, val layer: Int
) {
    fun withLevels(levels: Int): TextureSpec {
        return TextureSpec(
            target, type, levels, layers, samples, fixedSampleLocations, minFilter, magFilter,
            wrapS, wrapT, wrapR, compareMode, compareFunc, computeAccess, owned, existingId, externalWidth,
            externalHeight, attachKind, level, layer
        )
    }


    fun withFilters(minFilter: Int, magFilter: Int): TextureSpec {

        return TextureSpec(
            target, type, levels, layers, samples, fixedSampleLocations, minFilter, magFilter,
            wrapS, wrapT, wrapR, compareMode, compareFunc, computeAccess, owned, existingId, externalWidth,
            externalHeight, attachKind, level, layer
        )
    }

    fun withWrap(wrapS: Int, wrapT: Int, wrapR: Int): TextureSpec {
        return TextureSpec(
            target, type, levels, layers, samples, fixedSampleLocations, minFilter, magFilter,
            wrapS, wrapT, wrapR, compareMode, compareFunc, computeAccess, owned, existingId, externalWidth,
            externalHeight, attachKind, level, layer
        )
    }

    fun withCompareMode(compareMode: Boolean, compareFunc: Int): TextureSpec {
        return TextureSpec(
            target, type, levels, layers, samples, fixedSampleLocations, minFilter, magFilter,
            wrapS, wrapT, wrapR, compareMode, compareFunc, computeAccess, owned, existingId, externalWidth,
            externalHeight, attachKind, level, layer
        )
    }

    fun withComputeAccess(access: ComputeAccess?): TextureSpec {
        return TextureSpec(
            target, type, levels, layers, samples, fixedSampleLocations, minFilter, magFilter,
            wrapS, wrapT, wrapR, compareMode, compareFunc, access, owned, existingId, externalWidth,
            externalHeight, attachKind, level, layer
        )
    }

    companion object {
        @JvmStatic
        fun texture2D(type: ITexture.Type): TextureSpec {
            val filter = if (type.isInteger) GL11.GL_NEAREST else GL11.GL_LINEAR
            return TextureSpec(
                TextureTarget.TEXTURE_2D,
                type,
                1,
                1,
                1,
                true,
                filter,
                filter,
                GL12.GL_CLAMP_TO_EDGE,
                GL12.GL_CLAMP_TO_EDGE,
                GL12.GL_CLAMP_TO_EDGE,
                false,
                GL11.GL_LEQUAL,
                ComputeAccess.READ_ONLY,
                true,
                0,
                0,
                0,
                TextureAttachKind.WHOLE_TEXTURE,
                0,
                0
            )
        }

        fun texture2DMultisample(type: ITexture.Type?, samples: Int, fixedSampleLocations: Boolean): TextureSpec {
            return TextureSpec(
                TextureTarget.TEXTURE_2D_MULTISAMPLE,
                type,
                1,
                1,
                samples,
                fixedSampleLocations,
                GL11.GL_NEAREST,
                GL11.GL_NEAREST,
                GL12.GL_CLAMP_TO_EDGE,
                GL12.GL_CLAMP_TO_EDGE,
                GL12.GL_CLAMP_TO_EDGE,
                false,
                GL11.GL_LEQUAL,
                ComputeAccess.READ_ONLY,
                true,
                0,
                0,
                0,
                TextureAttachKind.WHOLE_TEXTURE,
                0,
                0
            )
        }

        fun texture2DArray(type: ITexture.Type, layers: Int): TextureSpec {
            val filter = if (type.isInteger) GL11.GL_NEAREST else GL11.GL_LINEAR
            return TextureSpec(
                TextureTarget.TEXTURE_2D_ARRAY,
                type,
                1,
                layers,
                1,
                true,
                filter,
                filter,
                GL12.GL_CLAMP_TO_EDGE,
                GL12.GL_CLAMP_TO_EDGE,
                GL12.GL_CLAMP_TO_EDGE,
                false,
                GL11.GL_LEQUAL,
                ComputeAccess.READ_ONLY,
                true,
                0,
                0,
                0,
                TextureAttachKind.WHOLE_TEXTURE,
                0,
                0
            )
        }

        fun texture2DArrayLayer(type: ITexture.Type, layers: Int, layer: Int, level: Int): TextureSpec {
            val filter = if (type.isInteger) GL11.GL_NEAREST else GL11.GL_LINEAR
            return TextureSpec(
                TextureTarget.TEXTURE_2D_ARRAY,
                type,
                max(1, level + 1),
                layers,
                1,
                true,
                filter,
                filter,
                GL12.GL_CLAMP_TO_EDGE,
                GL12.GL_CLAMP_TO_EDGE,
                GL12.GL_CLAMP_TO_EDGE,
                false,
                GL11.GL_LEQUAL,
                ComputeAccess.READ_ONLY,
                true,
                0,
                0,
                0,
                TextureAttachKind.LAYER,
                level,
                layer
            )
        }

        fun texture2DMultisampleArray(
            type: ITexture.Type?,
            layers: Int,
            samples: Int,
            fixedSampleLocations: Boolean
        ): TextureSpec {
            return TextureSpec(
                TextureTarget.TEXTURE_2D_MULTISAMPLE_ARRAY,
                type,
                1,
                layers,
                samples,
                fixedSampleLocations,
                GL11.GL_NEAREST,
                GL11.GL_NEAREST,
                GL12.GL_CLAMP_TO_EDGE,
                GL12.GL_CLAMP_TO_EDGE,
                GL12.GL_CLAMP_TO_EDGE,
                false,
                GL11.GL_LEQUAL,
                ComputeAccess.READ_ONLY,
                true,
                0,
                0,
                0,
                TextureAttachKind.WHOLE_TEXTURE,
                0,
                0
            )
        }

        fun texture2DMultisampleArrayLayer(
            type: ITexture.Type?,
            layers: Int,
            samples: Int,
            fixedSampleLocations: Boolean,
            layer: Int
        ): TextureSpec {
            return TextureSpec(
                TextureTarget.TEXTURE_2D_MULTISAMPLE_ARRAY,
                type,
                1,
                layers,
                samples,
                fixedSampleLocations,
                GL11.GL_NEAREST,
                GL11.GL_NEAREST,
                GL12.GL_CLAMP_TO_EDGE,
                GL12.GL_CLAMP_TO_EDGE,
                GL12.GL_CLAMP_TO_EDGE,
                false,
                GL11.GL_LEQUAL,
                ComputeAccess.READ_ONLY,
                true,
                0,
                0,
                0,
                TextureAttachKind.LAYER,
                0,
                layer
            )
        }

        fun cubeMap(type: ITexture.Type): TextureSpec {
            val filter = if (type.isInteger) GL11.GL_NEAREST else GL11.GL_LINEAR
            return TextureSpec(
                TextureTarget.TEXTURE_CUBE_MAP,
                type,
                1,
                6,
                1,
                true,
                filter,
                filter,
                GL12.GL_CLAMP_TO_EDGE,
                GL12.GL_CLAMP_TO_EDGE,
                GL12.GL_CLAMP_TO_EDGE,
                false,
                GL11.GL_LEQUAL,
                ComputeAccess.READ_ONLY,
                true,
                0,
                0,
                0,
                TextureAttachKind.WHOLE_TEXTURE,
                0,
                0
            )
        }

        fun cubeFace(type: ITexture.Type, face: Int, level: Int): TextureSpec {
            val filter = if (type.isInteger) GL11.GL_NEAREST else GL11.GL_LINEAR
            return TextureSpec(
                TextureTarget.TEXTURE_CUBE_MAP,
                type,
                max(1, level + 1),
                6,
                1,
                true,
                filter,
                filter,
                GL12.GL_CLAMP_TO_EDGE,
                GL12.GL_CLAMP_TO_EDGE,
                GL12.GL_CLAMP_TO_EDGE,
                false,
                GL11.GL_LEQUAL,
                ComputeAccess.READ_ONLY,
                true,
                0,
                0,
                0,
                TextureAttachKind.LAYER,
                level,
                face
            )
        }

        fun external(
            target: TextureTarget?,
            type: ITexture.Type?,
            existingId: Int,
            width: Int,
            height: Int,
            levels: Int,
            layers: Int,
            samples: Int,
            attachKind: TextureAttachKind?,
            level: Int,
            layer: Int,
            access: ComputeAccess?
        ): TextureSpec {
            return TextureSpec(
                target,
                type,
                levels,
                layers,
                samples,
                true,
                GL11.GL_NEAREST,
                GL11.GL_NEAREST,
                GL12.GL_CLAMP_TO_EDGE,
                GL12.GL_CLAMP_TO_EDGE,
                GL12.GL_CLAMP_TO_EDGE,
                false,
                GL11.GL_LEQUAL,
                if (access == null) ComputeAccess.READ_ONLY else access,
                false,
                existingId,
                width,
                height,
                attachKind,
                level,
                layer
            )
        }

        fun depth2D(type: ITexture.Type?, compareMode: Boolean): TextureSpec {
            return TextureSpec(
                TextureTarget.TEXTURE_2D,
                type,
                1,
                1,
                1,
                true,
                GL11.GL_NEAREST,
                GL11.GL_NEAREST,
                GL12.GL_CLAMP_TO_EDGE,
                GL12.GL_CLAMP_TO_EDGE,
                GL12.GL_CLAMP_TO_EDGE,
                compareMode,
                GL11.GL_LEQUAL,
                ComputeAccess.READ_ONLY,
                true,
                0,
                0,
                0,
                TextureAttachKind.WHOLE_TEXTURE,
                0,
                0
            )
        }
    }
}
