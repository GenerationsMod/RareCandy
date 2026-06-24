package gg.generations.rarecandy.renderer.model.material;

import com.google.gson.JsonObject;
import gg.generations.rarecandy.pokeutils.MaterialReference;
import imgui.ImGui;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Objects;

public record MaterialValues(
        Vector3f baseColor1,
        Vector3f baseColor2,
        Vector3f baseColor3,
        Vector3f baseColor4,
        Vector3f baseColor5,
        Vector3f emiColor1,
        Vector3f emiColor2,
        Vector3f emiColor3,
        Vector3f emiColor4,
        Vector3f emiColor5,
        float emiIntensity1,
        float emiIntensity2,
        float emiIntensity3,
        float emiIntensity4,
        float emiIntensity5,
        boolean useLight,
        boolean disableDepth
) implements IMaterialValues {

//
//    public void fill(JsonObject object) {
//        if (object.has("color")) baseColor1 = MaterialReference.color(object.get("color"));
//        if (object.has("baseColor1")) baseColor1 = MaterialReference.color(object.get("baseColor1"));
//        if (object.has("baseColor2")) baseColor2 = MaterialReference.color(object.get("baseColor2"));
//        if (object.has("baseColor3")) baseColor3 = MaterialReference.color(object.get("baseColor3"));
//        if (object.has("baseColor4")) baseColor4 = MaterialReference.color(object.get("baseColor4"));
//        if (object.has("baseColor5")) baseColor5 = MaterialReference.color(object.get("baseColor5"));
//        if (object.has("emiColor1")) emiColor1 = MaterialReference.color(object.get("emiColor1"));
//        if (object.has("emiColor2")) emiColor2 = MaterialReference.color(object.get("emiColor2"));
//        if (object.has("emiColor3")) emiColor3 = MaterialReference.color(object.get("emiColor3"));
//        if (object.has("emiColor4")) emiColor4 = MaterialReference.color(object.get("emiColor4"));
//        if (object.has("emiColor5")) emiColor5 = MaterialReference.color(object.get("emiColor5"));
//        if (object.has("emiIntensity1")) emiIntensity1 = object.get("emiIntensity1").getAsFloat();
//        if (object.has("emiIntensity2")) emiIntensity2 = object.get("emiIntensity2").getAsFloat();
//        if (object.has("emiIntensity3")) emiIntensity3 = object.get("emiIntensity3").getAsFloat();
//        if (object.has("emiIntensity4")) emiIntensity4 = object.get("emiIntensity4").getAsFloat();
//        if (object.has("emiIntensity5")) emiIntensity5 = object.get("emiIntensity5").getAsFloat();
//        if (object.has("useLight")) useLight = object.get("useLight").getAsBoolean();
//        if (object.has("disableDepth")) disableDepth = object.get("disableDepth").getAsBoolean();
//    }
//
//    public MaterialValues fill(MaterialValues values) {
//        if(values.baseColor1 != null) this.baseColor1 = values.baseColor1;
//        if(values.baseColor2 != null) this.baseColor2 = values.baseColor2;
//        if(values.baseColor3 != null) this.baseColor3 = values.baseColor3;
//        if(values.baseColor4 != null) this.baseColor4 = values.baseColor4;
//        if(values.baseColor5 != null) this.baseColor5 = values.baseColor5;
//
//        if(values.emiColor1 != null) this.emiColor1 = values.emiColor1;
//        if(values.emiColor2 != null) this.emiColor2 = values.emiColor2;
//        if(values.emiColor3 != null) this.emiColor3 = values.emiColor3;
//        if(values.emiColor4 != null) this.emiColor4 = values.emiColor4;
//        if(values.emiColor5 != null) this.emiColor5 = values.emiColor5;
//
//        if(values.emiIntensity1 != 0.0f) this.emiIntensity1 = values.emiIntensity1;
//        if(values.emiIntensity2 != 0.0f) this.emiIntensity2 = values.emiIntensity2;
//        if(values.emiIntensity3 != 0.0f) this.emiIntensity3 = values.emiIntensity3;
//        if(values.emiIntensity4 != 0.0f) this.emiIntensity4 = values.emiIntensity4;
//        if(values.emiIntensity5 != 1.0f) this.emiIntensity5 = values.emiIntensity5;
//
//        if(values.useLight != this.useLight) this.useLight = values.useLight;
//        if(values.disableDepth != this.disableDepth) this.disableDepth = values.disableDepth;
//
//        return this;
//    }
//
//    public MaterialValues complete() {
//        if(baseColor1 == null) this.baseColor1 = new Vector3f(1f, 1f, 1f);
//        if(baseColor2 == null) this.baseColor2 = new Vector3f(1f, 1f, 1f);
//        if(baseColor3 == null) this.baseColor3 = new Vector3f(1f, 1f, 1f);
//        if(baseColor4 == null) this.baseColor4 = new Vector3f(1f, 1f, 1f);
//        if(baseColor5 == null) this.baseColor5 = new Vector3f(1f, 1f, 1f);
//
//        if(emiColor1 == null) this.emiColor1 = new Vector3f(0f, 0f, 0f);
//        if(emiColor2 == null) this.emiColor2 = new Vector3f(0f, 0f, 0f);
//        if(emiColor3 == null) this.emiColor3 = new Vector3f(0f, 0f, 0f);
//        if(emiColor4 == null) this.emiColor4 = new Vector3f(0f, 0f, 0f);
//        if(emiColor5 == null) this.emiColor5 = new Vector3f(1f, 1f, 1f);
//
//        return this;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof MaterialValues that)) return false;
//        return Float.compare(emiIntensity1, that.emiIntensity1) == 0 &&
//                Float.compare(emiIntensity2, that.emiIntensity2) == 0 &&
//                Float.compare(emiIntensity3, that.emiIntensity3) == 0 &&
//                Float.compare(emiIntensity4, that.emiIntensity4) == 0 &&
//                Float.compare(emiIntensity5, that.emiIntensity5) == 0 &&
//                Objects.equals(baseColor1, that.baseColor1) &&
//                Objects.equals(baseColor2, that.baseColor2) &&
//                Objects.equals(baseColor3, that.baseColor3) &&
//                Objects.equals(baseColor4, that.baseColor4) &&
//                Objects.equals(baseColor5, that.baseColor5) &&
//                Objects.equals(emiColor1, that.emiColor1) &&
//                Objects.equals(emiColor2, that.emiColor2) &&
//                Objects.equals(emiColor3, that.emiColor3) &&
//                Objects.equals(emiColor4, that.emiColor4) &&
//                Objects.equals(emiColor5, that.emiColor5) &&
//                useLight == that.useLight &&
//                disableDepth == that.disableDepth;
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(
//                baseColor1,
//                baseColor2,
//                baseColor3,
//                baseColor4,
//                baseColor5,
//                emiColor1,
//                emiColor2,
//                emiColor3,
//                emiColor4,
//                emiColor5,
//                emiIntensity1,
//                emiIntensity2,
//                emiIntensity3,
//                emiIntensity4,
//                emiIntensity5,
//                useLight,
//                disableDepth);
//    }


}
