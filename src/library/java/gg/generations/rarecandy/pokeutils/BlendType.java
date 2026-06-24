package gg.generations.rarecandy.pokeutils;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public enum BlendType {
    None(-1, -1, -1, -1), Regular(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);

    private final int rgbSrc;
    private final int rgbDst;
    private final int alphaSrc;
    private final int alphaDst;

    BlendType(int rgbSrc, int rgbDst, int alphaSrc, int alphaDst) {
        this.rgbSrc = rgbSrc;
        this.rgbDst = rgbDst;
        this.alphaSrc = alphaSrc;
        this.alphaDst = alphaDst;
    }

    public void enable() {
        if(this != BlendType.Regular) return;
        GL20.glBlendFuncSeparate(rgbSrc, rgbDst, alphaSrc, alphaDst);
        GL11.glEnable(GL11.GL_BLEND);
    }

    public static BlendType from(String cull) {
        try {
            if (cull.equalsIgnoreCase("regular")) return Regular;
            else return None;
        } catch (Exception e) {
            return None;
        }
    }

    public static BlendType fromJson(JsonElement element) {
        return from(element.getAsJsonPrimitive().getAsString());
    }

    public void disable() {
        if(this != BlendType.Regular) return;
        GL11.glDisable(GL11.GL_BLEND);
    }

    public JsonElement toJson() {
        return new JsonPrimitive(name().toLowerCase());
    }
}
