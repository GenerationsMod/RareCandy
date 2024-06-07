package gg.generations.rarecandy.renderer.loading;

import com.traneptora.jxlatte.JXLDecoder;
import com.traneptora.jxlatte.JXLOptions;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
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

    public Texture(ByteBuffer buffer, int width, int height) {
        details = new TextureDetails(buffer, width, height);
    }

    private record TextureDetails(ByteBuffer buffer, int width, int height) implements Closeable {
        @Override
        public void close() {
            MemoryUtil.memFree(buffer());
        }

        public int init() {
            var id = GL11.glGenTextures();
            GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, id);
            GL11C.glTexImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA8, width, height, 0, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, buffer);

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
        BufferedImage temp;


        if (name.endsWith("jxl")) {
            temp = new JXLDecoder(new ByteArrayInputStream(imageBytes), options).decode().asBufferedImage();
        } else {

            temp = ImageIO.read(new ByteArrayInputStream(imageBytes));
        }

        var width = temp.getWidth();
        var height = temp.getHeight();
        pixelData = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p = temp.getRGB(x, y);
                pixelData.setRGB(x, y, p);
            }
        }

        return new Texture(read(pixelData), pixelData.getWidth(), pixelData.getHeight());
    }

    public static ByteBuffer read(BufferedImage image) {
        if (image == null) {
            return null;
        }

        var buffer = image.getData().getDataBuffer();

        ByteBuffer readyData;

        if (buffer instanceof DataBufferFloat intBuffer) {
            var rawData = intBuffer.getData();
            readyData = MemoryUtil.memAlloc(rawData.length * 4);

            for (var hdrChannel : rawData) {
                var channelValue = hdrToRgb(hdrChannel);
                readyData.put((byte) channelValue);
            }

            readyData.flip();
        } else if (buffer instanceof DataBufferInt floatBuffer) {
            var rawData = floatBuffer.getData();
            readyData = MemoryUtil.memAlloc(rawData.length * 4);

            for (var pixel : rawData) {
                readyData.put((byte) ((pixel >> 16) & 0xFF));
                readyData.put((byte) ((pixel >> 8) & 0xFF));
                readyData.put((byte) (pixel & 0xFF));
                readyData.put((byte) ((pixel >> 24) & 0xFF));
            }

            readyData.flip();
        } else if (buffer instanceof DataBufferByte dataBufferByte) {
            var rawData = dataBufferByte.getData();
            readyData = MemoryUtil.memAlloc(rawData.length);

            readyData.put(rawData);
            readyData.flip();
        } else throw new RuntimeException("Unknown Data Type: " + buffer.getClass().getName());

        return readyData;
    }

    private static int hdrToRgb(float hdr) {
        return (int) Math.min(Math.max(Math.pow(hdr, 1.0 / 2.2) * 255, 0), 255);
    }
}