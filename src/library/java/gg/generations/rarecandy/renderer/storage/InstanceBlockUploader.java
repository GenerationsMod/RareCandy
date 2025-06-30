package gg.generations.rarecandy.renderer.storage;

import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import gg.generations.rarecandy.renderer.ubo.UniformBlockUploader;

import java.util.HashMap;
import java.util.Map;

public class InstanceBlockUploader extends UniformBlockUploader {
    public static final Map<Class<? extends ObjectInstance>, InstanceBlockUploader> UPLOADER_MAP = new HashMap<>();

    private final int size;

    public InstanceBlockUploader(int size, int index) {
        super(size, index);
        this.size = size;
    }

    public static <T extends ObjectInstance> void bind(T t) {
        var uploader = UPLOADER_MAP.get(t.getClass());

        if(uploader == null) throw new RuntimeException("Error no block uploader for " + t.getClass().getSimpleName());

        t.update();

        uploader.upload(0, uploader.size, t.pointer);
    }

    public static void register(Class<? extends ObjectInstance> clazz, int binding, int size) {
        UPLOADER_MAP.put(clazz, new InstanceBlockUploader(size, binding));
    }
}
