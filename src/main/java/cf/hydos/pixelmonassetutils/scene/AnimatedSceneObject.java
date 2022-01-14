package cf.hydos.pixelmonassetutils.scene;

import cf.hydos.pixelmonassetutils.reader.GlbReader;
import cf.hydos.pixelmonassetutils.scene.material.Material;

public class AnimatedSceneObject extends SceneObject {

    public AnimatedSceneObject(String name, GlbReader.MeshData mesh, Material material) {
        super(name, mesh, material);
    }
}
