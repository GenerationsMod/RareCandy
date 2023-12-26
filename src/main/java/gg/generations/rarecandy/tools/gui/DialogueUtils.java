package gg.generations.rarecandy.tools.gui;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.nfd.NFDFilterItem;
import org.lwjgl.util.nfd.NFDPathSetEnum;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static gg.generations.rarecandy.renderer.LoggerUtil.print;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.util.nfd.NativeFileDialog.*;

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

    public static List<Path> chooseMultipleFiles(String filterList) {
        var list = Stream.of(filterList.split(":")).map(a -> a.split(";")).filter(a -> a.length == 2).toList();

        try (MemoryStack stack = MemoryStack.stackPush(); var filters = NFDFilterItem.malloc(list.size())) {
            var pp = stack.callocPointer(1);
            for (int i = 0; i < list.size(); i++) {
                var element = list.get(i);
                filters.get(i).name(stack.UTF8(element[0])).spec(stack.UTF8(element[1]));
            }

            var result = NativeFileDialog.NFD_OpenDialogMultiple(pp, filters, (CharSequence) null);

            if (result == NativeFileDialog.NFD_OKAY) {
                long pathSet = pp.get(0);

                NFDPathSetEnum psEnum = NFDPathSetEnum.calloc(stack);
                NFD_PathSet_GetEnum(pathSet, psEnum);

                List<Path> paths = new ArrayList<>();

                int i = 0;
                while (NFD_PathSet_EnumNext(psEnum, pp) == NFD_OKAY && pp.get(0) != NULL) {
                    paths.add(Path.of(pp.getStringUTF8(0)));
                    NFD_PathSet_FreePath(pp.get(0));
                }

                NFD_PathSet_FreeEnum(psEnum);
                NFD_PathSet_Free(pathSet);


                System.out.println("Blep4");
                return paths;
            }
        }

        System.out.println("Blep");
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Path chooseFolder() {
        var outPath = MemoryUtil.memAllocPointer(1);

        try {
            var result = NativeFileDialog.NFD_PickFolder(outPath, (CharSequence) null);

            if (result == NativeFileDialog.NFD_OKAY) {
                return Paths.get(outPath.getStringUTF8(0));
            } else if (result == NativeFileDialog.NFD_ERROR) {
                print(NativeFileDialog.NFD_GetError());
            }
        } finally {
            MemoryUtil.memFree(outPath);
        }


        return null;
    }
}