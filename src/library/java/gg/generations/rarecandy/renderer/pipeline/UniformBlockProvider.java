package gg.generations.rarecandy.renderer.pipeline;

import gg.generations.rarecandy.renderer.ubo.UniformBlockUploader;

public interface UniformBlockProvider {
    public UniformBlockUploader get();
}
