package gg.generations.rarecandy.tools;

import gg.generations.rarecandy.pokeutils.Pair;
import gg.generations.rarecandy.pokeutils.PixelAsset;
import gg.generations.rarecandy.tools.pokemodding.AnimationReadout;
import gg.generations.rarecandy.tools.gui.DialogueUtils;
import gg.generations.rarecandy.tools.gui.PokeUtilsGui;
import gg.generations.rarecandy.tools.pixelmonTester.MinecraftSimulator;
import gg.generations.rarecandy.tools.pkcreator.Convert;
import gg.generations.rarecandy.tools.pkcreator.PixelConverter;
import gg.generations.rarecandy.tools.pkcreator.PixelmonArchiveBuilder;
import gg.generations.rarecandy.tools.pokemodding.QuaternionConverterGUI;
import gg.generations.rarecandy.tools.swsh.EyeTexture;
import gg.generations.rarecandy.tools.swsh.LongBoi;
import gg.generations.rarecandy.tools.swsh.MouthTexture;
import org.lwjgl.util.nfd.NativeFileDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static gg.generations.rarecandy.renderer.LoggerUtil.printError;

public class Main {
    public static final List<Command> ARGUMENTS = List.of(
            new Command("archiveExplorer", "Opens a AWT Gui allowing you to view the structure of a PK file", Main::archiveExplorer),
            new Command("converter", "Lets you convert individual files inside of converter/in into their opposite. eg: pk -> glb, glb -> pk, smd -> smdx, etc", Main::converter),
            new Command("pixelmonArchiveBuilder", "Allows you to build an entire pokemon. each pokemon should match their folder name eg: converter/in/koraidon should have a glb called koraidon.glb and all the anims", Main::pixelmonArchiveBuilder),
            new Command("eyeFixer (swsh)", "Used to convert all pairs of iris and eye textures in a folder into the format used in Sword and Shield pokemon model eyes", Main::eyeFixer),
            new Command("mouthFixer (swsh)", "Used to convert mouth textures in a folder into the format used in Sword and Shield pokemon model mouth", Main::mouthFixer),
            new Command("longBoi (swsh)", "Used to convert all selected non eye related textures into a long boi (mirrored version of itself) that makes setting up Sword and Shield pokemon models easier the format used in Sword and Shield pokemon model eyes.", Main::longBoi),
            new Command("Glb Convert", "Experimental converter for a full glb into the new config.json based form.", Main::glbConverter),
            new Command("Model Viewer", "Simplified viewer for opening and reviewing models before packaging", Main::modelViewer),
            new Command("GFBANM/TRACM converter", "Converts gfbanm and tracms to json and back.", Main::gfbanmConvert)
            );

    private static void gfbanmConvert(String[] args) {
        try {
            GfbanmConvert.main(args);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void modelViewer(String[] strings) {
        NativeFileDialog.NFD_Init();

        new ModelViewer(pairConsumer -> ModelViewer.createFrame("Load pk or folder?", "PK", () -> {
            var path = DialogueUtils.chooseFile("PK;pk");

            if (path != null) {
                try (var is = Files.newInputStream(path)) {
                    return new Pair<>(path, new PixelAsset(is, path.getFileName().toString()));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                return null;
            }
            }, "Folder", () -> {
            var path = DialogueUtils.chooseFolder();

                return path != null ? new Pair<>(path, new PixelAsset(path)) : null;
            }, pairConsumer), true);

    }

    private static void smdToGfbanm(String[] strings) {
        try {
            AnimationReadout.main(strings);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void quaterionSwizzleTest(String[] strings) {
        QuaternionConverterGUI.main(strings);
    }


    private static void gfbanmreadout(String[] args) {
        try {
            AnimationReadout.gfbanmPrintOut(args);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static DualOutputStream outStream;

    private static void mouthFixer(String[] strings) {
        try {
            MouthTexture.main(strings);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void eyeFixer(String[] args) {
        try {
            EyeTexture.main(args);
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

    private static void longBoi(String[] args) {
        try {
            LongBoi.main(args);
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

    private static void regimented(String[] args) {
        try {
            Convert.main(args);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void glbConverter(String[] args) {
        try {
            GlbReader.main(args);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void archiveExplorer(String[] args) {
        PokeUtilsGui.main(args);
    }

    public static void main(String[] args) throws IOException {
        try {
            new CommandGUI(Main.ARGUMENTS, args);
//        if (args.length == 0 || args[0].equals("help")) {
//            System.err.println("Please specify which tool you want to use. Options: ");
//            ARGUMENTS.forEach(command -> Main.print(command.name() + " - " + command.description()));
//        } else {
//            var command = ARGUMENTS.stream()
//                    .filter(cmd -> cmd.name().equals(args[0]))
//                    .findAny();
//
//            if (command.isEmpty()) System.err.println("No command with the name \"" + args[0] + "\"");
//            else command.get().consumer().accept(Arrays.copyOfRange(args, 1, args.length));
//        }
        } catch (Exception e) {
            printError(e);
        }
    }
}
