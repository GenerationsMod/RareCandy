package gg.generations.rarecandy.pokeutils;

import gg.generations.rarecandy.renderer.animation.Animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModelConfig {
    public float scale = 1.0f;
    public Map<String, MaterialReference> materials;

    public Map<String, VariantDetails> defaultVariant;
    public Map<String, VariantParent> variants;
    public Map<String, HideDuringAnimation> hideDuringAnimation;

    public Map<String, Integer> animationFpsOverride;

    public Map<String, List<String>> materialsWithSameMaterialAnimation;

    public List<String> ignoreScaleInAnimation;

    public List<String> getMaterialsForAnimation(String trackName) {
        var list = new ArrayList<String>();
        list.add(trackName);

        if(materialsWithSameMaterialAnimation != null) {
            if(materialsWithSameMaterialAnimation.containsKey(trackName)) {
                list.addAll(materialsWithSameMaterialAnimation.get(trackName));
            }
        }

        return list;
    }

    public record HideDuringAnimation(boolean blackList, List<String> animations) {
        public static final HideDuringAnimation NONE = new HideDuringAnimation();

        public HideDuringAnimation() {
            this(false, null);
        }

        public boolean check(Animation animation) {
            return check(animation != null ? animation.name : null);
        }

        public boolean check(String animation) {
            if (animations != null)
                return animations.contains(animation) == blackList;
            return false;
        }
    }
}
