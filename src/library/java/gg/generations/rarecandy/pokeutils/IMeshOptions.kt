package gg.generations.rarecandy.pokeutils

import gg.generations.rarecandy.codec.*

data class IMeshOptions(
    val invert: Boolean,
    val aliases: MutableList<String>) {

    companion object {
        val CODEC: Codec<IMeshOptions> = codec({ options ->
            obj().also {
                it.putIfNotEquals("invert", BOOL, { !it }, options.invert)
                it.putIfNotEquals("aliases", STRING_LIST, { it.isEmpty() }, options.aliases)
            }
        }, { it ->
            it.obj().let {
                IMeshOptions(
                    it.read("invert", BOOL, false),
                    it.read("aliases", STRING_LIST, mutableListOf())
                )
            }
        })

        val DEFAULT: IMeshOptions = IMeshOptions(false, mutableListOf())
    }
}
