package gg.generations.rarecandy.renderer.textures;

import io.github.mudbill.dds.DDSFile;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
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
    private boolean resident;

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

        ITexture.super.bind(slot);
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
        if (details != null) {
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

    /** Convenience for call sites that do not care about custom descriptors. */
    public long getLinearRepeatHandle() {
        return getSamplerHandle(SamplerPresets.TRILINEAR_REPEAT);
    }

    @Override
    public long getSamplerHandle(SamplerDesc sampler) {
        BindlessSupport.require();
        int tex = getId();
        long handle = SamplerCache.getOrCreateHandle(tex, sampler);
        resident = true;
        return handle;
    }

    @Override
    public long getImageHandle(int level, boolean layered, ITexture.ComputeAccess access) {
        BindlessSupport.require();
        int tex = getId();
        long handle = ImageHandleCache.getOrCreate(tex, level, layered, getType().internalFormat, access);
        resident = true;
        return handle;
    }

    @Override
    public void close() throws IOException {
        // Unresident any bindless handles first
        if (resident) {
            SamplerCache.unresidentForTexture(id);
            ImageHandleCache.unresidentForTexture(id);
            resident = false;
        }
        GL11.glDeleteTextures(id);
    }

    public static Texture read(byte[] imageBytes, String name) throws IOException {
        if(name.endsWith(".dds")) {
            var dds = new DDSFile(new ByteArrayInputStream(imageBytes));

            return new Texture(new  DDSTextureDetails(dds));
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

    public static ByteBuffer scaleAndProcess(byte[] bytes) {
        ByteBuffer imageBuffer = MemoryUtil.memAlloc(bytes.length).put(bytes).flip();

        IntBuffer w = MemoryUtil.memAllocInt(1);
        IntBuffer h = MemoryUtil.memAllocInt(1);
        IntBuffer c = MemoryUtil.memAllocInt(1);

        if (!STBImage.stbi_info_from_memory(imageBuffer, w, h, c)) {
            MemoryUtil.memFree(w);
            MemoryUtil.memFree(h);
            MemoryUtil.memFree(c);
            MemoryUtil.memFree(imageBuffer);
            return null;
        }

        // force decode to 4 channels (RGBA8)
        ByteBuffer decoded = STBImage.stbi_load_from_memory(imageBuffer, w, h, c, 4);
        if (decoded == null) {
            MemoryUtil.memFree(w);
            MemoryUtil.memFree(h);
            MemoryUtil.memFree(c);
            MemoryUtil.memFree(imageBuffer);
            return null;
        }

        int srcW = w.get(0);
        int srcH = h.get(0);
        int channels = 4;

//        ByteBuffer scaled = MemoryUtil.memAlloc(1024 * 1024 * 4);

        if(srcH == 1024 && srcW == 1024) {
            MemoryUtil.memFree(w);
            MemoryUtil.memFree(h);
            MemoryUtil.memFree(c);
            MemoryUtil.memFree(imageBuffer);

            return decoded;
        }

        ByteBuffer scaled = STBImageResize.stbir_resize_uint8_srgb(
                decoded, srcW, srcH, 0,
                null, 1024, 1024, 0,
                channels
        );

        STBImage.stbi_image_free(decoded);
        MemoryUtil.memFree(w);
        MemoryUtil.memFree(h);
        MemoryUtil.memFree(c);
        MemoryUtil.memFree(imageBuffer);

        if (scaled == null) {
//            MemoryUtil.memFree(scaled);
            throw new RuntimeException("stbir_resize_uint8 failed");
        }

        return scaled;
    }


}