package gg.generations.rarecandy.legacy.pipeline;

import com.thebombzen.jxlatte.JXLDecoder;
import com.thebombzen.jxlatte.JXLOptions;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL45C;
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
import java.nio.file.Files;
import java.nio.file.Path;

public record Texture(int id) implements ITexture, Closeable {
    public void bind(int slot) {
        assert (slot >= 0 && slot <= 31);
        GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + slot);
        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, id);
    }

    /**
     * I could not give a single fuck at this point im doing this unpaid so i can be left alone
     */
    public static void checkError() {
//        var error = GL11C.glGetError();
//        if(error != GL11.GL_NO_ERROR) {
//            System.out.println("GL ERROR: 0x" + Integer.toHexString(error));
//        }
    }


    @Override
    public void close() {
        GL11C.glDeleteTextures(id);
    }
}
