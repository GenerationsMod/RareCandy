package cf.hydos.engine.core;

import cf.hydos.engine.rendering.RenderingEngine;

public abstract class RenderingApplication {
    private RenderObject root;

    public abstract void init();

    public void Input(float delta) {
        GetRootObject().onInput(delta);
    }

    public void Update(float delta) {
        GetRootObject().onUpdate(delta);
    }

    public void render(RenderingEngine renderingEngine) {
        renderingEngine.Render(GetRootObject());
    }

    public void add(RenderObject object) {
        GetRootObject().addChild(object);
    }

    private RenderObject GetRootObject() {
        if (root == null)
            root = new RenderObject();

        return root;
    }

    public void setRenderer(LoopManager engine) {
        GetRootObject().SetEngine(engine);
    }
}
