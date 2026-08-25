package gg.generations.rarecandy.pokeutils

import gg.generations.rarecandy.codec.BOOL
import gg.generations.rarecandy.codec.STRING_LIST
import gg.generations.rarecandy.codec.codec
import gg.generations.rarecandy.codec.obj
import gg.generations.rarecandy.codec.putIfNotEquals
import gg.generations.rarecandy.codec.read

data class IHideDuringAnimation(
    val blackList: Boolean,
    val animations: MutableList<String>
) {



    fun check(animation: String?): Boolean {
        return animations.contains(animation) != blackList
    }

    companion object {
        val CODEC = codec({ hide ->
            obj().also {
                it.putIfNotEquals("blackList", BOOL, { !it }, hide.blackList)
                it.putIfNotEquals("animations", STRING_LIST, { it.isEmpty() }, hide.animations)
            }
        }, {
            it.obj().let {
                IHideDuringAnimation(
                    it.read("blackList", BOOL, false),
                    it.read("animations", STRING_LIST, mutableListOf())
                )
            }
        })

        val NONE: IHideDuringAnimation = IHideDuringAnimation(false, mutableListOf())
    }
}
