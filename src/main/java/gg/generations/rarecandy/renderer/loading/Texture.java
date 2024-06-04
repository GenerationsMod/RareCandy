package gg.generations.rarecandy.renderer.loading;

import com.traneptora.jxlatte.JXLDecoder;
import com.traneptora.jxlatte.JXLOptions;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Texture implements ITexture {
    private static final JXLOptions options;

    static {
        options = new JXLOptions();
        options.hdr = JXLOptions.HDR_OFF;
        options.threads = 4;
    }
    private TextureDetails details;
    public int id;

    public Texture(ByteBuffer buffer, Type type, int width, int height) {
        this(new TextureDetails(buffer, type, width, height));
    }

    public Texture(TextureDetails textureDetails) {
        this.details = textureDetails;
    }

    private record TextureDetails(ByteBuffer buffer, Type type, int width, int height) implements Closeable {
        @Override
        public void close() {
            MemoryUtil.memFree(buffer());
        }

        public int init() {
            var id = GL11.glGenTextures();
            GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, id);
            GL11C.glTexImage2D(GL11C.GL_TEXTURE_2D, 0, type.internalFormat, width, height, 0, type.format, type.type, buffer);

            GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL11C.GL_REPEAT);
            GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL11C.GL_REPEAT);

            GL11C.glTexParameterf(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST);
            GL11C.glTexParameterf(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST);

            MemoryUtil.memFree(buffer);

            return id;
        }
    }

    public void bind(int slot) {
        if(details != null) {
            this.id = details.init();
            details = null;
        }

        assert (slot >= 0 && slot <= 31);
        GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + slot);
        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, this.id);
    }

    @Override
    public void close() throws IOException {
        GL11.glDeleteTextures(id);
    }

    public static Texture read(byte[] imageBytes, String name) throws IOException {
        BufferedImage pixelData;
        BufferedImage temp = name.endsWith("jxl") ? new JXLDecoder(new ByteArrayInputStream(imageBytes), options).decode().asBufferedImage() : ImageIO.read(new ByteArrayInputStream(imageBytes));

        return new Texture(readRegular(temp));

//        var width = temp.getWidth();
//        var height = temp.getHeight();
//        pixelData = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//        for (int y = 0; y < height; y++) {
//            for (int x = 0; x < width; x++) {
//                int p = temp.getRGB(x, y);
//                pixelData.setRGB(x, y, p);
//            }
//        }


    }

    public static TextureDetails readRegular(BufferedImage image) {
        if (image == null) {
            return null;
        }

        var buffer = image.getData().getDataBuffer();

        ByteBuffer readyData;

        if (buffer instanceof DataBufferFloat floatBuffer) {

            var length = floatBuffer.getSize();

            var channels = floatBuffer.getBankData().length;

            readyData = MemoryUtil.memAlloc(length * 4 * 4);
            var rawData = floatBuffer.getBankData();

            for (int i = 0; i < length; i++) {
                for (int channel = 0; channel < channels; channel++) {
                    readyData.putFloat(rawData[channel][i]);
                }
            }

            readyData.flip();

            var type = channels == 3 ? Type.RGB_FLOAT : Type.RGBA_FLOAT;

            return new TextureDetails(readyData, type, image.getWidth(), image.getHeight());
        } else if (buffer instanceof DataBufferInt floatBuffer) {
            var rawData = floatBuffer.getData();

            var isRGBA = image.getType() == BufferedImage.TYPE_INT_ARGB;

            readyData = MemoryUtil.memAlloc(rawData.length * (isRGBA ? 4 : 3));

            for (var pixel : rawData) {
                readyData.put((byte) ((pixel >> 16) & 0xFF));
                readyData.put((byte) ((pixel >> 8) & 0xFF));
                readyData.put((byte) (pixel & 0xFF));
                if(isRGBA) readyData.put((byte) ((pixel >> 24) & 0xFF));
            }

            readyData.flip();

            return new TextureDetails(readyData, isRGBA ? Type.RGBA_BYTE : Type.RGB_BYTE, image.getWidth(), image.getHeight());
        } else if (buffer instanceof DataBufferByte dataBufferByte) {
            var rawData = dataBufferByte.getData();

            var isRGBA = image.getType() == BufferedImage.TYPE_INT_ARGB;

            readyData = MemoryUtil.memAlloc(rawData.length);

            readyData.put(rawData);
            readyData.flip();

            return new TextureDetails(readyData, isRGBA ? Type.RGBA_BYTE : Type.RGB_BYTE, image.getWidth(), image.getHeight());

        } else throw new RuntimeException("Unknown Data Type: " + buffer.getClass().getName());

//        return readyData;
    }

    private static byte hdrToRgb(float hdr) {
        return (byte) Math.min(Math.max(Math.pow(hdr, 1.0 / 2.2) * 255, 0), 255);
    }

    public enum Type {
        RGBA_BYTE(GL30.GL_RGBA8, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, true, true),
        RGB_BYTE(GL30.GL_RGB8, GL30.GL_RGB, GL30.GL_UNSIGNED_BYTE, true, false),

        RGBA_FLOAT(GL30.GL_RGBA32F, GL30.GL_RGBA, GL30.GL_FLOAT, false, true),
        RGB_FLOAT(GL30.GL_RGB32F, GL30.GL_RGB, GL30.GL_FLOAT, false, false);

        private final int internalFormat;
        private final int format;
        private final int type;
        private final boolean isByte;
        private final boolean hasAlpha;

        Type(int internalFormat, int format, int type, boolean isByte, boolean hasAlpha) {
            this.internalFormat = internalFormat;
            this.format = format;
            this.type = type;
            this.isByte = isByte;
            this.hasAlpha = hasAlpha;
        }

        public int getInternalFormat() {
            return internalFormat;
        }

        public int getFormat() {
            return format;
        }

        public int getType() {
            return type;
        }

        public boolean isByte() {
            return isByte;
        }

        public boolean isHasAlpha() {
            return hasAlpha;
        }
    }
}
