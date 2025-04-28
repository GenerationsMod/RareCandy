package gg.generations.rarecandy.renderer.model.material;

import com.google.gson.JsonObject;
import gg.generations.rarecandy.pokeutils.MaterialReference;
import org.joml.Vector3f;

import java.util.Objects;

public class MaterialValues {
    private Vector3f baseColor1;
    private Vector3f baseColor2;
    private Vector3f baseColor3;
    private Vector3f baseColor4;
    private Vector3f baseColor5;

    private Vector3f emiColor1;
    private Vector3f emiColor2;
    private Vector3f emiColor3;
    private Vector3f emiColor4;
    private Vector3f emiColor5;

    private float emiIntensity1 = 0.0f;
    private float emiIntensity2 = 0.0f;
    private float emiIntensity3 = 0.0f;
    private float emiIntensity4 = 0.0f;
    private float emiIntensity5 = 1.0f;

    private boolean useLight;

    public Vector3f getBaseColor1() {
        return baseColor1;
    }

    public void setBaseColor1(Vector3f baseColor1) {
        this.baseColor1 = baseColor1;
    }

    public Vector3f getBaseColor2() {
        return baseColor2;
    }

    public void setBaseColor2(Vector3f baseColor2) {
        this.baseColor2 = baseColor2;
    }

    public Vector3f getBaseColor3() {
        return baseColor3;
    }

    public void setBaseColor3(Vector3f baseColor3) {
        this.baseColor3 = baseColor3;
    }

    public Vector3f getBaseColor4() {
        return baseColor4;
    }

    public void setBaseColor4(Vector3f baseColor4) {
        this.baseColor4 = baseColor4;
    }

    public Vector3f getBaseColor5() {
        return baseColor5;
    }

    public void setBaseColor5(Vector3f baseColor5) {
        this.baseColor5 = baseColor5;
    }

    public Vector3f getEmiColor1() {
        return emiColor1;
    }

    public void setEmiColor1(Vector3f emiColor1) {
        this.emiColor1 = emiColor1;
    }

    public Vector3f getEmiColor2() {
        return emiColor2;
    }

    public void setEmiColor2(Vector3f emiColor2) {
        this.emiColor2 = emiColor2;
    }

    public Vector3f getEmiColor3() {
        return emiColor3;
    }

    public void setEmiColor3(Vector3f emiColor3) {
        this.emiColor3 = emiColor3;
    }

    public Vector3f getEmiColor4() {
        return emiColor4;
    }

    public void setEmiColor4(Vector3f emiColor4) {
        this.emiColor4 = emiColor4;
    }

    public Vector3f getEmiColor5() {
        return emiColor5;
    }

    public void setEmiColor5(Vector3f emiColor5) {
        this.emiColor5 = emiColor5;
    }

    public float getEmiIntensity1() {
        return emiIntensity1;
    }

    public void setEmiIntensity1(float emiIntensity1) {
        this.emiIntensity1 = emiIntensity1;
    }

    public float getEmiIntensity2() {
        return emiIntensity2;
    }

    public void setEmiIntensity2(float emiIntensity2) {
        this.emiIntensity2 = emiIntensity2;
    }

    public float getEmiIntensity3() {
        return emiIntensity3;
    }

    public void setEmiIntensity3(float emiIntensity3) {
        this.emiIntensity3 = emiIntensity3;
    }

    public float getEmiIntensity4() {
        return emiIntensity4;
    }

    public void setEmiIntensity4(float emiIntensity4) {
        this.emiIntensity4 = emiIntensity4;
    }

    public float getEmiIntensity5() {
        return emiIntensity5;
    }

    public void setEmiIntensity5(float emiIntensity5) {
        this.emiIntensity5 = emiIntensity5;
    }

    public boolean getUseLight() {
        return useLight;
    }

    public void setUseLight(boolean useLight) {
        this.useLight = useLight;
    }

