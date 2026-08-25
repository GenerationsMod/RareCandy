package gg.generations.rarecandy.tools

import java.util.function.Consumer

data class Command(val name: String, val description: String, val consumer: Consumer<Array<String>>)
