package gg.generations.rarecandy.renderer.rendering

class StateToggle(private val enable: () -> Unit, private val disable: () -> Unit, private var state : Boolean = false) {

    fun toggle(newState: Boolean) {
        if (newState && !state) {
            state = true
            enable.invoke()
        } else if (!newState && state) {
            state = false
            disable.invoke()
        }
    }
}
