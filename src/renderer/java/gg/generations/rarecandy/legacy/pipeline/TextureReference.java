package gg.generations.rarecandy.legacy.pipeline;

import com.thebombzen.jxlatte.JXLDecoder;
import com.thebombzen.jxlatte.JXLOptions;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public record TextureReference(ByteBuffer rgbaBytes, int width, int height) implements ITexture {

    @Override
    public void close() throws IOException {

    }

    @Override
    public void bind(int slot) {

    }

    public static ITexture of(BufferedImage image) {
        return read(image);
    }

    public static ITexture of(Path of) throws IOException {
        return of(readImage(of));
    }

    public static TextureReference read(BufferedImage image) {
        if (image == null) {
//            ImageUtils.warning("Could not read image from image data");
            return null;
        }

        image = process(image);

        var buffer = image.getData().getDataBuffer();

        ByteBuffer readyData;
        int height;
        int width;

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

            for (int i = 0; i < rawData.length; i += 4) {
                readyData
                        .put(rawData[i + 3])
                        .put(rawData[i + 2])
                        .put(rawData[i + 1])
                        .put(rawData[i]);
            }

            readyData.flip();
        } else throw new RuntimeException("Unknown Data Type: " + buffer.getClass().getName());

        return new TextureReference(readyData, image.getWidth(), image.getHeight());
    }

    private static int hdrToRgb(float hdr) {
        return (int) Math.min(Math.max(Math.pow(hdr, 1.0 / 2.2) * 255, 0), 255);
    }

    public Texture create() {
        var id = GL11C.glGenTextures();

        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, id);
        GL11C.glTexImage2D(GL11C.GL_TEXTURE_2D, 0, GL11C.GL_RGBA8, width, height, 0, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, rgbaBytes);
        GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL11C.GL_REPEAT);
        GL11C.glTexParameteri(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL11C.GL_REPEAT);

        GL11C.glTexParameterf(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_NEAREST);
        GL11C.glTexParameterf(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_NEAREST);

        return new Texture(id);
    }


    public static BufferedImage read(byte[] imageBytes) throws IOException {
        var options = new JXLOptions();
        options.hdr = JXLOptions.HDR_OFF;
        options.threads = 2;
        var reader = new JXLDecoder(new ByteArrayInputStream(imageBytes), options);
        var image = reader.decode();
        return image.asBufferedImage();
    }

    public static BufferedImage readImage(Path path) {
        try {
            var is = Files.newInputStream(path);

            return process(path.toString().endsWith(".jxl") ? read(is.readAllBytes()) : ImageIO.read(is));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedImage process(BufferedImage image) {
        int height = image.getHeight();
        int width = image.getWidth();

        // Mirror image if not square. TODO: maybe do this in the shader to save gpu memory and upload time in general
        if (height / width == 2) {
            var mirror = new BufferedImage(width * 2, height, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < height; y++) {
                for (int lx = 0, rx = width * 2 - 1; lx < width; lx++, rx--) {
                    int p = image.getRGB(lx, y);
                    mirror.setRGB(lx, y, p);
                    mirror.setRGB(rx, y, p);
                }
            }

            return mirror;
        } else {
            var mirror = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int p = image.getRGB(x, y);
                    mirror.setRGB(x, y, p);
                }
            }

            return mirror;
        }
    }
}
