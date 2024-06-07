package gg.generations.rarecandy.renderer.model;

import gg.generations.rarecandy.pokeutils.DataUtils;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GLModel implements Closeable {
    public List<MeshDrawCommand> meshDrawCommands = new ArrayList<>();

    public int vao = -1;

    public Vector3f dimensions = new Vector3f();
    public int ebo = -1;
    public int[] vbos;

    public void runDrawCalls() {
        for (var drawCommand : meshDrawCommands) {
            drawCommand.run();
        }
    }

    @Override
    public int hashCode() {
        return meshDrawCommands != null ? meshDrawCommands.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        var glModel = (GLModel) o;
        return Objects.equals(meshDrawCommands, glModel.meshDrawCommands);
    }

    @Override
    public void close() throws IOException {
        GL30.glDeleteVertexArrays(vao);
        GL30.glDeleteBuffers(ebo);

        DataUtils.deleteBuffer(vbos);
    }
}
