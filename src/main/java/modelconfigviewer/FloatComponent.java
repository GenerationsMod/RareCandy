package modelconfigviewer;

import java.util.function.Supplier;

public class FloatComponent {
    private final String name;
    private final Supplier<Float> scale;

    public FloatComponent(String name, Supplier<Float> supplier) {
        this.name = name;
        this.scale = supplier;
    }

    @Override
    public String toString() {
        return name + ": " + scale.get();
    }
}
