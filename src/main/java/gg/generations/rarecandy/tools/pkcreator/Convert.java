package gg.generations.rarecandy.tools.pkcreator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import de.javagl.jgltf.model.io.GltfModelReader;
import dev.thecodewarrior.binarysmd.formats.SMDTextReader;
import dev.thecodewarrior.binarysmd.formats.SMDTextWriter;
import dev.thecodewarrior.binarysmd.studiomdl.SMDFileBlock;
import dev.thecodewarrior.binarysmd.studiomdl.SkeletonBlock;
import org.joml.Vector3f;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static gg.generations.rarecandy.legacy.LoggerUtil.print;
import static gg.generations.rarecandy.legacy.LoggerUtil.printError;

public class Convert {
    private static final GltfModelReader reader = new GltfModelReader();
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static String generateJson(float scale, List<String> images) {
        JsonObject json = new JsonObject();
        json.addProperty("scale", scale);

        JsonObject materials = new JsonObject();
        for (String image : images) {
            JsonObject material = new JsonObject();
            material.addProperty("type", "solid");
            material.addProperty("texture", image + ".png");
            materials.add(image, material);
        }
        json.add("materials", materials);

        JsonObject defaultVariant = new JsonObject();
        JsonObject defaultModel = new JsonObject();
        defaultModel.addProperty("material", images.get(0));
        defaultModel.addProperty("hide", "false");
        defaultVariant.add("model", defaultModel);
        json.add("defaultVariant", defaultVariant);

        JsonObject variants = new JsonObject();
        for (String image : images) {
            JsonObject variant = new JsonObject();
            if (!defaultVariant.get("model").getAsJsonObject().get("material").getAsString().equals(image)) {
                JsonObject variantModel = new JsonObject();
                variantModel.addProperty("material", image);
                variant.add("model", variantModel);
            }

            variants.add(image, variant);
        }
        json.add("variants", variants);

        return gson.toJson(json);
    }

    public static void main(String[] args) throws IOException {
        var inFolder = Paths.get("converter/in");
        var outFolder = Paths.get("converter/out");

        Files.createDirectories(inFolder);
        Files.createDirectories(outFolder);

        readSubfolders(inFolder, outFolder);
    }

    public static void readSubfolders(Path inputPath, Path outPath) {
        try {
//            Files.createDirectories(outputPath);

            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(inputPath);
            for (Path subfolder : directoryStream) {
                if (Files.isDirectory(subfolder)) {
                    String folderName = subfolder.getFileName().toString();
//                    if (folderNames.stream().anyMatch(folderName::contains)) {
                    if (Files.exists(subfolder.resolve("model.glb"))) {
                        // Copy contents to new subfolder
                        Path newSubfolder = inputPath.resolve(folderName);
                        if (!Files.notExists(newSubfolder)) {
                            Files.createDirectories(newSubfolder);
                        }

                        if (Files.exists(subfolder.resolve("textures"))) {
                            // Generate list of PNG names without extension
                            List<String> pngNames = generatePngNameList(subfolder.resolve("textures"));

                            // Create model.config file and write generated JSON
                            Path configFile = newSubfolder.resolve("config.json");

                            var scale = 1.0f;

                            try (var is = Files.newInputStream(newSubfolder.resolve("model.glb"))) {
                                var model = reader.readWithoutReferences(new ByteArrayInputStream(is.readAllBytes()));
                                var buf = model.getMeshModels().get(0).getMeshPrimitiveModels().get(0).getAttributes().get("POSITION").getBufferViewModel().getBufferViewData();

                                var smallestVertexX = 0f;
                                var smallestVertexY = 0f;
                                var smallestVertexZ = 0f;
                                var largestVertexX = 0f;
                                var largestVertexY = 0f;
                                var largestVertexZ = 0f;
                                for (int i = 0; i < buf.capacity(); i += 12) { // Start at the y entry of every vertex and increment by 12 because there are 12 bytes per vertex
                                    var xPoint = buf.getFloat(i);
                                    var yPoint = buf.getFloat(i + 4);
                                    var zPoint = buf.getFloat(i + 8);
                                    smallestVertexX = Math.min(smallestVertexX, xPoint);
                                    smallestVertexY = Math.min(smallestVertexY, yPoint);
                                    smallestVertexZ = Math.min(smallestVertexZ, zPoint);
                                    largestVertexX = Math.max(largestVertexX, xPoint);
                                    largestVertexY = Math.max(largestVertexY, yPoint);
                                    largestVertexZ = Math.max(largestVertexZ, zPoint);
                                }

                                scale = 1 / new Vector3f(largestVertexX - smallestVertexX, largestVertexY - smallestVertexY, largestVertexZ - smallestVertexZ).y;

                            } catch (Exception e) {
                                printError(e);
                            }

                            generateAndWriteJson(scale, pngNames, configFile);

                            PixelmonArchiveBuilder.convertToPk(inputPath, Files.walk(newSubfolder).filter(a -> !a.toString().endsWith("model.smd")).toList(), outPath.resolve(subfolder.getFileName().toString() + ".pk"));

                            print("Completed: " + folderName);
                        }
                    }
                }
            }
        } catch (IOException ex) {
//            throw new RuntimeException(ex);
        }
    }

