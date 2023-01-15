package com.pokemod.rarecandy.tools;

import com.pokemod.rarecandy.tools.gui.PokeUtilsGui;
import com.pokemod.rarecandy.tools.pixelmonTester.MinecraftSimulator;
import com.pokemod.rarecandy.tools.pkcreator.PixelConverter;
import com.pokemod.rarecandy.tools.pkcreator.PixelmonArchiveBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Main {
    private static final List<Command> ARGUMENTS = List.of(
            new Command("archiveExplorer", "Opens a AWT Gui allowing you to view the structure of a PK file", Main::archiveExplorer),
            new Command("converter", "Lets you convert individual files inside of converter/in into their opposite. eg: pk -> glb, glb -> pk, smd -> smdx, etc", Main::converter),
            new Command("pixelmonArchiveBuilder", "Allows you to build an entire pokemon. each pokemon should match their folder name eg: converter/in/koraidon should have a glb called koraidon.glb and all the anims", Main::pixelmonArchiveBuilder),
            new Command("modelTester", "Lets you view what a model will probably look like in game. All you need to do is supply the folder with the glb file inside being named the same as the folder name and anims in the same folder", Main::modelTester)
    );

    private static void pixelmonArchiveBuilder(String[] args) {
        try {
            PixelmonArchiveBuilder.main(args);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void modelTester(String[] args) {
        MinecraftSimulator.main(args);
    }

    private static void converter(String[] args) {
        try {
            PixelConverter.main(args);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void archiveExplorer(String[] args) {
        PokeUtilsGui.main(args);
    }

    public static void main(String[] args) {
        if (args.length == 0 || args[0].equals("help")) {
            System.err.println("Please specify which tool you want to use. Options: ");
            ARGUMENTS.forEach(command -> System.out.println(command.name() + " - " + command.description()));
        } else {
            var command = ARGUMENTS.stream()
                    .filter(cmd -> cmd.name().equals(args[0]))
                    .findAny();

            if (command.isEmpty()) System.err.println("No command with the name \"" + args[0] + "\"");
            else command.get().consumer().accept(Arrays.copyOfRange(args, 1, args.length));
        }
    }
}
