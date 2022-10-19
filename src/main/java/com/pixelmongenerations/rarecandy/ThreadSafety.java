package com.pixelmongenerations.rarecandy;

import com.pixelmongenerations.rarecandy.rendering.RareCandy;
import org.jetbrains.annotations.Nullable;

public class ThreadSafety {

    @Nullable
    private static Thread contextThread;

    public static void initContextThread() {
        contextThread = Thread.currentThread();
    }

    public static void assertContextThread() {
        if(Thread.currentThread() != contextThread) {
            throw new RuntimeException("Code run on wrong thread.");
        }
    }

    public static Runnable wrapException(Runnable r) {
        return () -> {
            try {
                r.run();
            } catch (Exception e) {
                RareCandy.runLater(() -> {throw e;});
                throw new RuntimeException("Stopping Thread due to Error");
            }
        };
    }

    public static void runOnContextThread(Runnable r) {
        try {
            RareCandy.runLater(r);
        } catch (Exception e) {
            RareCandy.runLater(() -> {throw e;});
            throw new RuntimeException("Stopping Thread due to Error");
        }
    }
}
