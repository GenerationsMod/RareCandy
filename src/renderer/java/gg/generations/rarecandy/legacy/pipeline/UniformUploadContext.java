package gg.generations.rarecandy.legacy.pipeline;

import gg.generations.rarecandy.arceus.model.Model;
import gg.generations.rarecandy.arceus.model.RenderingInstance;

public record UniformUploadContext(
        Model object,
        RenderingInstance instance,
        Uniform uniform
) {

    public void bindAndUploadTex(Texture texture, int slot) {
        texture.bind(slot);
        uniform.uploadInt(slot);
    }
}