package gg.generations.rarecandy.tools.gui

import gg.generations.rarecandy.renderer.animation.Animation
import gg.generations.rarecandy.tools.gui.ModelTracker.model
import imgui.ImGui
import kotlin.math.max

object AnimationState {
    var seconds: Double = 0.0
    private var lastTime: Double = Double.NaN
    private var paused: Boolean = false
    private var reversed: Boolean = false
    var animationName: String? = null
    private var animationId: Int = -1
    var animationNames: Array<String>? = null

    fun update(time: Double) {
        val prev = lastTime
        lastTime = time
        if (prev.isNaN() || paused) return

        val delta = time - prev
        if (delta <= 0.0) return

        seconds = wrap(animation, seconds + if (reversed) -delta else delta)
    }

    val animation: Animation?
        get() {
            if (model == null || animationId == -1) return null
            val id = animationId
            return if (id >= 0 && id < (model?.animations?.size ?: 0)) model?.animations?.get(id) else null
        }

    fun setAnimation(animation: String?) {
        var id = model?.animationNameToId?.getOrDefault(animation, -1)

        animationId = model?.animationNameToId?.getOrDefault(animation, -1) ?: -1
        if (animationId == -1) animationName = "none"
        else animationName = animation!!
    }


    private var progress: Float = 0f


    fun render() {


        ImGui.begin("Animation Playback")


        val animation = animation ?: run {
            ImGui.text("No animation loaded")
            ImGui.end()
            return
        }

        animationNames?.render("Animation", animationId) {
            setAnimation(it)
        }

        val duration: Double = animation.durationSeconds
        progress = if (duration > 0.0) (seconds / duration).toFloat() else 0.0f

        ImGui.text(String.format("%.3f / %.3f s", seconds, duration))

        progress.render("Progress", 0f, 1f) {
            progress = it
            seconds = wrap(animation, duration * progress)
            paused = true
        }

        if (ImGui.button(if (paused) "Play" else "Pause")) togglePause()
        ImGui.sameLine()

        if (ImGui.button("Rewind")) rewind()
        ImGui.sameLine()
        if (ImGui.button(if (reversed) "Forward" else "Reverse")) {
            toggleReverse()
        }
        ImGui.sameLine()
        if (ImGui.button("Reset")) {
            seconds = 0.0
            paused = false
            reversed = false
        }

        ImGui.end()

        animationNames?.also {
            begin("Animations") {
                it.forEach {
                    tree(it) {
                        true.render("Enabled") {}
                    }
                }
            }
        }
    }

    fun toggleReverse() {
        reversed = !reversed
    }

    fun rewind() {
        seconds = 0.0
    }

    fun togglePause() {
        paused = !paused
    }
}

    private val Animation?.durationSeconds: Double
        get() {
            if (this == null || this.animationDuration <= 0.0 || this.ticksPerSecond == 0.0f) {
                return 0.0
            }

            return this.animationDuration / this.ticksPerSecond
        }

    private fun wrap(animation: Animation?, seconds: Double): Double {
        val duration: Double = animation.durationSeconds
        if (duration <= 0.0) {
            return max(0.0, seconds)
        }

        val wrapped = seconds % duration
        return if (wrapped < 0.0) wrapped + duration else wrapped
    }
