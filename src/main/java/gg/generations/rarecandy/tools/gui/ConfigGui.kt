package gg.generations.rarecandy.tools.gui

import gg.generations.rarecandy.pokeutils.BlendType
import gg.generations.rarecandy.pokeutils.CullType
import gg.generations.rarecandy.pokeutils.IMaterialReference
import gg.generations.rarecandy.pokeutils.IVariantDetails
import gg.generations.rarecandy.pokeutils.IVariantParent
import gg.generations.rarecandy.renderer.animation.ITransform
import gg.generations.rarecandy.renderer.animation.ITransformSet
import gg.generations.rarecandy.renderer.loading.Names
import gg.generations.rarecandy.renderer.model.material.IMaterialImages
import gg.generations.rarecandy.renderer.model.material.IMaterialValues
import imgui.ImGui
import imgui.type.ImFloat
import imgui.type.ImInt
import imgui.type.ImString
import org.joml.Vector2f
import org.joml.Vector4f

internal val colorArray = FloatArray(3)
internal val singleFloat = ImFloat()

fun Float.render(name: String, min: Float, max: Float, function: (Float) -> Unit): Boolean {
    singleFloat.set(this)

    ImGui.text(name)

    sameLine()

    return ImGui.sliderFloat("##$name", singleFloat.data, min, max).ifTrue { function.invoke(singleFloat.get()) }
}

fun Float.render(name: String, function: (Float) -> Unit): Boolean {
    singleFloat.set(this)

    ImGui.text(name)

    sameLine()

    return ImGui.inputFloat("##$name", singleFloat).ifTrue { function.invoke(singleFloat.get()) }
}


fun Boolean.render(name: String, function: (Boolean) -> Unit): Boolean = ImGui.checkbox(name, this).ifTrue { function.invoke(!this) }

fun IMaterialValues.Color.render(name: String): Boolean {
    colorArray[0] = r
    colorArray[1] = g
    colorArray[2] = b

    return ImGui.colorEdit3(name, colorArray).ifTrue {
        this.r = colorArray[0]
        this.g = colorArray[1]
        this.b = colorArray[2]
    }
}

fun Boolean.ifTrue(function: () -> Unit): Boolean = this.also { if (it) function.invoke() }

private fun addValue(name: String, function: () -> Unit): Boolean {
    ImGui.alignTextToFramePadding()
    ImGui.text(name)
    ImGui.sameLine()
    return ImGui.button("+").ifTrue { function.invoke() }
}

data class MapState<T>(
    var selected: Int,
    val supplier: () -> T
)

fun Array<String>.render(name: String, selected: Int, block: (String) -> Unit): Boolean {
    intValue.set(selected)

    ImGui.text(name)

    sameLine()

    return ImGui.combo("##$name", intValue, this).ifTrue { block.invoke(this[intValue.get()]) }
}

fun sameLine() {
    ImGui.sameLine()
    ImGui.setNextItemWidth(-1f)
}

private fun Array<String>.render(name: String, state: MapState<*>): Boolean {
    intValue.set(state.selected)
    return ImGui.combo(name, intValue, this).ifTrue { state.selected = intValue.get() }
}

private fun <T> addValue(state: MapState<T>, options: Array<String>, block: (String) -> Unit): Boolean {
    var dirty = false

    ImGui.alignTextToFramePadding()
    dirty = dirty or options.render("", state)
    ImGui.sameLine()
    dirty = dirty or ImGui.button("+").ifTrue { block.invoke(options[state.selected]) }

    return true
}

