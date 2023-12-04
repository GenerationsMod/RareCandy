package gg.generations.rarecandy.pokeutils;

import java.util.List;
import java.util.Map;

public class ModelConfig {
    public float scale = 1.0f;
    public Map<String, MaterialReference> materials;

    public Map<String, VariantDetails> defaultVariant;
    public Map<String, VariantParent> variants;
    public Map<String, HideDuringAnimation> hideDuringAnimation;

    public Map<String, Integer> animationFpsOverride;

    public record HideDuringAnimation(boolean blackList, List<String> animations) {
        public static final HideDuringAnimation NONE = new HideDuringAnimation();

        public HideDuringAnimation() {
            this(false, null);
        }

        public boolean check(String animation) {
            if (animations != null)
                return animations.contains(animation) == blackList;
            return false;
        }
    }
}
