package gg.generations.rarecandy.tools.pkcreator

import dev.thecodewarrior.binarysmd.formats.SMDBinaryReader
import dev.thecodewarrior.binarysmd.formats.SMDTextWriter
import gg.generations.rarecandy.pokeutils.resource.ResourceLocator
import gg.generations.rarecandy.renderer.LoggerUtil
import org.msgpack.core.MessagePack
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Utility for writing and reading Pixelmon: Generation's model format.
 */
object PixelConverter {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val inFolder = Paths.get("converter/in")
        val outFolder = Paths.get("converter/out")

        Files.createDirectories(inFolder)
        Files.createDirectories(outFolder)


        Files.walk(inFolder).forEach { path: Path? ->
            val relativePath = inFolder.relativize(path)
            var outputPath = outFolder.resolve(relativePath).getParent()
            if (path != inFolder) {
                if (!Files.isDirectory(path)) {
                    if (path.toString().endsWith("smdx")) {
                        outputPath = outputPath.resolve(path!!.getFileName().toString().replace(".smdx", ".smd"))
                        PixelConverter.convertToSmd(path, outputPath)
                    } else if (path.toString().endsWith(".pk")) {
                        outputPath = outputPath.resolve(path!!.getFileName().toString().replace(".pk", ""))
                        unpackPk(path, outputPath)
                    }
                } else {
                    outputPath = outputPath.resolve(path!!.getFileName().toString() + ".pk")

                    unpackPk(path, outputPath)
                }
            }
        }
    }

    fun unpackPk(path: Path?, outputPath: Path?) {
        try {
            val input = ResourceLocator.of(path)
            val output = ResourceLocator.of(outputPath)

            for (file in input.getFileNames()) {
                output.putFile(file, input.getFile(file))
            }

            output.save()
        } catch (e: Exception) {
            println("Issue: " + path)
            e.printStackTrace()
        }
    }

    private fun convertToSmd(path: Path, outputPath: Path) {
        try {
            Files.writeString(
                outputPath, SMDTextWriter().write(
                    SMDBinaryReader().read(
                        MessagePack.newDefaultUnpacker(
                            Files.newInputStream(path)
                        )
                    )
                )
            )
        } catch (e: IOException) {
            LoggerUtil.printError(e)
        }
    }
}