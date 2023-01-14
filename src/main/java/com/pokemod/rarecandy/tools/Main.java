package com.pokemod.rarecandy.tools;

import com.pokemod.pokeutils.PixelConverter;
import com.pokemod.pokeutils.PixelmonArchiveBuilder;
import com.pokemod.pokeutils.gui.PokeUtilsGui;
import com.pokemod.rarecandy.tools.tester.FeatureTester;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;

public class Main {
    private static Map<String, Consumer<String[]>> ARGUMENTS = Map.of(
            "archiveExplorer", Main::archiveExplorer,
            "converter", Main::converter,
            "pixelmonArchiveBuilder", Main::pixelmonArchiveBuilder,
            "modelTester", Main::modelTester
    );

    private static void pixelmonArchiveBuilder(String[] args) {
        try {
            PixelmonArchiveBuilder.main(args);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void modelTester(String[] args) {
        FeatureTester.main(args);
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
        if (args.length == 0) {
            System.out.println("Please specify which tool you want to use. Options: " + ARGUMENTS.keySet());
        } else {
            if (!ARGUMENTS.containsKey(args[0])) System.err.println("No option for " + args[0]);
            else ARGUMENTS.get(args[0]).accept(Arrays.copyOfRange(args, 1, args.length));
        }
    }
}
