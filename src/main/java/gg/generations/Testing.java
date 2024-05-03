package gg.generations;

import gg.generations.rarecandy.pokeutils.PixelAsset;
import gg.generations.rarecandy.pokeutils.reader.ITextureLoader;
import gg.generations.rarecandy.renderer.components.AnimatedMeshObject;
import gg.generations.rarecandy.renderer.components.MeshObject;
import gg.generations.rarecandy.renderer.components.MultiRenderObject;
import gg.generations.rarecandy.renderer.loading.ModelLoader;
import gg.generations.rarecandy.renderer.rendering.RareCandy;
import gg.generations.rarecandy.tools.TextureLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Testing {
    public static void main(String[] args) throws IOException {
        var render = new RareCandy();

        var path = Path.of("test");

        var fullTime = System.currentTimeMillis();

        ITextureLoader.setInstance(new TextureLoader());

        Files.newDirectoryStream(path).forEach(x -> {
            System.out.println("Rawr: " + x);

            var time = System.currentTimeMillis();


            try {
                var asset = new PixelAsset(Files.newInputStream(x), null).getImageFiles();



            } catch (Exception e) {
                System.out.println("Model: " + x.toString() + " failed." + e.getMessage());
            }
        });

        System.out.println("Total Time to load: " + ((System.currentTimeMillis() - fullTime)/1000f));
    }

    protected static <T extends MeshObject> void load(RareCandy renderer, PixelAsset is, Consumer<MultiRenderObject<T>> onFinish, Supplier<T> supplier) {
        var loader = renderer.getLoader();
        loader.createObject(
                new MultiRenderObject<>(),
                () -> is,
                supplier,
                onFinish
        );
    }

}
