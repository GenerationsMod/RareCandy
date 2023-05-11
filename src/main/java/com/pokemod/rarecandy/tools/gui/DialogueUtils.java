package com.pokemod.rarecandy.tools.gui;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DialogueUtils {

    public static Path saveFile(String filterList) {
        var outPath = MemoryUtil.memAllocPointer(1);
        var result = NativeFileDialog.NFD_SaveDialog(outPath, null, filterList, null);
        if (result == NativeFileDialog.NFD_OKAY) return Paths.get(outPath.getStringUTF8(0));

        MemoryUtil.memFree(outPath);
        return null;
    }

    public static Path chooseFile(String filterList) {
        var outPath = MemoryUtil.memAllocPointer(1);
        var result = NativeFileDialog.NFD_OpenDialog(outPath, null, filterList);
        if (result == NativeFileDialog.NFD_OKAY) return Paths.get(outPath.getStringUTF8(0));

        MemoryUtil.memFree(outPath);
        return null;
    }
}
