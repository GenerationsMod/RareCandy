package com.pixelmongenerations.rarecandy.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GLModel {
    public List<MeshDrawCommand> meshDrawCommands = new ArrayList<>();
    public float vertexYRange = 0;

    public void runDrawCalls() {
        for (var drawCommand : meshDrawCommands) {
            drawCommand.run();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        var glModel = (GLModel) o;
        return Objects.equals(meshDrawCommands, glModel.meshDrawCommands);
    }

    @Override
    public int hashCode() {
        return meshDrawCommands != null ? meshDrawCommands.hashCode() : 0;
    }
}
