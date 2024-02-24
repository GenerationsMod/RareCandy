package gg.generationsmod.rarecandy;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record Pair<K, V>(
        K a,
        V b
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair<?, ?> pair)) return false;

        if (!Objects.equals(a, pair.a)) return false;
        return Objects.equals(b, pair.b);
    }

    @Override
    public int hashCode() {
        int result = a != null ? a.hashCode() : 0;
        result = 31 * result + (b != null ? b.hashCode() : 0);
        return result;
    }
}
