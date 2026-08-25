package gg.generations.rarecandy.renderer.textures.framebuffer;

import gg.generations.rarecandy.renderer.textures.ITexture;

public data class RenderbufferSpec(val format: ITexture.Type, val samples: Int, val owned: Boolean, val existingId: Int, val externalWidth: Int, val externalHeight: Int) {

    companion object {
        fun depth24(): RenderbufferSpec = RenderbufferSpec(ITexture.Type.DEPTH24, 1, true, 0, 0, 0)
        fun depth24Stencil8(): RenderbufferSpec = RenderbufferSpec(ITexture.Type.DEPTH24_STENCIL8, 1, true, 0, 0, 0)
        fun depth32fStencil8(): RenderbufferSpec = RenderbufferSpec(ITexture.Type.DEPTH32F_STENCIL8, 1, true, 0, 0, 0)
        fun stencil8(): RenderbufferSpec = RenderbufferSpec(ITexture.Type.STENCIL8, 1, true, 0, 0, 0)
        fun multisample(format: ITexture.Type, samples: Int): RenderbufferSpec = RenderbufferSpec(format, samples, true, 0, 0, 0)
        fun external(format: ITexture.Type, existingId: Int, width: Int, height: Int, samples: Int): RenderbufferSpec = RenderbufferSpec(format, samples, false, existingId, width, height);
    }
}
