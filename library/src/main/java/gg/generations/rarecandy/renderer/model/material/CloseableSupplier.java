package gg.generations.rarecandy.renderer.model.material;

import java.io.Closeable;
import java.util.function.Supplier;

public interface CloseableSupplier<T> extends Supplier<T>, Closeable {

}