    public static void copyContents(Path source, Path destination) throws IOException {
        Files.createDirectories(destination);
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith("model.smd")) {
                    List<String> string = List.of("blender.exe", "-noaudio", "--python", "C:\\Users\\water\\Documents\\Converter13.py", "--background", "--", file.toString(), destination.resolve("model.glb").toString());
//                    Main.print("Blep1: " + string);
                    ProcessBuilder processBuilder = new ProcessBuilder(string);
//                    processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
//                    processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                    Process process = processBuilder.start();

                    try {
                        process.waitFor();
//                        Main.print("Blep: " + process.waitFor());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                } else if (file.toString().endsWith(".smd")) {
                    var smdFile = new SMDTextReader().read(Files.readAllLines(file).stream().collect(Collectors.joining("\n")));

                    for (SMDFileBlock a : smdFile.blocks) {
                        if (a instanceof SkeletonBlock block) {
                            var frame0 = block.keyframes.get(0).states.get(0);

                            int state = 0;

                            if (frame0.rotX == 0) {
                                if (block.keyframes.get(0).states.get(1).rotX == 1.570796f) {
                                    state = 1;
                                } else {
                                    continue;
                                }
                            }

                            for (int i = 0; i < block.keyframes.size(); i++) {
                                var frame = block.keyframes.get(i);

                                frame.states.get(state).rotX = 0;
                            }
                        }
                    }

                    Files.writeString(destination.resolve(file.getFileName().toString()), new SMDTextWriter().write(smdFile));

//                    List<String> string = List.of("blender.exe", "-noaudio", "--python", "C:\\Users\\water\\Documents\\Converter_Anim.py", "--background", "--", file.toString(), destination.resolve(file.getFileName().toString()).toString());
//                    Main.print("Blep1: " + string);
//                    ProcessBuilder processBuilder = new ProcessBuilder(string);
//                    processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
//                    processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
//                    Process process = processBuilder.start();

//                    try {
//                        process.waitFor();
////                        Main.print("Blep: " + process.waitFor());
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }

                } else {
                    Files.copy(file, destination.resolve(file.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static List<String> generatePngNameList(Path folderPath) throws IOException {
        List<String> pngNames = new ArrayList<>();
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folderPath, "*.png");
        for (Path filePath : directoryStream) {
            String fileName = filePath.getFileName().toString();
            String nameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
            pngNames.add(nameWithoutExtension);
        }
        return pngNames;
    }

    public static void generateAndWriteJson(float version, List<String> pngNames, Path configFile) throws IOException {
        // Call the generateJson method to get the JSON string
        String json = generateJson(version, pngNames);

        // Write the JSON to the model.config file
        Files.writeString(configFile, json);
    }
}
