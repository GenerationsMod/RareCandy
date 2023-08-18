package gg.generations.rarecandy.model;

import de.javagl.jgltf.model.GltfModel;
import dev.thecodewarrior.binarysmd.studiomdl.SMDFile;
import gg.generations.pokeutils.ModelConfig;
import gg.generations.pokeutils.reader.TextureReference;
import gg.generations.rarecandy.components.MultiRenderObject;
import gg.generations.rarecandy.components.RenderObject;

import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface GlCallSupplier<T extends RenderObject> {

    List<Runnable> getCalls(GltfModel model, Map<String, SMDFile> smdFiles, Map<String, byte[]> gfbFiles, Map<String, TextureReference> imageFiles, ModelConfig config, MultiRenderObject<T> mro);
}
