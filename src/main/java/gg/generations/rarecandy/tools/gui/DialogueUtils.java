package gg.generations.rarecandy.tools.gui;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.nfd.NFDFilterItem;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DialogueUtils {

    public static Path saveFile(String filterList) {
        var outPath = MemoryUtil.memAllocPointer(1);
        var result = NativeFileDialog.NFD_SaveDialog(outPath, toBuffer(filterList), (CharSequence) null, (CharSequence) null);
        if (result == NativeFileDialog.NFD_OKAY) {
            return Paths.get(outPath.getStringUTF8(0));
        }

        MemoryUtil.memFree(outPath);
        return null;
    }

    public static Path chooseFile(String filterList) {
        var outPath = MemoryUtil.memAllocPointer(1);
        var result = NativeFileDialog.NFD_OpenDialog(outPath, toBuffer(filterList), (CharSequence) null);
        if (result == NativeFileDialog.NFD_OKAY) {
            return Paths.get(outPath.getStringUTF8(0));
        }

        MemoryUtil.memFree(outPath);
        return null;
    }

    public static NFDFilterItem.Buffer toBuffer(String string) {
        return new NFDFilterItem.Buffer(ByteBuffer.wrap(string.getBytes()));
    }
}