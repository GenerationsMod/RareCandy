package cf.hydos.engine.core;

import cf.hydos.engine.rendering.RenderingEngine;

public abstract class RenderingApplication {
    private RenderObject root;

    public abstract void init();

    public void update(float delta) {
        getRootObject().update(delta);
    }

    public void render(RenderingEngine renderingEngine) {
        renderingEngine.render(getRootObject());
    }

    public void add(RenderObject object) {
        getRootObject().addChild(object);
    }

    private RenderObject getRootObject() {
        if (root == null)
            root = new RenderObject();

        return root;
    }

    public void setRenderer(LoopManager engine) {
        getRootObject().SetEngine(engine);
    }
}
