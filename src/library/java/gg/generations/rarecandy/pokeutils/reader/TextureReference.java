package gg.generations.rarecandy.pokeutils.reader;

import com.traneptora.jxlatte.JXLDecoder;
import com.traneptora.jxlatte.JXLOptions;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public record TextureReference(TextureDetails data, String name) {

    public static TextureReference read(byte[] imageBytes, String name, boolean isGlb) throws Exception {
        if(name.endsWith("img")) {
            var buffer = ByteBuffer.wrap(imageBytes);
            return new TextureReference(ImgUtils.read(buffer), name);
        }
        BufferedImage pixelData;
        BufferedImage temp;


        if (name.endsWith("jxl")) {
            var options = new JXLOptions();
            options.hdr = JXLOptions.HDR_OFF;
            options.threads = 2;
            temp = new JXLDecoder(new ByteArrayInputStream(imageBytes), options).decode().asBufferedImage();
        } else {

            temp = ImageIO.read(new ByteArrayInputStream(imageBytes));
        }


        var width = temp.getWidth();
        var height = temp.getHeight();

        var hasTransparent = false;

        var checkTransparency = temp.getColorModel().getTransparency() != Transparency.OPAQUE;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                var p = temp.getRGB(x, y);
                if(checkTransparency) hasTransparent = ((p>>24) & 0xff) != 255 || hasTransparent;
            }
        }

        pixelData = new BufferedImage(width, height, hasTransparent ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                var p = temp.getRGB(x, y);
                pixelData.setRGB(x, y, p);
            }
        }

        return new TextureReference(ImgUtils.save(pixelData), name);
    }

    public static ByteBuffer read(BufferedImage image) {
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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TextureReference) obj;
        return Objects.equals(this.data, that.data) &&
                Objects.equals(this.name, that.name);
    }

    @Override
    public String toString() {
        return "TextureReference[" +
                "data=" + data + ", " +
                "name=" + name + ']';
    }

    public void save(Path directory) {
        var name1 = stripFileExtension(name);

        var path = directory.resolve(name1 + ".img");


        var buffer = data().toBytes();
//        try {
//            Files.write(path, buffer.array());
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        MemoryUtil.memFree(buffer);
    }

    public static String stripFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }
}
