package gg.generations.rarecandy.renderer.rendering

class StateManager(
    blendEnable: () -> Unit,
    blendDisable: () -> Unit,
    cullEnable: () -> Unit,
    cullDisable: () -> Unit,
    depthEnable: () -> Unit,
    depthDisable: () -> Unit
) {
    private val blend: StateToggle = StateToggle(blendEnable, blendDisable)
    private val cull: StateToggle = StateToggle(cullEnable, cullDisable)
    private val depth: StateToggle = StateToggle(depthEnable, depthDisable)

    fun toggle(isBlend: Boolean, isCull: Boolean, isDepth: Boolean) {
        blend.toggle(isBlend)
        cull.toggle(isCull)
        depth.toggle(isDepth)
    }

    fun toggle(stage: RenderStage) = toggle(stage.isBlend, stage.isCull, stage.isDepthTest)

    fun reset() = toggle(isBlend = false, isCull = false, isDepth = false)
}
