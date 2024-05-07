package gg.generations.rarecandy.pokeutils.reader;

import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11C.*;

public record TextureDetails(Type type, Wrap wrapS, Wrap wrapT, Filter minFilter, Filter maxFilter, short width, short height, byte[] data) {
    public ByteBuffer toBytes() {
        var palette = new ArrayList<Integer>();
        var indexedData = new int[width*height];

        for (int i = 0; i < indexedData.length; i++) {
            int d = type.getPixel(data, i);
            var index = palette.indexOf(d);

            if (index == -1) {
                index = palette.size();
                palette.add(d);
            }

            indexedData[i] = index;
        }


        System.out.println("Pallete size: " + palette.size());





//        var arryy = ByteBuffer.allocate(5 + (Short.BYTES * 2) + data.length)
//                .put((byte) type.ordinal())
//                .put((byte) wrapS.ordinal())
//                .put((byte) wrapT.ordinal())
//                .put((byte) minFilter.ordinal())
//                .put((byte) maxFilter.ordinal())
//                .putShort(width)
//                .putShort(height)
//                .put(data);



        return null;
    }

    public enum Type {
        RGBA(GL_RGBA8, GL_RGBA, 4) {
            @Override
            public void setBytes(int index, byte[] data, int pixel) {
                var i = index * getNumBytes();

                data[i + 0] =((byte) ((pixel >> 16) & 0xFF));
                data[i + 1] =((byte) ((pixel >> 8) & 0xFF));
                data[i + 2] =((byte) (pixel & 0xFF));
                data[i + 3] = ((byte) ((pixel >> 24) & 0xFF));
            }

            @Override
            public int getPixel(byte[] data, int index) {
                var i = index * getNumBytes();

                return ((data[i + 0] & 0xFF) << 16) |
                        ((data[i + 1] & 0xFF) << 8) |
                        (data[i + 2] & 0xFF) |
                        ((data[i + 3] & 0xFF) << 24);
            }

        },
        RGB(GL_RGB8, GL_RGB, 3) {
            @Override
            public void setBytes(int index, byte[] data, int pixel) {
                var i = index * getNumBytes();
                data[i + 0] = ((byte) ((pixel >> 16) & 0xFF));
                data[i + 1] = ((byte) ((pixel >> 8) & 0xFF));
                data[i + 2] = ((byte) (pixel & 0xFF));
            }

            @Override
            public int getPixel(byte[] data, int i) {
                return ((data[i] & 0xFF) << 16) | ((data[i + 1] & 0xFF) << 8) | (data[i + 2] & 0xFF);
            }

        }/*,
        GRAY*/;

        private final int texture;
        private final int pixelFormat;
        private final int numBytes;

        Type(int texture, int pixelFormat, int numBytes) {
            this.texture = texture;
            this.pixelFormat = pixelFormat;
            this.numBytes = numBytes;
        }

        public int getTexture() {
            return texture;
        }

        public int getPixelFormat() {
            return pixelFormat;
        }

        public int getNumBytes() {
            return numBytes;
        }

        abstract public void setBytes(int i, byte[] data, int pixel);

        abstract public int getPixel(byte[] data, int i);
    }

    public enum Wrap {
        GL_CLAMP(GL11.GL_CLAMP),
        GL_REPEAT(GL11.GL_REPEAT);

        private final int glValue;

        Wrap(int glValue) {

            this.glValue = glValue;
        }

        public int value() {
            return glValue;
        }
    }

    public enum Filter {
        NEAREST(GL11.GL_NEAREST),
        LINEAR(GL11.GL_LINEAR);

        private final int value;

        Filter(int value) {

            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
