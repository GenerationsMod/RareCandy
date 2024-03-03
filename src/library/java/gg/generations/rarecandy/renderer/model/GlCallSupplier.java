package gg.generations.rarecandy.renderer.model;

import de.javagl.jgltf.model.GltfModel;
import dev.thecodewarrior.binarysmd.studiomdl.SMDFile;
import gg.generations.rarecandy.pokeutils.ModelConfig;
import gg.generations.rarecandy.pokeutils.Pair;
import gg.generations.rarecandy.pokeutils.PixelAsset;
import gg.generations.rarecandy.pokeutils.reader.TextureReference;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.components.RenderObject;
import org.lwjgl.assimp.AIScene;

import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface GlCallSupplier<T extends RenderObject> {

    List<Runnable> getCalls(PixelAsset model, Map<String, SMDFile> smdFiles, Map<String, byte[]> gfbamnFiles, Map<String, Pair<byte[], byte[]>> trFiles, Map<String, String> imageFiles, ModelConfig config, MultiRenderObject<T> mro);
}
