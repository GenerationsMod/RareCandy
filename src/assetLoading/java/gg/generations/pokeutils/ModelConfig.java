package gg.generations.pokeutils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ModelConfig {
    public float scale = 1.0f;
    public Map<String, MaterialReference> materials;

    public Map<String, VariantDetails> defaultVariant;
    public Map<String, VariantParent> variants;
    public Map<String, HideDuringAnimation> hideDuringAnimation;

    public static final class MaterialReference {
        private String texture;
        private String type;

        public MaterialReference(String texture, String type) {
            this.texture = texture;
            this.type = type;
        }

        public String texture() {
            return texture;
        }

        public void setTexture(String texture) {
            this.texture = texture;
        }

        public String type() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(texture, type);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (MaterialReference) obj;
            return Objects.equals(this.texture, that.texture) &&
                   Objects.equals(this.type, that.type);
        }

        @Override
        public String toString() {
            return "MaterialReference[" +
                   "texture=" + texture + ", " +
                   "type=" + type + ']';
        }
    }

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
