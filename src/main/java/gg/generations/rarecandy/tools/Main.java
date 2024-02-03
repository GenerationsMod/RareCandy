package gg.generations.rarecandy.tools;

import com.google.gson.GsonBuilder;
import de.javagl.jgltf.model.io.GltfModelReader;
import de.javagl.jgltf.model.io.GltfModelWriter;
import gg.generations.rarecandy.pokeutils.Pair;
import gg.generations.rarecandy.pokeutils.PixelAsset;
import gg.generations.rarecandy.pokeutils.tracm.*;
import gg.generations.rarecandy.renderer.animation.Animation;
import gg.generations.rarecandy.renderer.animation.GfbAnimation;
import gg.generations.rarecandy.renderer.animation.TransformStorage;
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
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
            new Command("Tranm Printer", "Simplified viewer for opening and reviewing models before packaging", Main::tranmPrinter)
            );

    private static void tranmPrinter(String[] strings) {
        NativeFileDialog.NFD_Init();

        var path = DialogueUtils.chooseFile("TRACM;tracm");

        if(path == null) {
            return;
        }

        try {
            var tracm = TRACM.getRootAsTRACM(ByteBuffer.wrap(Files.readAllBytes(path)));

//            IntStream.range(0, tracm.tracksLength()).mapToObj(tracm::tracks).forEach(track -> {
//                var string = new StringBuilder();
//                string.append(track.trackPath() + ":").append("\n");
//
//                if(track.materialAnimation() != null) {
//                    IntStream.range(0, track.materialAnimation().materialTrackLength()).mapToObj(b -> track.materialAnimation()).forEach(trackMaterialTimeline -> IntStream.range(0, trackMaterialTimeline.materialTrackLength()).mapToObj(trackMaterialTimeline::materialTrack).forEach(trackMaterial -> {
//                        string.append("\t").append(trackMaterial.name()).append("\n");
//
//                        IntStream.range(0, trackMaterial.animValuesLength()).mapToObj(b -> trackMaterial.animValues(b)).forEach(a -> {
//                            string.append("\t\t" + a.name() + "\n");
//
//                            string.append("\t\t\t" + a.name() + "\n");
//                        });
//                    }));
//                } else {
//                    string.append("\tNone\n");
//                }
                var offset = fillTrOffsets(tracm);

                System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(offset));
//            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static HashMap<String, Map<String, Animation.Offset>> fillTrOffsets(TRACM animationPair) {
        var offsets = new HashMap<String, Map<String, Animation.Offset>>();

        if(animationPair != null) {
            IntStream.range(0, animationPair.tracksLength()).mapToObj(animationPair::tracks).filter(a -> a.materialAnimation() != null).flatMap(a -> IntStream.range(0, a.materialAnimation().materialTrackLength()).mapToObj(b -> a.materialAnimation().materialTrack(b))).collect(Collectors.toMap(b -> b.name(), b -> IntStream.range(0, b.animValuesLength()).mapToObj(b::animValues).collect(Collectors.toMap(TrackMaterialAnim::name, c -> {
                return new GfbAnimation.GfbOffset(toStorage(c.list().blue()), toStorage(c.list().alpha()), toStorage(c.list().red()), toStorage(c.list().green()));
            })))).forEach((k, v) -> {
                var map = offsets.computeIfAbsent(k, a -> new HashMap<>());

                if(map != null) map.putAll(v);



//                if(v.containsKey("UVScaleOffset")) offsets.put(k, v.get("UVScaleOffset"));
            });
        }

        return offsets;
    }

    private static TransformStorage<Float> toStorage(TrackMaterialValueList value) {
        var storage = new TransformStorage<Float>();

        for (int i = 0; i < value.valuesLength(); i++) {
            var val = value.values(i);

            storage.add(val.time(), val.value());
        }

        return storage;
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

            if(args.length == 2 && args[0].equals("gltfconvert")) {
                var root = Path.of(args[1]);

                var reader = new GltfModelReader();
                var writer= new GltfModelWriter();

                Files.walk(root, 4).filter(a -> a.getFileName().toString().endsWith(".glb")).forEach(path -> {
                    try {
                        var gltf = reader.readWithoutReferences(Files.newInputStream(path));
                        writer.write(gltf, Path.of(path.toString().replace(".glb", ".gltf")).toFile());
                        System.out.println("<3");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                });

                return;
            }

            new CommandGUI(Main.ARGUMENTS, args);
        if (args.length == 0 || args[0].equals("help")) {
            System.err.println("Please specify which tool you want to use. Options: ");
//            ARGUMENTS.forEach(command -> Main.print(command.name() + " - " + command.description()));
        } else {
            var command = ARGUMENTS.stream()
                    .filter(cmd -> cmd.name().equals(args[0]))
                    .findAny();

            if (command.isEmpty()) System.err.println("No command with the name \"" + args[0] + "\"");
            else command.get().consumer().accept(Arrays.copyOfRange(args, 1, args.length));
        }
        } catch (Exception e) {
            printError(e);
        }
    }
}
