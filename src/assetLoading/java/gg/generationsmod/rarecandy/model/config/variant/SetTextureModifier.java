package gg.generationsmod.rarecandy.model.config.variant;

import com.google.gson.JsonObject;

public class SetTextureModifier implements VariantModifier {

    public final String material;
    public final int textureSlot;
    public final String append;
    public final String method;

    public SetTextureModifier(JsonObject object) {
        this.material = object.get("material").getAsJsonPrimitive().getAsString();
        this.textureSlot = object.getAsJsonPrimitive("textureSlot").getAsInt();
        this.append = object.get("append").getAsJsonPrimitive().getAsString();
        this.method = object.get("method").getAsJsonPrimitive().getAsString();
    }
}
