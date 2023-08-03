package gg.generations.rarecandy.tools.swsh;

import gg.generations.rarecandy.LoggerUtil;
import gg.generations.rarecandy.tools.Main;
import gg.generations.rarecandy.tools.gui.DialogueUtils;
import org.lwjgl.util.nfd.NativeFileDialog;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import static gg.generations.rarecandy.tools.swsh.EyeTexture.processEye;

public class MouthTexture {
    public static void main(String[] args) throws IOException {
        NativeFileDialog.NFD_Init();
        var chosenFile = DialogueUtils.chooseMultipleFiles("PNG;png");

        if(chosenFile == null) {
            LoggerUtil.print("Didn't select a folder");
            return;
        }

        for (Path texturePair : chosenFile) {
            BufferedImage expandedTextureA = processEye(texturePair);

            ImageIO.write(expandedTextureA, "PNG", texturePair.toFile());
        }
    }
}
