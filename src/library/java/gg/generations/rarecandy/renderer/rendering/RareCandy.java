package gg.generations.rarecandy.renderer.rendering;

import gg.generations.rarecandy.renderer.LoggerUtil;
import gg.generations.rarecandy.renderer.ThreadSafety;
import gg.generations.rarecandy.renderer.pipeline.neo.regular.Pipeline;
import gg.generations.rarecandy.renderer.storage.ObjectManager;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RareCandy {
    private static final Queue<Runnable> TASKS = new ConcurrentLinkedQueue<>();
    public static boolean DEBUG_THREADS = true;
    public final ObjectManager objectManager = new ObjectManager();
    public RareCandy() {
        ThreadSafety.initContextThread();
        var startLoad = System.currentTimeMillis();
        LoggerUtil.print("RareCandy Startup took " + (System.currentTimeMillis() - startLoad) + "ms");
    }

    public static void fatal(String message) {
        throw new RuntimeException("Fatal RareCandy Error! '" + message + "'");
    }

    public static void runLater(Runnable r) {
        TASKS.add(r);
    }

    public void update(double secondsPassed) {
        var task = TASKS.poll();
        while (task != null) {
            task.run();
            task = TASKS.poll();
        }

        objectManager.update(secondsPassed);
    }

    public void render(Pipeline pipeline, RenderStage stage, boolean clearInstances) {

        objectManager.render(pipeline, stage);

        if (clearInstances) {
            this.objectManager.clearObjects();
        }
    }
}
