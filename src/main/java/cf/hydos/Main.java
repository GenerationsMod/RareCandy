package cf.hydos;

import cf.hydos.engine.AnimationUtil;
import cf.hydos.engine.core.RenderObject;
import cf.hydos.engine.core.LoopManager;
import cf.hydos.engine.core.RenderingApplication;
import cf.hydos.pixelmonassetutils.PixelAsset;
import cf.hydos.pixelmonassetutils.reader.GlbReader;
import org.joml.Vector3f;

import java.util.Objects;

public class Main extends RenderingApplication {

    public static void main(String[] args) {
        new LoopManager(960, 540, "Pixelmon: Generations .pk Renderer", new Main()).start();
    }

    @Override
    public void init() {
        addPokemon("arceus", new Vector3f(-2f, -1.6f, -1.5f), 0.05f, false);
        addPokemon("megaAlakazam", new Vector3f(-0.3f, -1.6f, -1.5f), 0.02f, false);
        addPokemonWithRotation("megaRayquaza", new Vector3f(1.9f, -0.2f, -1.5f), 0.03f);
        addPokemon("pikachu", new Vector3f(2f, -0.2f, -1.5f), 0.03f, false);
        addPokemon("mimikyu", new Vector3f(1.2f, -1.6f, -1.5f), 0.008f, false);
        addPokemon("mudkip", new Vector3f(2.2f, -1.6f, -1.5f), 0.01f, false);
    }

    private void addPokemon(String name, Vector3f pos, float scale, boolean brokenAxis) {
        PixelAsset model = new PixelAsset(Objects.requireNonNull(Main.class.getResourceAsStream("/" + name + ".pk"), "Failed to read /" + name + ".pk"));
        RenderObject pokemon = new RenderObject().addComponent(AnimationUtil.loadAnimatedFile(((GlbReader) model.reader).rawScene));
        pokemon.getTransformation().rotate((float) Math.toRadians(45), new Vector3f(0, 1, 0)).translate(pos).scale(new Vector3f(scale, scale, scale));

        if (brokenAxis) {
            pokemon.getTransformation().rotate((float) Math.toRadians(90), new Vector3f(-1, 0, 0));
        }

        add(pokemon);
    }

    private void addPokemonWithRotation(String name, Vector3f pos, float scale) {
        PixelAsset model = new PixelAsset(Objects.requireNonNull(Main.class.getResourceAsStream("/" + name + ".pk"), "Failed to read /" + name + ".pk"));
        RenderObject pokemon = new RenderObject().addComponent(AnimationUtil.loadAnimatedFile(((GlbReader) model.reader).rawScene));
        pokemon.getTransformation().rotate((float) Math.toRadians(90), new Vector3f(0, 1, 0)).translate(pos).scale(new Vector3f(scale, scale, scale));

        add(pokemon);
    }
}
