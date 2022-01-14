package cf.hydos.pixelmonassetutils.scene.material;

public abstract class Texture {

    public final String name;

    public Texture(String name) {
        this.name = name;
    }

    public abstract byte[] getAsBytes();
}
