package cf.hydos;

import cf.hydos.animationRendering.engine.core.CoreEngine;

public class Main {
    public static void main(String[] args) {
        System.loadLibrary("renderdoc");

        CoreEngine engine = new CoreEngine(1280, 720, 1200, new Test());
        engine.CreateWindow("Pixelmon: Generations Test renderer");
        engine.Start();
    }
}
