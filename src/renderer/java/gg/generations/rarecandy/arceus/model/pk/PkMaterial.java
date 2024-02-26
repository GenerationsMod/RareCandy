package gg.generations.rarecandy.arceus.model.pk;

import gg.generations.rarecandy.arceus.model.Material;
import gg.generations.rarecandy.legacy.pipeline.ITexture;
import gg.generations.rarecandy.legacy.pipeline.ShaderProgram;
import gg.generations.rarecandy.legacy.pipeline.Texture;
import gg.generationsmod.rarecandy.model.config.pk.BlendType;
import gg.generationsmod.rarecandy.model.config.pk.CullType;
import gg.generationsmod.rarecandy.model.config.pk.MaterialReference;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PkMaterial implements Material {
    private final String materialName;
    private Map<String, String> images;

    private Map<String, Object> values;

    private CullType cullType;
    private BlendType blendType;

    private String shader;

    public PkMaterial(String materialName, Map<String, String> images, Map<String, Object> values, CullType cullType, BlendType blendType, String shader) {
        this.materialName = materialName;
        this.images = images;
        this.cullType = cullType;
        this.blendType = blendType;
        this.shader = shader;
        this.values = values;
    }

    public ITexture getDiffuseTexture() {
        return getTexture("diffuse");
    }

    public ShaderProgram getProgram() {
        return PipelineRegistry.get(shader);
    }

    @Override
    public String name() {
        return materialName;
    }

    public CullType cullType() {
        return cullType;
    }

    public BlendType blendType() {
        return blendType;
    }

    public ITexture getTexture(String imageType) {
        return TextureLoader.instance().getTexture(images.get(imageType));
    }

    public Object getValue(String valueType) {
        return values.get(valueType);
    }

    public String getMaterialName() {
        return materialName;
    }

    @Override
    public String toString() {
        return materialName;
    }

    @Override
    public void close() throws IOException {
        if(images != null) {
            for (var texture : images.values()) {
                if(texture.contains(".")) TextureLoader.instance().remove(texture);
            }
        }
    }

    public boolean getBoolean(String value) {
        return getValue(value) instanceof Boolean bool ? bool : false;
    }

    public static PkMaterial process(String name, @NotNull Map<String, MaterialReference> materialreferences, @NotNull Map<String, String> imageMap) {
        var reference = materialreferences.get(name);

        var cull = reference.cull;
        var blend = reference.blend;
        var shader = reference.shader;
        var images = new HashMap<>(reference.images);
        var values = new HashMap<>(reference.values);
        var parent = reference.parent;

        while (parent != null) {
            reference = materialreferences.get(parent);

            if(reference == null) parent = null;
            else {

                if (!shader.equals(reference.shader)) {
                    shader = reference.shader;
                }

                if (!cull.equals(reference.cull)) {
                    cull = reference.cull;
                }

                if (!blend.equals(reference.blend)) {
                    blend = reference.blend;
                }

                reference.images.forEach((key, value) -> images.merge(key, value, (old, value1) -> old));
                reference.values.forEach((key, value) -> values.merge(key, value, (old, value1) -> old));

                parent = reference.parent;
            }
        }

        var map = new HashMap<String, String>();
        for (Map.Entry<String, String> a : images.entrySet()) {
            if (imageMap.containsKey(a.getValue())) {
                if (map.put(a.getKey(), imageMap.get(a.getValue())) != null) {
                    throw new IllegalStateException("Duplicate key");
                }
            } else {
                map.put(a.getKey(), a.getValue());
            }
        }
        return new PkMaterial(name, map, values, cull, blend, shader);
    }
}
