package gg.generations.rarecandy.pokeutils

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import gg.generations.rarecandy.codec.Codec
import gg.generations.rarecandy.codec.enumCodec
import org.lwjgl.opengl.GL11
import java.util.*

enum class CullType(val glConstant: Int) {
    Back(GL11.GL_BACK),
    Forward(GL11.GL_BACK),
    None(-1);

    fun enable() {
        if (glConstant != -1) {
            // Save previous state
            wasCullingEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE)
            if (wasCullingEnabled) {
                previousCullFace = GL11.glGetInteger(GL11.GL_CULL_FACE_MODE)
            }

            GL11.glEnable(GL11.GL_CULL_FACE)
            GL11.glCullFace(glConstant)
        }
    }

    fun disable() {
        if (glConstant != -1) {
            GL11.glDisable(GL11.GL_CULL_FACE)
        }
    }

    fun toJson(): JsonElement {
        return JsonPrimitive(name.lowercase(Locale.getDefault()))
    }

    companion object {
        private var wasCullingEnabled = false
        private var previousCullFace = 0

        val CODEC = enumCodec<CullType>()

        fun from(cull: String?): CullType {
            if (cull == null) return CullType.None
            return when (cull.lowercase(Locale.getDefault())) {
                "back" -> CullType.Back
                "forward" -> CullType.Forward
                else -> CullType.None
            }
        }

        fun restorePreviousState() {
            if (wasCullingEnabled) {
                GL11.glEnable(GL11.GL_CULL_FACE)
                GL11.glCullFace(previousCullFace)
            } else {
                GL11.glDisable(GL11.GL_CULL_FACE)
            }
        }

        fun fromJson(element: JsonElement): CullType {
            return from(element.getAsJsonPrimitive().getAsString())
        }
    }
}