package gg.generations.rarecandy.tools.swsh;

import gg.generations.rarecandy.tools.gui.DialogueUtils;
import org.lwjgl.util.nfd.NativeFileDialog;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class LongBoi {
    public static void main(String[] args) throws IOException {
        NativeFileDialog.NFD_Init();
        var paths = DialogueUtils.chooseMultipleFiles("PNG;png");
        NativeFileDialog.NFD_Quit();

        if(paths == null) {
            return;
        }

        for (Path path : paths) {
            BufferedImage input = ImageIO.read(path.toFile());

            BufferedImage mirrored = new BufferedImage(input.getWidth() * 2, input.getHeight(), BufferedImage.TYPE_INT_ARGB);
            mirrored.getGraphics().drawImage(input, 0, 0, null);
            mirrored.getGraphics().drawImage(EyeTexture.horizontalFlip(input), input.getWidth(), 0, null);

            ImageIO.write(mirrored, "PNG", path.toFile());
        }
    }
}
