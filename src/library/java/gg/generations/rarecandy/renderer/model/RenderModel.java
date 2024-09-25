package gg.generations.rarecandy.renderer.model;

import gg.generations.rarecandy.renderer.components.RenderObject;
import gg.generations.rarecandy.renderer.loading.Attribute;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;
import org.joml.Vector3f;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.List;

public interface RenderModel extends Closeable {
    void runDrawCalls();

    Vector3f getDimensions();

    public <T extends RenderObject> void render(List<ObjectInstance> instances, T object);

    public <T extends RenderObject> void render(ObjectInstance instance, T object);

    interface Provider {
        RenderModel create(ByteBuffer vertexBuffer, ByteBuffer indexBuffer, List<Runnable> glCalls, int indexSize, int gltType, List<Attribute> attributes);
    }
}
