package gg.generations.rarecandy.tools;

import gg.generations.rarecandy.renderer.model.GenericSSBO;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.nio.ByteBuffer;

public class SSBOUtils {
    public static final GenericSSBO.Adapter<Vector3f> VECTOR3F = new GenericSSBO.Adapter<>() {
        @Override
        public int stride() {
            return Float.BYTES * 3;
        }

        @Override
        public void write(ByteBuffer buffer, Vector3f value) {
            value.get(buffer); // writes x,y,z at current position
            buffer.position(buffer.position() + stride());
        }

        @Override
        public Vector3f read(ByteBuffer buffer) {
            Vector3f vec = new Vector3f();
            vec.set(buffer); // reads x,y,z from current position
            buffer.position(buffer.position() + stride());
            return vec;
        }
    };

    public static final GenericSSBO.Adapter<Vector4f> VECTOR4F = new GenericSSBO.Adapter<>() {
        @Override
        public int stride() {
            return Float.BYTES * 4;
        }

        @Override
        public void write(ByteBuffer buffer, Vector4f value) {
            value.get(buffer); // writes x,y,z,w at current position
            buffer.position(buffer.position() + stride());
        }

        @Override
        public Vector4f read(ByteBuffer buffer) {
            Vector4f vec = new Vector4f();
            vec.set(buffer); // reads x,y,z,w from current position
            buffer.position(buffer.position() + stride());
            return vec;
        }
    };

    public static final GenericSSBO.Adapter<Matrix4f> MATRIX4F = new GenericSSBO.Adapter<>() {
        @Override
        public int stride() {
            return Float.BYTES * 16;
        }

        @Override
        public void write(ByteBuffer buffer, Matrix4f value) {
            value.get(buffer); // writes x,y,z,w at current position
            buffer.position(buffer.position() + stride());
        }

        @Override
        public Matrix4f read(ByteBuffer buffer) {
            Matrix4f vec = new Matrix4f();
            vec.set(buffer); // reads x,y,z,w from current position
            buffer.position(buffer.position() + stride());
            return vec;
        }
    };
}
