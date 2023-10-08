package gg.generations.rarecandy.renderer.launch;

import gg.generations.rarecandy.renderer.rendering.RareCandy;

public class Launcher {

    public static void main(String[] args) {
        var renderer = Launcher.create(MultithreadingEffectiveness.HIGH);
    }

    public static RareCandy create(MultithreadingEffectiveness coreUseCount) {
        var gl = new OpenGL();
        return null;
    }
}