    public void fill(JsonObject object) {
        if (object.has("color")) baseColor1 = MaterialReference.color(object.get("color"));
        if (object.has("baseColor1")) baseColor1 = MaterialReference.color(object.get("baseColor1"));
        if (object.has("baseColor2")) baseColor2 = MaterialReference.color(object.get("baseColor2"));
        if (object.has("baseColor3")) baseColor3 = MaterialReference.color(object.get("baseColor3"));
        if (object.has("baseColor4")) baseColor4 = MaterialReference.color(object.get("baseColor4"));
        if (object.has("baseColor5")) baseColor5 = MaterialReference.color(object.get("baseColor5"));
        if (object.has("emiColor1")) emiColor1 = MaterialReference.color(object.get("emiColor1"));
        if (object.has("emiColor2")) emiColor2 = MaterialReference.color(object.get("emiColor2"));
        if (object.has("emiColor3")) emiColor3 = MaterialReference.color(object.get("emiColor3"));
        if (object.has("emiColor4")) emiColor4 = MaterialReference.color(object.get("emiColor4"));
        if (object.has("emiColor5")) emiColor5 = MaterialReference.color(object.get("emiColor5"));
        if (object.has("emiIntensity1")) emiIntensity1 = object.get("emiIntensity1").getAsFloat();
        if (object.has("emiIntensity2")) emiIntensity2 = object.get("emiIntensity2").getAsFloat();
        if (object.has("emiIntensity3")) emiIntensity3 = object.get("emiIntensity3").getAsFloat();
        if (object.has("emiIntensity4")) emiIntensity4 = object.get("emiIntensity4").getAsFloat();
        if (object.has("emiIntensity5")) emiIntensity5 = object.get("emiIntensity5").getAsFloat();
        if (object.has("useLight")) useLight = object.get("useLight").getAsBoolean();
    }

    public MaterialValues fill(MaterialValues values) {
        if(values.baseColor1 != null) this.baseColor1 = values.baseColor1;
        if(values.baseColor2 != null) this.baseColor2 = values.baseColor2;
        if(values.baseColor3 != null) this.baseColor3 = values.baseColor3;
        if(values.baseColor4 != null) this.baseColor4 = values.baseColor4;
        if(values.baseColor5 != null) this.baseColor5 = values.baseColor5;

        if(values.emiColor1 != null) this.emiColor1 = values.emiColor1;
        if(values.emiColor2 != null) this.emiColor2 = values.emiColor2;
        if(values.emiColor3 != null) this.emiColor3 = values.emiColor3;
        if(values.emiColor4 != null) this.emiColor4 = values.emiColor4;
        if(values.emiColor5 != null) this.emiColor5 = values.emiColor5;

        if(values.emiIntensity1 != 0.0f) this.emiIntensity1 = values.emiIntensity1;
        if(values.emiIntensity2 != 0.0f) this.emiIntensity2 = values.emiIntensity2;
        if(values.emiIntensity3 != 0.0f) this.emiIntensity3 = values.emiIntensity3;
        if(values.emiIntensity4 != 0.0f) this.emiIntensity4 = values.emiIntensity4;
        if(values.emiIntensity5 != 1.0f) this.emiIntensity5 = values.emiIntensity5;

        if(values.useLight) this.useLight = false;

        return this;
    }

    public MaterialValues complete() {
        if(baseColor1 == null) this.baseColor1 = new Vector3f(1f, 1f, 1f);
        if(baseColor2 == null) this.baseColor2 = new Vector3f(1f, 1f, 1f);
        if(baseColor3 == null) this.baseColor3 = new Vector3f(1f, 1f, 1f);
        if(baseColor4 == null) this.baseColor4 = new Vector3f(1f, 1f, 1f);
        if(baseColor5 == null) this.baseColor5 = new Vector3f(1f, 1f, 1f);

        if(emiColor1 == null) this.emiColor1 = new Vector3f(0f, 0f, 0f);
        if(emiColor2 == null) this.emiColor2 = new Vector3f(0f, 0f, 0f);
        if(emiColor3 == null) this.emiColor3 = new Vector3f(0f, 0f, 0f);
        if(emiColor4 == null) this.emiColor4 = new Vector3f(0f, 0f, 0f);
        if(emiColor5 == null) this.emiColor5 = new Vector3f(0f, 0f, 0f);

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MaterialValues that)) return false;
        return Float.compare(emiIntensity1, that.emiIntensity1) == 0 && Float.compare(emiIntensity2, that.emiIntensity2) == 0 && Float.compare(emiIntensity3, that.emiIntensity3) == 0 && Float.compare(emiIntensity4, that.emiIntensity4) == 0 && Float.compare(emiIntensity5, that.emiIntensity5) == 0 && useLight == that.useLight && Objects.equals(baseColor1, that.baseColor1) && Objects.equals(baseColor2, that.baseColor2) && Objects.equals(baseColor3, that.baseColor3) && Objects.equals(baseColor4, that.baseColor4) && Objects.equals(baseColor5, that.baseColor5) && Objects.equals(emiColor1, that.emiColor1) && Objects.equals(emiColor2, that.emiColor2) && Objects.equals(emiColor3, that.emiColor3) && Objects.equals(emiColor4, that.emiColor4) && Objects.equals(emiColor5, that.emiColor5);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseColor1, baseColor2, baseColor3, baseColor4, baseColor5, emiColor1, emiColor2, emiColor3, emiColor4, emiColor5, emiIntensity1, emiIntensity2, emiIntensity3, emiIntensity4, emiIntensity5, useLight);
    }
}
