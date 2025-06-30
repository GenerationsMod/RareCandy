package gg.generations.rarecandy.renderer.model.material;

import gg.generations.rarecandy.renderer.ubo.UniformBlockUploader;

public class MaterialUploader extends UniformBlockUploader {
    public static MaterialUploader INSTANCE;

    public MaterialUploader(int index) {
        super(208, index);
    }

    public void upload(Material material) {
        this.upload(0, 208, material.getPointer());
    }

    public static void setup(int binding) {
        INSTANCE = new MaterialUploader(binding);
    }
}
