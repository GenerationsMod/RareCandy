package gg.generations.rarecandy.renderer.model.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import gg.generations.rarecandy.pokeutils.util.JsonUtils;
import gg.generations.rarecandy.renderer.Colors;
import gg.generations.rarecandy.renderer.storage.SSBOBuffer;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.util.function.Function;

public interface IMaterialValues {
    IMaterialValues DEFAULT = new IMaterialValues() {
        @Override public Vector3f baseColor1() { return Colors.WHITE; }

        @Override public Vector3f baseColor2() { return Colors.WHITE; }

        @Override public Vector3f baseColor3() { return Colors.WHITE; }

        @Override public Vector3f baseColor4() { return Colors.WHITE; }

        @Override public Vector3f baseColor5() { return Colors.WHITE; }

        @Override public Vector3f emiColor1() { return Colors.BLACK; }

        @Override public Vector3f emiColor2() { return Colors.BLACK; }

        @Override public Vector3f emiColor3() { return Colors.BLACK; }

        @Override public Vector3f emiColor4() { return Colors.BLACK; }

        @Override public Vector3f emiColor5() { return Colors.WHITE; }

        @Override public float emiIntensity1() { return 0.0f; }

        @Override public float emiIntensity2() { return 0.0f; }

        @Override public float emiIntensity3() { return 0.0f; }

        @Override public float emiIntensity4() { return 0.0f; }

        @Override public float emiIntensity5() { return 1.0f; }

        @Override public boolean useLight() { return true; }

        @Override public boolean disableDepth() { return false; }

        @Override
        public boolean isNotEmpty() {
            return false;
        }
    };

    Vector3f baseColor1();
    Vector3f baseColor2();
    Vector3f baseColor3();
    Vector3f baseColor4();
    Vector3f baseColor5();
    Vector3f emiColor1();
    Vector3f emiColor2();
    Vector3f emiColor3();
    Vector3f emiColor4();
    Vector3f emiColor5();
    float emiIntensity1();
    float emiIntensity2();
    float emiIntensity3();
    float emiIntensity4();
    float emiIntensity5();
    boolean useLight();
    boolean disableDepth();

    default void put(SSBOBuffer pointer) {
        pointer.put(baseColor1()).put(0f);
        pointer.put(baseColor2()).put(0f);
        pointer.put(baseColor3()).put(0f);
        pointer.put(baseColor4()).put(0f);
        pointer.put(baseColor5()).put(0f);

        pointer.put(emiColor1()).put(0f);
        pointer.put(emiColor2()).put(0f);
        pointer.put(emiColor3()).put(0f);
        pointer.put(emiColor4()).put(0f);
        pointer.put(emiColor5()).put(0f);

        pointer.put(emiIntensity1());
        pointer.put(emiIntensity2());
        pointer.put(emiIntensity3());
        pointer.put(emiIntensity4());
        pointer.put(emiIntensity5());
        pointer.put(useLight());
    }

    default boolean isNotEmpty() {
        return
            this.baseColor1().equals(DEFAULT.baseColor1()) &&
            this.baseColor2().equals(DEFAULT.baseColor2()) &&
            this.baseColor3().equals(DEFAULT.baseColor3()) &&
            this.baseColor4().equals(DEFAULT.baseColor4()) &&
            this.baseColor5().equals(DEFAULT.baseColor5()) &&
            this.emiColor1().equals(DEFAULT.emiColor1()) &&
            this.emiColor2().equals(DEFAULT.emiColor2()) &&
            this.emiColor3().equals(DEFAULT.emiColor3()) &&
            this.emiColor4().equals(DEFAULT.emiColor4()) &&
            this.emiColor5().equals(DEFAULT.emiColor5()) &&
            this.emiIntensity1() == DEFAULT.emiIntensity1() &&
            this.emiIntensity2() == DEFAULT.emiIntensity2() &&
            this.emiIntensity3() == DEFAULT.emiIntensity3() &&
            this.emiIntensity4() == DEFAULT.emiIntensity4() &&
            this.emiIntensity5() == DEFAULT.emiIntensity5() &&
            this.useLight() == DEFAULT.useLight() &&
            this.disableDepth() == DEFAULT.disableDepth();
    }

    default JsonObject serialize() {
        var json = new JsonObject();
        JsonUtils.putIf(DEFAULT.baseColor1()::equals, json, "baseColor1", baseColor1(), JsonUtils::serializeColor);
        JsonUtils.putIf(DEFAULT.baseColor2()::equals, json, "baseColor2", baseColor2(), JsonUtils::serializeColor);
        JsonUtils.putIf(DEFAULT.baseColor3()::equals, json, "baseColor3", baseColor3(), JsonUtils::serializeColor);
        JsonUtils.putIf(DEFAULT.baseColor4()::equals, json, "baseColor4", baseColor4(), JsonUtils::serializeColor);
        JsonUtils.putIf(DEFAULT.baseColor5()::equals, json, "baseColor5", baseColor5(), JsonUtils::serializeColor);
        JsonUtils.putIf(DEFAULT.emiColor1()::equals, json, "emiColor1", emiColor1(), JsonUtils::serializeColor);
        JsonUtils.putIf(DEFAULT.emiColor2()::equals, json, "emiColor2", emiColor2(), JsonUtils::serializeColor);
        JsonUtils.putIf(DEFAULT.emiColor3()::equals, json, "emiColor3", emiColor3(), JsonUtils::serializeColor);
        JsonUtils.putIf(DEFAULT.emiColor4()::equals, json, "emiColor4", emiColor4(), JsonUtils::serializeColor);
        JsonUtils.putIf(DEFAULT.emiColor5()::equals, json, "emiColor5", emiColor5(), JsonUtils::serializeColor);
        JsonUtils.putIf(val -> val == DEFAULT.emiIntensity1(), json, "emiIntensity1", emiIntensity1(), JsonPrimitive::new);
        JsonUtils.putIf(val -> val == DEFAULT.emiIntensity2(), json, "emiIntensity2", emiIntensity2(), JsonPrimitive::new);
        JsonUtils.putIf(val -> val == DEFAULT.emiIntensity3(), json, "emiIntensity3", emiIntensity3(), JsonPrimitive::new);
        JsonUtils.putIf(val -> val == DEFAULT.emiIntensity4(), json, "emiIntensity4", emiIntensity4(), JsonPrimitive::new);
        JsonUtils.putIf(val -> val == DEFAULT.emiIntensity5(), json, "emiIntensity5", emiIntensity5(), JsonPrimitive::new);
        JsonUtils.putIf(val -> val == DEFAULT.useLight(), json, "useLight", useLight(), JsonPrimitive::new);
        JsonUtils.putIf(val -> val == DEFAULT.disableDepth(), json, "disableDepth", disableDepth(), JsonPrimitive::new);

        return json;
    }
}
