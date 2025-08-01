package gg.generations.rarecandy.renderer.model;

import java.nio.ByteBuffer;

public class GenericSSBO<T> extends SSBO {

    private final Adapter<T> adapter;

    public interface Adapter<T> {
        int stride();
        void write(ByteBuffer buffer, T value);
        T read(ByteBuffer buffer);
    }

    public GenericSSBO(int bindingPoint, int initialCapacity, int usage, Adapter<T> adapter) {
        super(bindingPoint, adapter.stride(), adapter.stride() * initialCapacity, usage);
        this.adapter = adapter;
    }

    public void upload(T[] data) {
        int totalBytes = data.length * adapter.stride();
        ensureCapacity(totalBytes);
        ByteBuffer buffer = ByteBuffer.allocateDirect(totalBytes);
        for (T datum : data) adapter.write(buffer, datum);
        buffer.flip();
        upload(buffer);
    }

    public void updateSubData(int index, T value) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(adapter.stride());
        adapter.write(buffer, value);
        buffer.flip();
        updateSubData(index * adapter.stride(), buffer);
    }
}
