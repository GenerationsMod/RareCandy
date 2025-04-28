package gg.generations.rarecandy.renderer.model.material;

import com.google.gson.JsonObject;

import java.util.Map;
import java.util.stream.Stream;

public class MaterialImages {
    private String diffuse;
    private String layer;
    private String mask;
    private String emission;

    public String getDiffuse() {
        return diffuse;
    }

    public MaterialImages setDiffuse(String diffuse) {
        this.diffuse = diffuse;
        return this;
    }

    public String getLayer() {
        return layer;
    }

    public void setLayer(String layer) {
        this.layer = layer;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public String getEmission() {
        return emission;
    }

    public void setEmission(String emission) {
        this.emission = emission;
    }

    public MaterialImages fill(MaterialImages images) {
        if(images.diffuse != null) this.diffuse = images.diffuse;
        if(images.layer != null) this.layer = images.layer;
        if(images.mask != null) this.mask = images.mask;
        if(images.emission != null) this.emission = images.emission;

        return this;
    }

    public MaterialImages fill(JsonObject images) {
        if(images.has("diffuse"))  this.diffuse = images.getAsJsonPrimitive("diffuse").getAsString();
        if(images.has("layer"))  this.layer = images.getAsJsonPrimitive("layer").getAsString();
        if(images.has("mask")) this.mask = images.getAsJsonPrimitive("mask").getAsString();
        if(images.has("emission"))  this.emission = images.getAsJsonPrimitive("emission").getAsString();

        return this;
    }

    public MaterialImages complete() {
        if(this.diffuse == null) this.diffuse = "bright";
        if(this.layer == null) this.layer = "dark";
        if(this.mask == null) this.mask = "dark";
        if(this.emission == null) this.emission = "dark";

        return this;
    }

    public MaterialImages processWithImageMap(Map<String, String> imageMap) {
        var out = new MaterialImages();

        out.diffuse = imageMap.getOrDefault(diffuse, diffuse);
        out.layer = imageMap.getOrDefault(layer, layer);
        out.mask = imageMap.getOrDefault(mask, mask);
        out.emission = imageMap.getOrDefault(emission, emission);

        return out;
    }

    public Stream<String> stream() {
        return Stream.of(diffuse, emission, layer, mask);
    }
}
