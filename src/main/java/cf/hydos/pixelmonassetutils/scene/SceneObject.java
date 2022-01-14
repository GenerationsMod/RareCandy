package cf.hydos.pixelmonassetutils.scene;

import cf.hydos.pixelmonassetutils.reader.GlbReader;
import cf.hydos.pixelmonassetutils.scene.material.Material;

public class SceneObject {

    public final String name;
    public final GlbReader.MeshData mesh;
    public final Material material;

    public SceneObject(String name, GlbReader.MeshData mesh, Material material) {
        this.name = name;
        this.mesh = mesh;
        this.material = material;
    }
}
