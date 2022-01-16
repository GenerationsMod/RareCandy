package cf.hydos.engine.core;

import cf.hydos.engine.rendering.RenderingEngine;

public abstract class RenderingApplication {
    private RenderObject root;

    public abstract void init();

    public void Input(float delta) {
        GetRootObject().InputAll(delta);
    }

    public void Update(float delta) {
        GetRootObject().UpdateAll(delta);
    }

    public void Render(RenderingEngine renderingEngine) {
        renderingEngine.Render(GetRootObject());
    }

    public void add(RenderObject object) {
        GetRootObject().AddChild(object);
    }

    private RenderObject GetRootObject() {
        if (root == null)
            root = new RenderObject();

        return root;
    }

    public void SetEngine(Renderer engine) {
        GetRootObject().SetEngine(engine);
    }
}
