package gg.generations.rarecandy.pokeutils.reader;

import gg.generations.rarecandy.pokeutils.reader.TextureDetails;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.stream.Stream;

public class ImgUtils {
    public static TextureDetails read(ByteBuffer bufffer) throws Exception {

        var type = TextureDetails.Type.values()[bufffer.get()];
        var wrapS = TextureDetails.Wrap.values()[bufffer.get()];
        var wrapT = TextureDetails.Wrap.values()[bufffer.get()];
        var minFilter = TextureDetails.Filter.values()[bufffer.get()];
        var maxFilter = TextureDetails.Filter.values()[bufffer.get()];

        var width = bufffer.getShort();
        var height = bufffer.getShort();

        var size = ((int) width) * ((int) height) * type.getNumBytes();

        var data = new byte[size];

        bufffer.get(data);

        return new TextureDetails(type, wrapS, wrapT, minFilter, maxFilter, width, height, data);
    }

    public static TextureDetails save(BufferedImage image) throws Exception {
        return save(image, TextureDetails.Wrap.GL_REPEAT, TextureDetails.Wrap.GL_REPEAT, TextureDetails.Filter.NEAREST, TextureDetails.Filter.NEAREST);
    }

    public static TextureDetails save(BufferedImage image, TextureDetails.Wrap wrapS, TextureDetails.Wrap wrapT, TextureDetails.Filter minFilter, TextureDetails.Filter maxFilter) throws Exception {
        var type = switch (image.getType()) {
            case BufferedImage.TYPE_INT_RGB -> TextureDetails.Type.RGB;
            case BufferedImage.TYPE_INT_ARGB -> TextureDetails.Type.RGBA;
            default -> throw new Exception("Incompatible type");
        };

        var width = image.getWidth();
        var height = image.getHeight();

        var size = width * height;

        var data = new byte[type.getNumBytes() * size];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                var i = x + y * width;
                var pixel = image.getRGB(x, y);

                type.setBytes(i, data, pixel);
            }
        }

        return new TextureDetails(type, wrapS, wrapT, minFilter, maxFilter, (short) width, (short) height, data);
    }
}
