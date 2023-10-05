package gg.generations.rarecandy.legacy.model.misc;

import com.thebombzen.jxlatte.JXLDecoder;
import com.thebombzen.jxlatte.JXLOptions;
import de.javagl.jgltf.model.image.PixelData;
import gg.generations.pokeutils.util.DefaultPixelData;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class ITexture {
    private static BiFunction<String, byte[], Texture> function = ((BiFunction<String, byte[], PixelData>) ITexture::read).andThen(Texture::new);

    public static void setTextureGenerator(Function<PixelData, Texture> function) {
        ITexture.function = ((BiFunction<String, byte[], PixelData>) ITexture::read).andThen(function);
    }

    public static ITexture generate(String name, byte[] data) {
        return function.apply(name, data);
    }

    public abstract void bind(int slot);

    public static PixelData read(String name, byte[] imageBytes) {
        BufferedImage pixelData;
        BufferedImage temp;

        try {
            if (name.endsWith("jxl")) {
                var options = new JXLOptions();
                options.hdr = JXLOptions.HDR_OFF;
                options.threads = 2;
                temp = new JXLDecoder(new ByteArrayInputStream(imageBytes), options).decode().asBufferedImage();
            } else {
                temp = ImageIO.read(new ByteArrayInputStream(imageBytes));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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

        return read(pixelData);
    }

    public static PixelData read(BufferedImage image) {
        if (image == null) {
//            ImageUtils.warning("Could not read image from image data");
            return null;
        }

        var buffer = image.getData().getDataBuffer();

        ByteBuffer readyData;
        int height;
        int width;

        if (buffer instanceof DataBufferFloat intBuffer) {
            var rawData = intBuffer.getData();
            readyData = MemoryUtil.memAlloc(rawData.length * 4);
            width = image.getWidth();
            height = image.getHeight();

            for (var hdrChannel : rawData) {
                var channelValue = hdrToRgb(hdrChannel);
                readyData.put((byte) channelValue);
            }

            readyData.flip();
        } else if (buffer instanceof DataBufferInt floatBuffer) {
            var rawData = floatBuffer.getData();
            readyData = MemoryUtil.memAlloc(rawData.length * 4);
            width = image.getWidth();
            height = image.getHeight();

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
            width = image.getWidth();
            height = image.getHeight();

            readyData.put(rawData);
            readyData.flip();
        } else throw new RuntimeException("Unknown Data Type: " + buffer.getClass().getName());

        return new DefaultPixelData(width, height, readyData);
    }

    private static int hdrToRgb(float hdr) {
        return (int) Math.min(Math.max(Math.pow(hdr, 1.0 / 2.2) * 255, 0), 255);
    }
}
