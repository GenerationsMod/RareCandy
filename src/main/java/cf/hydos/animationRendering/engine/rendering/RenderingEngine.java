package cf.hydos.animationRendering.engine.rendering;

import cf.hydos.animationRendering.engine.components.Camera;
import cf.hydos.animationRendering.engine.core.GameObject;
import cf.hydos.animationRendering.engine.core.Vector3f;
import cf.hydos.animationRendering.engine.rendering.resourceManagement.MappedValues;

import java.util.HashMap;

import static org.lwjgl.opengl.GL11.*;

public class RenderingEngine extends MappedValues {
    private final HashMap<String, Integer> m_samplerMap;

    private final Shader m_forwardAmbient;
    private Camera m_mainCamera;

    public RenderingEngine() {
        super();
        m_samplerMap = new HashMap<>();
        m_samplerMap.put("diffuse", 0);
        m_samplerMap.put("normalMap", 1);
        m_samplerMap.put("dispMap", 2);

        AddVector3f("ambient", new Vector3f(0.1f, 0.1f, 0.1f));

        m_forwardAmbient = new Shader("animated");

        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        glFrontFace(GL_CW);
        glCullFace(GL_FRONT);
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);

        glEnable(GL_TEXTURE_2D);
    }

    public void UpdateUniformStruct(String uniformType) {
        throw new IllegalArgumentException(uniformType + " is not a supported type in RenderingEngine");
    }

    public void Render(GameObject object) {
        if (GetMainCamera() == null)
            System.err.println("Error! Main camera not found. This is very very big bug, and game will crash.");
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        object.RenderAll(m_forwardAmbient, this);

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);
        glDepthMask(false);
        glDepthFunc(GL_EQUAL);

        glDepthFunc(GL_LESS);
        glDepthMask(true);
        glDisable(GL_BLEND);
    }


    public void AddCamera(Camera camera) {
        m_mainCamera = camera;
    }

    public int GetSamplerSlot(String samplerName) {
        return m_samplerMap.get(samplerName);
    }

    public Camera GetMainCamera() {
        return m_mainCamera;
    }
}
