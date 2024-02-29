package gg.generations.rarecandy.arceus.model.pk;

import gg.generations.rarecandy.arceus.model.lowlevel.RenderData;

import java.util.HashMap;
import java.util.List;

public record MultiModel(List<String> details, MultiRenderData renderData) {
    public record MeshDetail(int count, int offset) {}
}
