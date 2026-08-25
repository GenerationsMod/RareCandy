package gg.generations.rarecandy.renderer.model.material

import gg.generations.rarecandy.codec.*
import gg.generations.rarecandy.renderer.storage.SSBOBuffer

data class IMaterialValues(
    var baseColor1: Color?,
    var baseColor2: Color?,
    var baseColor3: Color?,
    var baseColor4: Color?,
    var baseColor5: Color?,
    var emiColor1: Color?,
    var emiColor2: Color?,
    var emiColor3: Color?,
    var emiColor4: Color?,
    var emiColor5: Color?,
    var emiIntensity1: Float?,
    var emiIntensity2: Float?,
    var emiIntensity3: Float?,
    var emiIntensity4: Float?,
    var emiIntensity5: Float?,
    var useLight: Boolean?,
    var disableDepth: Boolean?
) {

    constructor(): this(
        null, null, null, null, null,
        null, null, null, null, null,
        null, null, null, null, null,
        null, null)

    fun put(pointer: SSBOBuffer) {
        pointer.put(baseColor1 ?: Color.WHITE).put(0f)
        pointer.put(baseColor2 ?: Color.WHITE).put(0f)
        pointer.put(baseColor3 ?: Color.WHITE).put(0f)
        pointer.put(baseColor4 ?: Color.WHITE).put(0f)
        pointer.put(baseColor5 ?: Color.WHITE).put(0f)

        pointer.put(emiColor1 ?: Color.BLACK).put(0f)
        pointer.put(emiColor2 ?: Color.BLACK).put(0f)
        pointer.put(emiColor3 ?: Color.BLACK).put(0f)
        pointer.put(emiColor4 ?: Color.BLACK).put(0f)
        pointer.put(emiColor5 ?: Color.WHITE).put(0f)

        pointer.put(emiIntensity1 ?: 0.0f)
        pointer.put(emiIntensity2 ?: 0.0f)
        pointer.put(emiIntensity3 ?: 0.0f)
        pointer.put(emiIntensity4 ?: 0.0f)
        pointer.put(emiIntensity5 ?: 1.0f)
        pointer.put(useLight ?: true)
    }

    private fun SSBOBuffer.put(color: Color): SSBOBuffer = this.put(color.r).put(color.g).put(color.b)

    val isDefault: Boolean
        get() = this.baseColor1 == Color.WHITE &&
                this.baseColor2 == Color.WHITE &&
                this.baseColor3 == Color.WHITE &&
                this.baseColor4 == Color.WHITE &&
                this.baseColor5 == Color.WHITE &&
                this.emiColor1 == Color.BLACK &&
                this.emiColor2 == Color.BLACK &&
                this.emiColor3 == Color.BLACK &&
                this.emiColor4 == Color.BLACK &&
                this.emiColor5 == Color.WHITE &&
                this.emiIntensity1 == 0f &&
                this.emiIntensity2 == 0f &&
                this.emiIntensity3 == 0f &&
                this.emiIntensity4 == 0f &&
                this.emiIntensity5 == 1f &&
                this.useLight == true &&
                this.disableDepth == false

    data class Color(var r: Float, var g: Float, var b: Float) {
        override fun toString(): String {
            val r = Math.clamp((r * 255).toInt().toLong(), 0, 255)
            val g = Math.clamp((g * 255).toInt().toLong(), 0, 255)
            val b = Math.clamp((b * 255).toInt().toLong(), 0, 255)
            return String.format("#%02X%02X%02X", r, g, b)
        }

        companion object {
            val WHITE = Color(1f, 1f, 1f)
            val BLACK = Color(0f, 0f, 0f)

            fun fromString(color: String): Color {
                val rgb = color.removePrefix("#").toInt(16)
                val r = (rgb shr 16 and 0xFF) / 255f
                val g = (rgb shr 8 and 0xFF) / 255f
                val b = (rgb and 0xFF) / 255f

                return Color(r, g, b)
            }
        }
    }

    companion object {
        val COLOR = STRING.xmap(Color::fromString, Color::toString).readAlso {
            var array = arr(3)

            val r = array[0].asFloat
            val g = array[1].asFloat
            val b = array[2].asFloat

            Color(r, g, b)
        }

        val CODEC = codec<IMaterialValues>({ values ->
            obj().also {
                it.putIfNotEquals("baseColor1", COLOR, { it == Color.WHITE }, values.baseColor1)
                it.putIfNotEquals("baseColor2", COLOR, { it == Color.WHITE }, values.baseColor2)
                it.putIfNotEquals("baseColor3", COLOR, { it == Color.WHITE }, values.baseColor3)
                it.putIfNotEquals("baseColor4", COLOR, { it == Color.WHITE }, values.baseColor4)
                it.putIfNotEquals("baseColor5", COLOR, { it == Color.WHITE }, values.baseColor5)
                it.putIfNotEquals("emiColor1", COLOR, { it == Color.BLACK }, values.emiColor1)
                it.putIfNotEquals("emiColor2", COLOR, { it == Color.BLACK }, values.emiColor2)
                it.putIfNotEquals("emiColor3", COLOR, { it == Color.BLACK }, values.emiColor3)
                it.putIfNotEquals("emiColor4", COLOR, { it == Color.BLACK }, values.emiColor4)
                it.putIfNotEquals("emiColor5", COLOR, { it == Color.WHITE }, values.emiColor5)
                it.putIfNotEquals("emiIntensity1", FLOAT, { it == 0f }, values.emiIntensity1)
                it.putIfNotEquals("emiIntensity2", FLOAT, { it == 0f }, values.emiIntensity2)
                it.putIfNotEquals("emiIntensity3", FLOAT, { it == 0f }, values.emiIntensity3)
                it.putIfNotEquals("emiIntensity4", FLOAT, { it == 0f }, values.emiIntensity4)
                it.putIfNotEquals("emiIntensity5", FLOAT, { it == 1f }, values.emiIntensity5)
                it.putIfNotEquals("useLight", BOOL, { it }, values.useLight)
                it.putIfNotEquals("disableDepth", BOOL, { !it }, values.disableDepth)
            }
        }, {
            it.obj().let {
                IMaterialValues(
                it.readOrNull("baseColor1", COLOR),
                it.readOrNull("baseColor2", COLOR),
                it.readOrNull("baseColor3", COLOR),
                it.readOrNull("baseColor4", COLOR),
                it.readOrNull("baseColor5", COLOR),
                it.readOrNull("emiColor1", COLOR),
                it.readOrNull("emiColor2", COLOR),
                it.readOrNull("emiColor3", COLOR),
                it.readOrNull("emiColor4", COLOR),
                it.readOrNull("emiColor5", COLOR),
                it.readOrNull("emiIntensity1", FLOAT),
                it.readOrNull("emiIntensity2", FLOAT),
                it.readOrNull("emiIntensity3", FLOAT),
                it.readOrNull("emiIntensity4", FLOAT),
                it.readOrNull("emiIntensity5", FLOAT),
                it.readOrNull("useLight", BOOL),
                it.readOrNull("disableDepth", BOOL)
                )
            }
        })

        @JvmField
        val DEFAULT = IMaterialValues(
            Color.WHITE,
            Color.WHITE,
            Color.WHITE,
            Color.WHITE,
            Color.WHITE,
            Color.BLACK,
            Color.BLACK,
            Color.BLACK,
            Color.BLACK,
            Color.WHITE,
            0.0f,
            0.0f,
            0.0f,
            0.0f,
            1.0f,
            useLight = true,
            disableDepth = false,
        )
    }
}
