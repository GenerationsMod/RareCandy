package cf.hydos.animationRendering.engine.core;

import cf.hydos.animationRendering.engine.rendering.RenderingEngine;

public abstract class Game {
    private GameObject m_root;

    public void Init() {
    }

    public void Input(float delta) {
        GetRootObject().InputAll(delta);
    }

    public void Update(float delta) {
        GetRootObject().UpdateAll(delta);
    }

    public void Render(RenderingEngine renderingEngine) {
        renderingEngine.Render(GetRootObject());
    }

    public void AddObject(GameObject object) {
        GetRootObject().AddChild(object);
    }

    private GameObject GetRootObject() {
        if (m_root == null)
            m_root = new GameObject();

        return m_root;
    }

    public void SetEngine(CoreEngine engine) {
        GetRootObject().SetEngine(engine);
    }
}
