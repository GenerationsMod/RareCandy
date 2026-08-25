package gg.generations.rarecandy.renderer.textures.framebuffer

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL32

enum class TextureTarget(@JvmField val glTarget: Int, @JvmField val layered: Boolean, @JvmField val multisample: Boolean) {
    TEXTURE_2D(GL11.GL_TEXTURE_2D, false, false),
    TEXTURE_2D_ARRAY(GL30.GL_TEXTURE_2D_ARRAY, true, false),
    TEXTURE_CUBE_MAP(GL13.GL_TEXTURE_CUBE_MAP, true, false),
    TEXTURE_2D_MULTISAMPLE(GL32.GL_TEXTURE_2D_MULTISAMPLE, false, true),
    TEXTURE_2D_MULTISAMPLE_ARRAY(GL32.GL_TEXTURE_2D_MULTISAMPLE_ARRAY, true, true)
}
