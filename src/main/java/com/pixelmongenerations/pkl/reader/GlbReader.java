package com.pixelmongenerations.pkl.reader;

import com.pixelmongenerations.pkl.ModelNode;
import com.pixelmongenerations.pkl.scene.Scene;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.image.PixelDatas;
import de.javagl.jgltf.model.io.GltfModelReader;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class GlbReader {

    public final GltfModelReader reader = new GltfModelReader();

    public Scene read(TarFile file) throws IOException {
        var model = loadScene(file);
        return read(model);
    }

    public Scene read(InputStream file) throws IOException {
        var model = loadScene(file.readAllBytes());
        return read(model);
    }

    @NotNull
    private Scene read(GltfModel model) {
        var textures = model.getTextureModels().stream().map(raw -> new TextureReference(PixelDatas.create(raw.getImageModel().getImageData()), raw.getImageModel().getName())).toList();
        return new Scene(model, new ModelNode(model.getNodeModels()), textures);
    }

    private GltfModel loadScene(TarFile file) throws IOException {
        for (TarArchiveEntry entry : file.getEntries()) {
            if (entry.getName().endsWith(".glb")) {
                return loadScene(file.getInputStream(entry).readAllBytes());
            }
        }

        throw new RuntimeException("pk format archive contained no glb formatted files");
    }

    private GltfModel loadScene(byte[] bytes) throws IOException {
        return reader.readWithoutReferences(new ByteArrayInputStream(bytes));
    }
}
