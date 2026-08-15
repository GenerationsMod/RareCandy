package gg.generations.rarecandy.tools;

import gg.generations.experimental.ExperimentalViewer;
import gg.generations.rarecandy.tools.gui.DialogueUtils;
import gg.generations.rarecandy.tools.gui.PokeUtilsGui;
//import gg.generations.rarecandy.tools.pkcreator.Convert;
import gg.generations.rarecandy.tools.pkcreator.PixelConverter;
import gg.generations.rarecandy.tools.pkcreator.PixelmonArchiveBuilder;
import gg.generations.rarecandy.tools.pokemodding.QuaternionConverterGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static gg.generations.rarecandy.renderer.LoggerUtil.printError;

public class Main {
    public static final List<Command> ARGUMENTS = List.of(
            new Command("archiveExplorer", "Opens a AWT Gui allowing you to view the structure of a PK file", Main::archiveExplorer),
            new Command("atlasBuilder", "Load a PK or folder and visually arrange textures into atlas pages", Main::atlasBuilder),
            new Command("experimentalViewer", "Just an entry for testing the new viewer.", ExperimentalViewer::main),
            new Command("converter", "Lets you convert individual files inside of converter/in into their opposite. eg: pk -> glb, glb -> pk, smd -> smdx, etc", Main::converter),
            new Command("pixelmonArchiveBuilder", "Allows you to build an entire pokemon. each pokemon should match their folder name eg: converter/in/koraidon should have a glb called koraidon.glb and all the anims", Main::pixelmonArchiveBuilder),
            new Command("specular_generator", "Generates Specular maps form image", SpecularGenerator::main)
            );

    private static void gfbanmConvert(String[] args) {
        try {
            GfbanmConvert.main(args);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void pixelmonArchiveBuilder(String[] args) {
        try {
            PixelmonArchiveBuilder.main(args);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void converter(String[] args) {
        try {
            PixelConverter.main(args);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void archiveExplorer(String[] args) {
        try {
            PokeUtilsGui.main(args);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void atlasBuilder(String[] args) {
        try {
            Class.forName("AtlasBuilderGui")
                    .getMethod("main", String[].class)
                    .invoke(null, (Object) args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        try {
            new CommandGUI(Main.ARGUMENTS, args);
        } catch (Exception e) {
            printError(e);
        }
    }
}

