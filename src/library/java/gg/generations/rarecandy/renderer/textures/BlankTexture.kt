package gg.generations.rarecandy.renderer.textures

import gg.generations.rarecandy.renderer.textures.ITexture.ComputeAccess
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11C
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL42
import java.util.*

class BlankTexture(override val type: ITexture.Type, override val width: Int, override val height: Int, override val access: ComputeAccess, override val id: Int = GL11.glGenTextures()) : ITexture {

    init {
        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, id)
        GL42.glTexStorage2D(GL11C.GL_TEXTURE_2D, 1, type.internalFormat, width, height)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST) // or GL_LINEAR
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST) // or GL_LINEAR

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0)

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT)
        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, 0)
    }

    override fun equals(other: Any?): Boolean = (other as? BlankTexture)?.let { this.type == type && this.width == width && this.height == height } ?: false

    override fun hashCode(): Int = Objects.hash(type, width, height)

    override fun toString(): String = "BlankDetails[type=$type, width=$width, height=$height]"

    override fun delete() = GL11.glDeleteTextures(id)
}
