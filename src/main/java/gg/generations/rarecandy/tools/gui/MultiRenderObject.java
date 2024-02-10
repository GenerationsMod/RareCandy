package gg.generations.rarecandy.tools.gui;

import gg.generations.rarecandy.arceus.core.DefaultRenderGraph;
import gg.generations.rarecandy.arceus.core.RareCandyScene;
import gg.generations.rarecandy.arceus.model.RenderingInstance;
import gg.generationsmod.rarecandy.model.Model;

import java.util.ArrayList;
import java.util.List;

public class MultiRenderObject<T extends RenderingInstance> {
    public final List<T> objects = new ArrayList<>();

    public MultiRenderObject(Model model) {

    }

    public void add(RareCandyScene<RenderingInstance> graph) {
        objects.forEach(graph::addInstance);
    }

    public void remove(RareCandyScene<RenderingInstance> graph) {
        objects.forEach(graph::removeInstance);
    }

}
