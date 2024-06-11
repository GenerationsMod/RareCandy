package gg.generations.rarecandy.renderer.loading;

import com.traneptora.jxlatte.JXLDecoder;
import com.traneptora.jxlatte.JXLOptions;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBImage.stbi_info_from_memory;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

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
        TextureDetails temp;
        if (name.endsWith("jxl"))
            temp = read(new JXLDecoder(new ByteArrayInputStream(imageBytes), options).decode().asBufferedImage());
        else temp = read(imageBytes);
        return new Texture(temp);
    }

    public static TextureDetails read(byte[] bytes) {
        ByteBuffer imageBuffer = MemoryUtil.memAlloc(bytes.length).put(bytes).flip();

        IntBuffer wBuffer = MemoryUtil.memAllocInt(1);
        IntBuffer hBuffer = MemoryUtil.memAllocInt(1);
        IntBuffer compBuffer = MemoryUtil.memAllocInt(1);

        // Use info to read image metadata without decoding the entire image.
        // We don't need this for this demo, just testing the API.
        if (!stbi_info_from_memory(imageBuffer, wBuffer, hBuffer, compBuffer)) {
//               throw new RuntimeException("Failed to read image information: " + stbi_failure_reason());

            return null;
        }

        // Decode the image
        var image = stbi_load_from_memory(imageBuffer, wBuffer, hBuffer, compBuffer, 0);
        if (image == null) {

            return null;
//            throw new RuntimeException("Failed to load image: " + stbi_failure_reason());
        }

        var w = wBuffer.get(0);
        var h = hBuffer.get(0);
        var comp = compBuffer.get(0);

        MemoryUtil.memFree(wBuffer);
        MemoryUtil.memFree(hBuffer);
        MemoryUtil.memFree(compBuffer);
        MemoryUtil.memFree(imageBuffer);

        if (comp != 3 && comp != 4) throw new RuntimeException("Inccorect amount of color channels");


        return new TextureDetails(image, comp == 3 ? Type.RGB_BYTE : Type.RGBA_BYTE, w, h);
    }

    public static TextureDetails read(BufferedImage image) {
        if (image == null) {
            return null;
        }

        var buffer = image.getData().getDataBuffer();

        ByteBuffer readyData;
        Type type;

        if (buffer instanceof DataBufferFloat floatBuffer) {
            var length = floatBuffer.getSize();

            var channels = floatBuffer.getBankData().length;

            readyData = MemoryUtil.memAlloc(length * Float.BYTES * channels);
            var rawData = floatBuffer.getBankData();

            if(channels == 3) {
                for (int i = 0; i < length; i++) {
                    readyData.putFloat(rawData[0][i]);
                    readyData.putFloat(rawData[1][i]);
                    readyData.putFloat(rawData[2][i]);
                }
            } else if(channels == 4) {
                for (int i = 0; i < length; i++) {
                    readyData.putFloat(rawData[0][i]);
                    readyData.putFloat(rawData[1][i]);
                    readyData.putFloat(rawData[2][i]);
                    readyData.putFloat(rawData[3][i]);
                }
            }else {
                throw new RuntimeException("Float buffer lacks 3 or 4 banks.");
            }

            readyData.flip();

            type = channels == 3 ? Type.RGB_FLOAT : Type.RGBA_FLOAT;
        } else if (buffer instanceof DataBufferInt floatBuffer) {
            var rawData = floatBuffer.getData();
            var isRGBA = image.getType() == BufferedImage.TYPE_INT_ARGB;

            readyData = MemoryUtil.memAlloc(rawData.length * 4);

            if(isRGBA) {
                for (var pixel : rawData) {
                    readyData.put((byte) ((pixel >> 16) & 0xFF));
                    readyData.put((byte) ((pixel >> 8) & 0xFF));
                    readyData.put((byte) (pixel & 0xFF));
                    readyData.put((byte) ((pixel >> 24) & 0xFF));
                }
            } else {
                for (var pixel : rawData) {
                    readyData.put((byte) ((pixel >> 16) & 0xFF));
                    readyData.put((byte) ((pixel >> 8) & 0xFF));
                    readyData.put((byte) (pixel & 0xFF));
                }
            }

            readyData.flip();

            type = isRGBA ? Type.RGBA_BYTE : Type.RGB_BYTE;
        } else if (buffer instanceof DataBufferByte dataBufferByte) {
            var rawData = dataBufferByte.getData();
            var isRGBA = image.getType() == BufferedImage.TYPE_INT_ARGB;

            readyData = MemoryUtil.memAlloc(rawData.length);
            readyData.put(rawData);
            readyData.flip();

            type = isRGBA ? Type.RGBA_BYTE : Type.RGB_BYTE;
        } else throw new RuntimeException("Unknown Data Type: " + buffer.getClass().getName());

        return new TextureDetails(readyData, type, image.getWidth(), image.getHeight());
    }

    private static double hdrToRgb(float hdr) {
        return (int) Math.min(Math.max(Math.pow(hdr, 1.0 / 2.2) * 255, 0), 255);
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