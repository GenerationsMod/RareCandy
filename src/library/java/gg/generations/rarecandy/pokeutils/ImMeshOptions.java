package gg.generations.rarecandy.pokeutils;

import imgui.ImGui;
import imgui.type.ImBoolean;

import java.util.ArrayList;
import java.util.List;

final class ImMeshOptions implements IMeshOptions, ImRenderable {
    private final ImBoolean invert;
    private final ImStringList aliases;

    static IMeshOptions empty() {
        return ImModelConfig.FACTORY.createMeshOptions(false, new ArrayList<>());
    }

    ImMeshOptions(boolean invert, List<String> aliases) {
        this.invert = new ImBoolean(invert);
        this.aliases = new ImStringList(aliases);
    }

    @Override
    public boolean render() {
        var dirty = ImGui.checkbox("Invert", invert);
        dirty |= aliases.render("Aliases");
        return dirty;
    }

    @Override public boolean invert() { return invert.get(); }
    @Override public List<String> aliases() { return aliases; }
}
