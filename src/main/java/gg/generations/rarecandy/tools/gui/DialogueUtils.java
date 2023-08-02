package gg.generations.rarecandy.tools.gui;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.nfd.NFDFilterItem;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DialogueUtils {

    public static Path saveFile(String filterList) {
        try (MemoryStack stack = MemoryStack.stackPush(); var filters = NFDFilterItem.malloc(1)) {
            var array = filterList.split(";");
            filters.get(0).name(stack.UTF8(array[0])).spec(stack.UTF8(array[1]));

            var outPath = MemoryUtil.memAllocPointer(1);
            var result = NativeFileDialog.NFD_SaveDialog(outPath, filters, null, (CharSequence) null);
            if (result == NativeFileDialog.NFD_OKAY)
                return Paths.get(outPath.getStringUTF8(0));
        }

        return null;
    }

    public static Path chooseFile(String filterList) {
        try (MemoryStack stack = MemoryStack.stackPush(); var filters = NFDFilterItem.malloc(1)) {
            var outPath = stack.callocPointer(1);
            var array = filterList.split(";");
            filters.get(0).name(stack.UTF8(array[0])).spec(stack.UTF8(array[1]));

            var result = NativeFileDialog.NFD_OpenDialog(outPath, filters, (CharSequence) null);
            if (result == NativeFileDialog.NFD_OKAY) {
                return Paths.get(outPath.getStringUTF8(0));
            }
        }

        return null;
    }
}