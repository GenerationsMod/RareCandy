package gg.generations.rarecandy.renderer.loading;

import io.github.mudbill.dds.DDSFile;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.system.MemoryUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBImage.stbi_info_from_memory;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

public class Texture implements ITexture {
    private TextureDetails details;
    public int id;
    private int width;
    private int height;
    private Type type;

    public Texture(TextureDetails textureDetails) {
        this.details = textureDetails;
        this.width = textureDetails.width();
        this.height = textureDetails.height();
    }

    public void bind(int slot) {
        if(details != null) {
            this.type = details.type();
            this.id = details.init();
//            try {
//                details.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
                details = null;
//            }
        }

        assert (slot >= 0 && slot <= 31);
        GL13C.glActiveTexture(GL13C.GL_TEXTURE0 + slot);
        GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, this.id);
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public int getId() {
        if(details != null) {
            this.id = details.init();
            try {
                details.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                details = null;
            }
    }

        return id;
    }

    @Override
    public ITexture.Type getType() {
        return type;
    }

    @Override
    public void close() throws IOException {
        GL11.glDeleteTextures(id);
    }

    public static Texture read(byte[] imageBytes, String name) throws IOException {
        if(name.endsWith(".dds")) {
            var dds = new DDSFile(new ByteArrayInputStream(imageBytes));

            return new Texture(new DDSTextureDetails(dds));
        } else return new Texture(read(imageBytes));
    }

    public static TextureDetails read(byte[] bytes) {
        ByteBuffer imageBuffer = MemoryUtil.memAlloc(bytes.length).put(bytes).flip();

        IntBuffer wBuffer = MemoryUtil.memAllocInt(1);
        IntBuffer hBuffer = MemoryUtil.memAllocInt(1);
        IntBuffer compBuffer = MemoryUtil.memAllocInt(1);

        // Use info to read image metadata without decoding the entire image.
        // We don't need this for this demo, just testing the API.
        if (!stbi_info_from_memory(imageBuffer, wBuffer, hBuffer, compBuffer)) {
            return null;
        }

        // Decode the image
        var image = stbi_load_from_memory(imageBuffer, wBuffer, hBuffer, compBuffer, 0);
        if (image == null) {
            return null;
        }

        var w = wBuffer.get(0);
        var h = hBuffer.get(0);
        var comp = compBuffer.get(0);

        MemoryUtil.memFree(wBuffer);
        MemoryUtil.memFree(hBuffer);
        MemoryUtil.memFree(compBuffer);
        MemoryUtil.memFree(imageBuffer);

        if (comp != 3 && comp != 4) throw new RuntimeException("Inccorect amount of color channels");


        return new TextureDetailsSTB(image, comp == 3 ? Type.RGB_BYTE : Type.RGBA_BYTE, w, h);
    }

}