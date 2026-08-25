package gg.generations.rarecandy.renderer.textures.framebuffer

import gg.generations.rarecandy.renderer.textures.ITexture
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL42
import org.lwjgl.opengl.GL45C

/**
 * A live attachment. {@code width}/{@code height} are the dimensions of the attached image at its
 * mip level, not the framebuffer's allocation size.
 *
 * <p>Owned attachments are deleted by the framebuffer — do not close one yourself, and do not
 * hold onto a record across {@link #resize}.
 */
data class TextureAttachment(
    override val id: Int, override val width: Int, override val height: Int, override val type: ITexture.Type, override val access: ITexture.ComputeAccess,
    val textureTarget: Int, val multisample: Boolean, val owned: Boolean) : ITexture {

    override fun bind(slot: Int) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + slot)
        GL11.glBindTexture(textureTarget, id)
    }

    fun bindImage(unit: Int, level: Int, layered: Boolean) {
        if (multisample) {
            throw IllegalStateException()
        }
        GL42.glBindImageTexture(unit, id, level, layered, 0, access.value, type.internalFormat)
    }

    override fun delete() {
        if (owned) {
            GL45C.glDeleteTextures(id)
        }
    }
}
