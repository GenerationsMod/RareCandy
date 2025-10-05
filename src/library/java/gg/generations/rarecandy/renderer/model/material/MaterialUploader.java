package gg.generations.rarecandy.renderer.model.material;

import gg.generations.rarecandy.renderer.ubo.UniformBlockUploader;

public class MaterialUploader extends UniformBlockUploader {
    public static MaterialUploader INSTANCE;

    public MaterialUploader() {
        super(192, 2);
    }

    public void upload(Material material) {
        this.upload(0, 192, material.getPointer());
    }

    public static void setup() {
        INSTANCE = new MaterialUploader();
    }
}