fun IMaterialValues.render(): Boolean {
    var dirty = false
    dirty = dirty or (baseColor1?.render("Base Color 1") ?: addValue("Base Color 1") { baseColor1 = IMaterialValues.Color.WHITE })
    dirty = dirty or (baseColor2?.render("Base Color 2") ?: addValue("Base Color 2") { baseColor2 = IMaterialValues.Color.WHITE })
    dirty = dirty or (baseColor3?.render("Base Color 3") ?: addValue("Base Color 3") { baseColor3 = IMaterialValues.Color.WHITE })
    dirty = dirty or (baseColor4?.render("Base Color 4") ?: addValue("Base Color 4") { baseColor4 = IMaterialValues.Color.WHITE })
    dirty = dirty or (baseColor5?.render("Base Color 5") ?: addValue("Base Color 5") { baseColor5 = IMaterialValues.Color.WHITE })
    dirty = dirty or (emiColor1?.render("Emission Color 1") ?: addValue("Emission Color 1") { emiColor1 = IMaterialValues.Color.BLACK  })
    dirty = dirty or (emiColor2?.render("Emission Color 2") ?: addValue("Emission Color 2") { emiColor2 = IMaterialValues.Color.BLACK })
    dirty = dirty or (emiColor3?.render("Emission Color 3") ?: addValue("Emission Color 3") { emiColor3 = IMaterialValues.Color.BLACK })
    dirty = dirty or (emiColor4?.render("Emission Color 4") ?: addValue("Emission Color 4") { emiColor4 = IMaterialValues.Color.BLACK })
    dirty = dirty or (emiColor5?.render("Emission Color 5") ?: addValue("Emission Color 5") { emiColor5 = IMaterialValues.Color.WHITE })
    dirty = dirty or (emiIntensity1?.render("Emission Intensity 1", 0f, 1f) { emiIntensity1 = it } ?: addValue("Emission Intensity ") { emiIntensity1 = 0f })
    dirty = dirty or (emiIntensity2?.render("Emission Intensity 2", 0f, 1f) { emiIntensity2 = it } ?: addValue("Emission Intensity ") { emiIntensity2 = 0f })
    dirty = dirty or (emiIntensity3?.render("Emission Intensity 3", 0f, 1f) { emiIntensity3 = it } ?: addValue("Emission Intensity ") { emiIntensity3 = 0f })
    dirty = dirty or (emiIntensity4?.render("Emission Intensity 4", 0f, 1f) { emiIntensity4 = it } ?: addValue("Emission Intensity ") { emiIntensity4 = 0f })
    dirty = dirty or (emiIntensity5?.render("Emission Intensity 5", 0f, 1f) { emiIntensity5 = it } ?: addValue("Emission Intensity ") { emiIntensity5 = 1f })
    dirty = dirty or (useLight?.render("Use light") { useLight = it } ?: addValue("Use light") { useLight = true })
    dirty = dirty or (disableDepth?.render("Disable depth") { disableDepth = it } ?: addValue("Disable depth") { useLight = false })

    return dirty
}

fun tree(name: String, block: () -> Unit) {
    ImGui.treeNode(name).ifTrue {
        block.invoke()
        ImGui.treePop()
    }
}

fun <T> MutableMap<String, T>.render(name: String, state: MapState<T>, block: (String, T) -> Boolean): Boolean {
    return render(name, null, state, block)
}

fun <T> MutableMap<String, T>.render(name: String, list: Set<String>?, state: MapState<T>, block: (String, T) -> Boolean): Boolean {
    var dirty = false


    tree("$name (${this.values.size})") {
        val available = list?.toMutableSet()

        this.forEach { (key, value) ->
            tree(key) {
                dirty = dirty or block.invoke(key, value)
            }

            available?.also { it -= key }
        }

        available?.takeIf { it.isNotEmpty() }?.also { addValue(state, it.toTypedArray()) { this } }
    }

    return dirty
}


private val shaderTypes = arrayOf("solid", "layered", "masked")

fun IMaterialReference.render(name: String, names: Names): Boolean {
    var dirty = false
    dirty = dirty or (this.parent?.renderDropDown("Parent", names.materials) { parent = it } ?: addValue("Parent") { parent = names.materials.first { it != name } })
    dirty = dirty or (this.shader?.renderDropDown("Shader", shaderTypes) { shader = it } ?: addValue("Shader") { shader = "solid" })
    dirty = dirty or (this.cull?.render("Cull") { this.cull = it } ?: addValue("Cull") { cull = CullType.None })
    dirty = dirty or (this.blend?.render("Blend") { this.blend = it } ?: addValue("Blend") { blend = BlendType.None })
    dirty = dirty or (this.images?.render("Images", names) { this.images = it } ?: addValue("Blend") { images =
        IMaterialImages()
    })
    return dirty
}

