package gg.generations.rarecandy.renderer.model;

import gg.generations.rarecandy.renderer.loading.Attribute;
import org.joml.Vector3f;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.List;

public interface RenderModel extends Closeable {
    void runDrawCalls();

    Vector3f getDimensions();

    interface Provider {
        RenderModel create(ByteBuffer vertexBuffer, ByteBuffer indexBuffer, int indexSize, int gltType, List<Attribute> attributes);
    }
}
