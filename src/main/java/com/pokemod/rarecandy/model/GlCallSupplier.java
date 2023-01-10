package com.pokemod.rarecandy.model;

import com.pokemod.rarecandy.components.MultiRenderObject;
import com.pokemod.rarecandy.components.RenderObject;
import de.javagl.jgltf.model.GltfModel;
import dev.thecodewarrior.binarysmd.studiomdl.SMDFile;

import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface GlCallSupplier<T extends RenderObject> {

    List<Runnable> getCalls(GltfModel model, Map<String, SMDFile> smdFiles, Map<String, byte[]> gfbFiles, MultiRenderObject<T> mro);
}
