package gg.generations.rarecandy.tools;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import gg.generations.rarecandy.pokeutils.PixelAsset;
import gg.generations.rarecandy.tools.gui.DialogueUtils;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class AssetMoving {
    private static List<Path> files;

    public static void main(String[] args) throws IOException {
        NativeFileDialog.NFD_Init();
        files = DialogueUtils.chooseMultipleFiles("PK;pk");
        if(files == null) return;
        var obj = new JsonObject();

        for (var file : files) {
            var name = file.getFileName().toString().replace(".pk", "");
            var blep = new JsonObject();


            obj.add("generations_core:" + name, blep);
            var variants = PixelAsset.of(file, null).getConfig().variants.keySet();

            for (var variant : variants) {
                var variantJson = new JsonObject();
                blep.add(variant, variantJson);
                variantJson.addProperty("profile", "generations_core:textures/pokemon/%s/profile-%s.png".formatted(name, variant));
                variantJson.addProperty("portrait", "generations_core:textures/pokemon/%s/portrait-%s.png".formatted(name, variant));
            }

        }

        var path = Path.of("sprite_mapping.json");

        if(Files.notExists(path)) Files.createFile(path);

        Files.writeString(path, new GsonBuilder().setPrettyPrinting().create().toJson(obj));
    }
}
