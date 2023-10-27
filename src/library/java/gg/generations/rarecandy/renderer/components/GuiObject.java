package gg.generations.rarecandy.renderer.components;

import gg.generations.rarecandy.renderer.model.material.Material;
import gg.generations.rarecandy.renderer.rendering.ObjectInstance;

import java.util.List;
import java.util.function.Predicate;

public class GuiObject extends RenderObject {

    @Override
    protected <T extends RenderObject> void render(Predicate<Material> predicate, List<ObjectInstance> instances, T object) {

    }
}
