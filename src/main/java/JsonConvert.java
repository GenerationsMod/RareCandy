import gg.generations.rarecandy.tools.gui.DialogueUtils;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class JsonConvert {
    public static void main(String[] arge) throws IOException {
        NativeFileDialog.NFD_Init();

        var root = DialogueUtils.chooseFolder();

        if(root == null) return;

        findJsonFiles(root).forEach(new Consumer<Path>() {
            @Override
            public void accept(Path path) {

            }
        });
    }

    public static List<Path> findJsonFiles(Path startPath) throws IOException {

        Predicate<Path> isJsonFile = path -> path.toString().toLowerCase().endsWith(".json") && Files.isRegularFile(path);

        return Files.walk(startPath)
                .filter(isJsonFile)
                .collect(Collectors.toList());
    }
}