fun IMaterialImages.render(name: String, names: Names, block: (IMaterialImages) -> Unit): Boolean {
    var dirty = false;

    tree(name) {
        dirty = dirty or (this.diffuse?.renderDropDown("Diffuse", names.images) { this.diffuse = it } ?: false)
    }

    return dirty
}

fun IVariantDetails.render(name: String, names: Names): Boolean {
    var dirty = false

    dirty = dirty or (this.material?.renderDropDown("Material", names.materials) { material = it} ?: addValue("Material") { material = names.materials.first { it != name } })
    dirty = dirty or (this.effect?.render("Effect") { effect = it} ?: addValue("Effect") { effect = "none" })
    dirty = dirty or (this.paradox?.render("Paradox") { paradox = it} ?: addValue("Paradox") { paradox = false })
    dirty = dirty or (this.transform?.render("Transform") ?: addValue("Transform") { transform = ITransformSet() })

    return dirty
}

fun IVariantParent.render(name: String, names: Names): Boolean {
    var dirty = false

    dirty = dirty or (this.parent?.renderDropDown("Parent", names.variants) { parent = it } ?: addValue("Parent") { parent = names.variants.first { it != name } })
//    dirty = dirty or (this.details.render(("Details", names.variants) { parent = it } ?: addValue("Parent") { parent = names.variants.first { it != name } })

    return dirty
}

fun ITransformSet.render(name: String): Boolean {
    var dirty = false

    tree(name) {
        dirty = dirty or (this.diffuse?.render("Diffuse") ?: addValue("Diffuse") { diffuse = ITransform() })
        dirty = dirty or (this.layer?.render("Layer") ?: addValue("Layer") { layer = ITransform() })
        dirty = dirty or (this.mask?.render("Mask") ?: addValue("Mask") { mask = ITransform() })
        dirty = dirty or (this.emission?.render("Emission") ?: addValue("Emission") { emission = ITransform() })
    }

    return dirty
}

fun ITransform.render(name: String): Boolean {
    var dirty = false

    tree(name) {
        dirty = dirty or scale.render("Scale")
        dirty = dirty or offset.render("Offset")
    }

    return dirty
}

private val vec2Array = FloatArray(2)

fun Vector2f.render(name: String): Boolean {
    vec2Array[0] = x
    vec2Array[1] = y

    return ImGui.inputFloat2(name, vec2Array).ifTrue { this.set(vec2Array[0], vec2Array[1]) }
}

private val vec4Array = FloatArray(4)

fun Vector4f.render(name: String): Boolean {
    vec4Array[0] = x
    vec4Array[1] = y
    vec4Array[2] = z
    vec4Array[3] = w

    return ImGui.inputFloat4(name, vec4Array).ifTrue { this.set(vec4Array[0], vec4Array[1], vec4Array[2], vec4Array[3]) }
}

private val stringValue = ImString()
private val intValue = ImInt()


fun String.renderDropDown(title: String, list: Array<String>, block: (String) -> Unit): Boolean {
    val selected = list.indexOf(this).takeUnless { it == -1 } ?: 0

    return list.render(title, selected, block)
}

inline fun <reified T: Enum<T>> T.render(name: String, crossinline block: (T) -> Unit): Boolean {
    return this.name.renderDropDown(name, enumValues<T>().map { it.name }.toTypedArray()) {
        block.invoke(enumValueOf(it))
    }
}

fun String.render(name: String, block: (String) -> Unit): Boolean {
    stringValue.set(this)

    return ImGui.inputText(name, stringValue).ifTrue { block.invoke(stringValue.get()) }
}