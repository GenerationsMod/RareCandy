package gg.generations.rarecandy.tools.gui;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.nfd.NFDFilterItem;
import org.lwjgl.util.nfd.NFDPathSetEnum;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static gg.generations.rarecandy.renderer.LoggerUtil.print;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.util.nfd.NativeFileDialog.*;

public class DialogueUtils {

    private static ExecutorService DIALOG_THREAD;

    public static void init() {
        DIALOG_THREAD = Executors.newSingleThreadExecutor();
        CompletableFuture.runAsync(NativeFileDialog::NFD_Init, DIALOG_THREAD);
    }

    public static void quit() {
        CompletableFuture.runAsync(NativeFileDialog::NFD_Quit, DIALOG_THREAD)
                .thenRun(DIALOG_THREAD::shutdown);
    }

    public static void saveFile(String defaultPath, String filterList, Consumer<Path> consumer) {
        CompletableFuture.supplyAsync(() -> {
            var array = filterList.split(";");
            var filters = NFDFilterItem.malloc(1);
            var name = MemoryUtil.memUTF8(array[0]);
            var spec = MemoryUtil.memUTF8(array[1]);
            var outPath = MemoryUtil.memAllocPointer(1);
            try {
                filters.get(0).name(name).spec(spec);
                var result = NativeFileDialog.NFD_SaveDialog(outPath, filters, null, defaultPath);
                if (result == NFD_OKAY) {
                    var path = Paths.get(outPath.getStringUTF8(0));
                    NFD_FreePath(outPath.get(0));
                    return path;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                MemoryUtil.memFree(name);
                MemoryUtil.memFree(spec);
                MemoryUtil.memFree(outPath);
                filters.free();
            }
            return null;
        }, DIALOG_THREAD).thenAccept(consumer);
    }

    public static void chooseMultipleFiles(String title, String defaultPath, String filterList, Consumer<List<Path>> consumer) {
        CompletableFuture.supplyAsync(() -> {
            var list = Stream.of(filterList.split(":")).map(a -> a.split(";")).filter(a -> a.length == 2).toList();
            var filters = NFDFilterItem.malloc(list.size());
            var names = list.stream().map(a -> MemoryUtil.memUTF8(a[0])).toList();
            var specs = list.stream().map(a -> MemoryUtil.memUTF8(a[1])).toList();
            var pp = MemoryUtil.memAllocPointer(1);
            try {
                for (int i = 0; i < list.size(); i++) {
                    filters.get(i).name(names.get(i)).spec(specs.get(i));
                }
                var result = NativeFileDialog.NFD_OpenDialogMultiple(pp, filters, defaultPath);
                if (result == NFD_OKAY) {
                    long pathSet = pp.get(0);
                    NFDPathSetEnum psEnum = NFDPathSetEnum.calloc();
                    NFD_PathSet_GetEnum(pathSet, psEnum);
                    List<Path> paths = new ArrayList<>();
                    while (NFD_PathSet_EnumNext(psEnum, pp) == NFD_OKAY && pp.get(0) != NULL) {
                        paths.add(Path.of(pp.getStringUTF8(0)));
                        NFD_PathSet_FreePath(pp.get(0));
                    }
                    NFD_PathSet_FreeEnum(psEnum);
                    NFD_PathSet_Free(pathSet);
                    return paths;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                names.forEach(MemoryUtil::memFree);
                specs.forEach(MemoryUtil::memFree);
                MemoryUtil.memFree(pp);
                filters.free();
            }
            return null;
        }, DIALOG_THREAD).thenAccept(consumer);
    }

    public static void chooseFile(String defaultPath, String filterList, Consumer<Path> consumer) {
//        var path = Path.of(defaultPath).toAbsolutePath().toString();

        CompletableFuture.supplyAsync(() -> {
            var array = filterList.split(";");
            var filters = NFDFilterItem.malloc(1);
            var name = MemoryUtil.memUTF8(array[0]);
            var spec = MemoryUtil.memUTF8(array[1]);
            var outPath = MemoryUtil.memAllocPointer(1);
            try {
                filters.get(0).name(name).spec(spec);
                var result = NativeFileDialog.NFD_OpenDialog(outPath, filters, defaultPath);
                print("NFD_OpenDialog result: " + result);
                print("NFD_OpenDialog defaultPath: " + defaultPath);
                if (result == NFD_OKAY) {
                    var path = Paths.get(outPath.getStringUTF8(0));
                    NFD_FreePath(outPath.get(0));
                    return path;
                } else if (result == NFD_ERROR) {
                    print("NFD_Error: " + NativeFileDialog.NFD_GetError());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                MemoryUtil.memFree(name);
                MemoryUtil.memFree(spec);
                MemoryUtil.memFree(outPath);
                filters.free();
            }
            return null;
        }, DIALOG_THREAD).thenAccept(consumer);
    }

    public static void chooseFolder(String defaultPath, Consumer<Path> consumer) {
        CompletableFuture.supplyAsync(() -> {
            var outPath = MemoryUtil.memAllocPointer(1);
            try {
                var result = NativeFileDialog.NFD_PickFolder(outPath, defaultPath);
                if (result == NFD_OKAY) {
                    var path = Paths.get(outPath.getStringUTF8(0));
                    NFD_FreePath(outPath.get(0));
                    return path;
                } else if (result == NFD_ERROR) {
                    print(NativeFileDialog.NFD_GetError());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                MemoryUtil.memFree(outPath);
            }
            return null;
        }, DIALOG_THREAD).thenAccept(consumer);
    }
}