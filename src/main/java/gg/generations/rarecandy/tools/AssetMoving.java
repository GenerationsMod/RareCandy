package gg.generations.rarecandy.tools;

import gg.generations.rarecandy.tools.gui.DialogueUtils;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.stream.Collectors;

public class AssetMoving {
    private static Path base;
    private static Path pack;
    private static Path generations;
    private static Set<String> species;

    public static void main(String[] args) throws IOException {
        NativeFileDialog.NFD_Init();
        base = DialogueUtils.chooseFolder();
        if(base == null) return;
        pack = DialogueUtils.chooseFolder();
        if(pack == null) return;
        generations = DialogueUtils.chooseFile("TXT;txt");
        if(generations == null) return;

        species = Files.readAllLines(generations).stream().map(String::toLowerCase).collect(Collectors.toSet());

        moveFiles("PKs", "models");
        moveFiles("posers", "posers");
        moveFiles("resolvers", "resolvers");
    }

    private static void moveFiles(String main, String folder) throws IOException {
        var from = base.resolve(main);
        var to = pack.resolve("packs").resolve(pack).resolve("assets").resolve("generations_core").resolve("bedrock").resolve("pokemon").resolve(folder);

        if(from != null && to != null) {
            Files.newDirectoryStream(from, entry -> {
                var entryString = entry.toString();
                return species.stream().anyMatch(entryString::contains);
            }).forEach(x -> {
                try {
                    System.out.println(x);

                    Files.move(x, to.resolve(x.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
