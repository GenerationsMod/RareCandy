package gg.generations.rarecandy.pokeutils.material;

import org.joml.Vector3f;
import java.util.Objects;

/**
 * Represents a single typed value in a MaterialReference's `values` map.
 */
public sealed interface MaterialValue permits 
    MaterialValue.BooleanValue,
    MaterialValue.FloatValue,
    MaterialValue.ColorValue,
    MaterialValue.StringValue {

    Object raw();

    // ─────────────────────────────────────────────
    // ✅ Boolean
    // ─────────────────────────────────────────────

    record BooleanValue(boolean value) implements MaterialValue {
        @Override public Object raw() { return value; }

        @Override public boolean equals(Object o) {
            return o instanceof BooleanValue other && value == other.value;
        }

        @Override public int hashCode() {
            return Boolean.hashCode(value);
        }
    }

    // ─────────────────────────────────────────────
    // ✅ Float (numeric)
    // ─────────────────────────────────────────────

    record FloatValue(float value) implements MaterialValue {
        @Override public Object raw() { return value; }

        @Override public boolean equals(Object o) {
            return o instanceof FloatValue other && Float.compare(value, other.value) == 0;
        }

        @Override public int hashCode() {
            return Float.hashCode(value);
        }
    }

    // ─────────────────────────────────────────────
    // ✅ Vector3f Color
    // ─────────────────────────────────────────────

    record ColorValue(Vector3f value) implements MaterialValue {
        private static final float EPSILON = 1e-6f;

        @Override public Object raw() { return value; }

        @Override public boolean equals(Object o) {
            if (!(o instanceof ColorValue other)) return false;
            Vector3f a = value, b = other.value;
            return Math.abs(a.x - b.x) < EPSILON &&
                   Math.abs(a.y - b.y) < EPSILON &&
                   Math.abs(a.z - b.z) < EPSILON;
        }

        @Override public int hashCode() {
            return Float.hashCode(value.x) ^ Float.hashCode(value.y) ^ Float.hashCode(value.z);
        }
    }

    // ─────────────────────────────────────────────
    // ✅ String
    // ─────────────────────────────────────────────

    record StringValue(String value) implements MaterialValue {
        @Override public Object raw() { return value; }

        @Override public boolean equals(Object o) {
            return o instanceof StringValue other && Objects.equals(value, other.value);
        }

        @Override public int hashCode() {
            return Objects.hashCode(value);
        }
    }
}
