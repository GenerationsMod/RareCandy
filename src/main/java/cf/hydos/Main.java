package cf.hydos;

import cf.hydos.engine.AnimationUtil;
import cf.hydos.engine.core.RenderObject;
import cf.hydos.engine.core.Renderer;
import cf.hydos.engine.core.RenderingApplication;
import cf.hydos.pixelmonassetutils.PixelAsset;
import cf.hydos.pixelmonassetutils.reader.GlbReader;
import org.joml.Vector3f;

import java.nio.file.Path;
import java.util.Objects;

public class Main extends RenderingApplication {

    public static void main(String[] args) {
        new Renderer((int) (480 * 1.5), (int) (270 * 1.5), 1200, new Main()).createWindow("Pixelmon: Generations .pk Renderer").start();
    }

    @Override
    public void init() {
        addPokemon("arceus", new Vector3f(-20, -8, -40), 2, false);
        addPokemon("mimikyu", new Vector3f(0, -8, -40), 0.01f, false);
        addPokemon("megaAlakazam", new Vector3f(20, -8, -40), 0.005f, false);
        addPokemon("mudkip", new Vector3f(40, -8, -40), 0.01f, false);
    }

    private void addPokemon(String name, Vector3f pos, float scale, boolean brokenAxis) {
        PixelAsset model = new PixelAsset(Objects.requireNonNull(Main.class.getResourceAsStream("/" + name + ".pk"), "Failed to read /" + name + ".pk"));
        RenderObject pokemon = new RenderObject().addComponent(AnimationUtil.loadAnimatedFile(((GlbReader) model.reader).rawScene));
        pokemon.getTransformation().scale(new Vector3f(scale, scale, scale)).translate(pos);

        if (brokenAxis) {
            pokemon.getTransformation().rotate((float) Math.toRadians(90), new Vector3f(-1, 0, 0));
        }

        add(pokemon);
    }
}
