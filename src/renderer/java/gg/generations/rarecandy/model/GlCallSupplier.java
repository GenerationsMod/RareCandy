package gg.generations.rarecandy.model;

import gg.generations.pokeutils.ModelConfig;
import gg.generations.rarecandy.components.MultiRenderObject;
import gg.generations.rarecandy.components.RenderObject;
import de.javagl.jgltf.model.GltfModel;
import dev.thecodewarrior.binarysmd.studiomdl.SMDFile;

import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface GlCallSupplier<T extends RenderObject> {

    List<Runnable> getCalls(GltfModel model, Map<String, SMDFile> smdFiles, Map<String, byte[]> gfbFiles, ModelConfig config, MultiRenderObject<T> mro);
}
