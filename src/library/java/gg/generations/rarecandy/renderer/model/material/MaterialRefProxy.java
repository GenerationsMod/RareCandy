package gg.generations.rarecandy.renderer.model.material;

import gg.generations.rarecandy.pokeutils.IMaterialReference;
import org.joml.Vector3f;

public class MaterialRefProxy {
    public static Vector3f baseColor1;
    public static Vector3f baseColor2;
    public static Vector3f baseColor3;
    public static Vector3f baseColor4;
    public static Vector3f baseColor5;
    public static Vector3f emiColor1;
    public static Vector3f emiColor2;
    public static Vector3f emiColor3;
    public static Vector3f emiColor4;
    public static Vector3f emiColor5;
    public static float emiIntensity1;
    public static float emiIntensity2;
    public static float emiIntensity3;
    public static float emiIntensity4;
    public static float emiIntensity5;
    public static boolean useLight;
    public static boolean disableDepth;

    public static void reset() {
        IMaterialReference reference = IMaterialReference.DEFAULT;
        baseColor1 = IMaterialValues.DEFAULT.baseColor1();
        baseColor2 = IMaterialValues.DEFAULT.baseColor2();
        baseColor3 = IMaterialValues.DEFAULT.baseColor3();
        baseColor4 = IMaterialValues.DEFAULT.baseColor4();
        baseColor5 = IMaterialValues.DEFAULT.baseColor5();
        emiColor1 = IMaterialValues.DEFAULT.emiColor1();
        emiColor2 = IMaterialValues.DEFAULT.emiColor2();
        emiColor3 = IMaterialValues.DEFAULT.emiColor3();
        emiColor4 = IMaterialValues.DEFAULT.emiColor4();
        emiColor5 = IMaterialValues.DEFAULT.emiColor5();
        emiIntensity1 = IMaterialValues.DEFAULT.emiIntensity1();
        emiIntensity2 = IMaterialValues.DEFAULT.emiIntensity2();
        emiIntensity3 = IMaterialValues.DEFAULT.emiIntensity3();
        emiIntensity4 = IMaterialValues.DEFAULT.emiIntensity4();
        emiIntensity5 = IMaterialValues.DEFAULT.emiIntensity5();
        useLight = IMaterialValues.DEFAULT.useLight();
        disableDepth = IMaterialValues.DEFAULT.disableDepth();
    }
}
