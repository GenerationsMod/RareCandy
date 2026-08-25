package gg.generations.rarecandy.tools

import gg.generations.rarecandy.renderer.LoggerUtil
import gg.generations.rarecandy.tools.gui.PokeUtilsGui
import gg.generations.rarecandy.tools.pkcreator.PixelConverter
import gg.generations.rarecandy.tools.pkcreator.PixelmonArchiveBuilder
import java.io.IOException

object Main {
    private fun MutableList<Command>.command(name: String, desc: String, function: (Array<String>) -> Unit) = add(Command(name, desc, function))

    val ARGUMENTS = buildList<Command> {
        command("archiveExplorer", "Opens a AWT Gui allowing you to view the structure of a PK file", PokeUtilsGui::main)
        command("atlasBuilder", "Load a PK or folder and visually arrange textures into atlas pages", AtlasBuilderGui::main)
        command("converter", "Lets you convert individual files inside of converter/in into their opposite. eg: pk -> glb, glb -> pk, smd -> smdx, etc", PixelConverter::main)
        command("pixelmonArchiveBuilder", "Allows you to build an entire pokemon. each pokemon should match their folder name eg: converter/in/koraidon should have a glb called koraidon.glb and all the anims", PixelmonArchiveBuilder::main)
        command("specular_generator", "Generates Specular maps form image", SpecularGenerator::main)
    }

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            CommandGui(ARGUMENTS, args).launch()
        } catch (e: Exception) {
            LoggerUtil.printError(e)
        }
    }
}

