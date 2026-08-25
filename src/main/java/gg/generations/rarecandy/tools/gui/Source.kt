package gg.generations.rarecandy.tools.gui

interface Source {
    /** A colour attachment of the G-buffer.  */
    @JvmRecord
    data class Attachment(@JvmField val index: Int) : Source

    /** The G-buffer's depth texture.  */
    enum class Depth : Source {
        INSTANCE
    }

    /** Output of the previous enabled pass, or the G-buffer's albedo if this is the first.  */
    enum class Previous : Source { INSTANCE }

    /** A texture from the texture loader, by key.  */
    @JvmRecord data class Named(@JvmField val key: String?) : Source

    /** Output of an earlier pass that declared `writesTo(key)`.  */
    @JvmRecord data class Pass(@JvmField val key: String?) : Source

    companion object {
        @JvmStatic fun attachment(index: Int): Source = Attachment(index)
        @JvmStatic fun depth(): Source = Depth.INSTANCE
        fun previous(): Source = Previous.INSTANCE
        @JvmStatic fun named(key: String?): Source = Named(key)
        fun pass(key: String?): Source = Pass(key)
    }
}