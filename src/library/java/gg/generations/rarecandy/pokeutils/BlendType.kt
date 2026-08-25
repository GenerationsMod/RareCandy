package gg.generations.rarecandy.pokeutils

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import gg.generations.rarecandy.codec.enumCodec
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import java.util.*

enum class BlendType(
    private val rgbSrc: Int,
    private val rgbDst: Int,
    private val alphaSrc: Int,
    private val alphaDst: Int
) {
    None(-1, -1, -1, -1), Regular(
        GL11.GL_SRC_ALPHA,
        GL11.GL_ONE_MINUS_SRC_ALPHA,
        GL11.GL_ONE,
        GL20.GL_ONE_MINUS_SRC_ALPHA
    );

    fun enable() {
        if (this != BlendType.Regular) return
        GL20.glBlendFuncSeparate(rgbSrc, rgbDst, alphaSrc, alphaDst)
        GL11.glEnable(GL11.GL_BLEND)
    }

    fun disable() {
        if (this != BlendType.Regular) return
        GL11.glDisable(GL11.GL_BLEND)
    }

    fun toJson(): JsonElement {
        return JsonPrimitive(name.lowercase(Locale.getDefault()))
    }

    companion object {
        val CODEC = enumCodec<BlendType>()

        fun from(cull: String): BlendType {
            try {
                if (cull.equals("regular", ignoreCase = true)) return BlendType.Regular
                else return BlendType.None
            } catch (e: Exception) {
                return BlendType.None
            }
        }

        fun fromJson(element: JsonElement): BlendType {
            return from(element.getAsJsonPrimitive().getAsString())
        }
    }
}
