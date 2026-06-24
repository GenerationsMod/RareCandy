package gg.generations.rarecandy.pokeutils;

import imgui.ImGui;
import imgui.type.ImBoolean;

import java.util.ArrayList;
import java.util.List;

final class ImHideDuringAnimation implements IHideDuringAnimation, ImRenderable {
    private final ImBoolean blackList;
    private final ImStringList animations;

    static IHideDuringAnimation empty() {
        return ImModelConfig.FACTORY.createHideDuringAnimation(false, new ArrayList<>());
    }

    ImHideDuringAnimation(boolean blackList, List<String> animations) {
        this.blackList = new ImBoolean(blackList);
        this.animations = new ImStringList(animations);
    }

    @Override
    public boolean render() {
        var dirty = ImGui.checkbox("Blacklist", blackList);
        dirty |= animations.render("Animations");
        return dirty;
    }

    @Override public boolean blackList() { return blackList.get(); }
    @Override public List<String> animations() { return animations; }
}
