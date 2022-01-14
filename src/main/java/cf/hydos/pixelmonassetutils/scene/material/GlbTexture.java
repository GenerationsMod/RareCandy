package cf.hydos.pixelmonassetutils.scene.material;

import java.nio.ByteBuffer;

public class GlbTexture extends Texture {

    private final byte[] bytes;

    public GlbTexture(ByteBuffer buffer, String name) {
        super(name);
        this.bytes = new byte[buffer.remaining()];

        for (int i = 0; i < buffer.remaining(); i++) {
            this.bytes[i] = buffer.get();
        }
    }

    @Override
    public byte[] getAsBytes() {
        return bytes;
    }
}
