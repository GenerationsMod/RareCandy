package gg.generations.rarecandy.rendering;

import gg.generations.rarecandy.ThreadSafety;
import gg.generations.rarecandy.loading.ModelLoader;
import gg.generations.rarecandy.storage.ObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RareCandy {
    private static final Logger LOGGER = LoggerFactory.getLogger(StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass());
    public static boolean DEBUG_THREADS = false;
    public final ObjectManager objectManager = new ObjectManager();
    private final ModelLoader loader;
    private static final Queue<Runnable> TASKS = new ConcurrentLinkedQueue<>();

    public RareCandy() {
        ThreadSafety.initContextThread();
        var startLoad = System.currentTimeMillis();
        this.loader = new ModelLoader();
        LOGGER.info("RareCandy Startup took " + (System.currentTimeMillis() - startLoad) + "ms");
    }

    public void render(boolean clearInstances, double secondsPassed) {
        var task = TASKS.poll();
        while (task != null) {
            task.run();
            task = TASKS.poll();
        }

        objectManager.update(secondsPassed);
        objectManager.render();

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

    public static void fatal(String message) {
        throw new RuntimeException("Fatal RareCandy Error! '" + message + "'");
    }

    public static void runLater(Runnable r) {
        TASKS.add(r);
    }
}
