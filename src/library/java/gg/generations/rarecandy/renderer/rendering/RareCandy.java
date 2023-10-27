package gg.generations.rarecandy.renderer.rendering;

import gg.generations.rarecandy.renderer.LoggerUtil;
import gg.generations.rarecandy.renderer.ThreadSafety;
import gg.generations.rarecandy.renderer.loading.ModelLoader;
import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.storage.ObjectManager;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;

public class RareCandy {
    private static final Queue<Runnable> TASKS = new ConcurrentLinkedQueue<>();
    public static boolean DEBUG_THREADS = false;
    public final ObjectManager objectManager = new ObjectManager();
    private final ModelLoader loader;

    public RareCandy() {
        ThreadSafety.initContextThread();
        var startLoad = System.currentTimeMillis();
        this.loader = new ModelLoader();
        LoggerUtil.print("RareCandy Startup took " + (System.currentTimeMillis() - startLoad) + "ms");
    }

    public static void fatal(String message) {
        throw new RuntimeException("Fatal RareCandy Error! '" + message + "'");
    }

    public static void runLater(Runnable r) {
        TASKS.add(r);
    }

    public void render(boolean clearInstances, double secondsPassed) {
        render(mat -> true, clearInstances, secondsPassed);
    }

    public void render(Predicate<Material> predicate, boolean clearInstances, double secondsPassed) {
        var task = TASKS.poll();
        while (task != null) {
            task.run();
            task = TASKS.poll();
        }

        objectManager.update(secondsPassed);
        objectManager.render(predicate);

        if (clearInstances) {
            this.objectManager.clearObjects();
        }
    }

    public void close() {
        this.loader.close();
    }

    public ModelLoader getLoader() {
        return loader;
    }
}
