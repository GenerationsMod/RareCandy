package cf.hydos.engine.rendering;

import cf.hydos.engine.core.RenderObject;
import cf.hydos.engine.rendering.resources.MappedValues;
import org.joml.Vector3f;

import java.util.HashMap;

import static org.lwjgl.opengl.GL11.*;

public class RenderingEngine extends MappedValues {
    private final HashMap<String, Integer> samplerMap;

    private final Shader forwardAmbient;

    public RenderingEngine() {
        super();
        samplerMap = new HashMap<>();
        samplerMap.put("diffuse", 0);
        samplerMap.put("normalMap", 1);
        samplerMap.put("dispMap", 2);

        AddVector3f("ambient", new Vector3f(0.1f, 0.1f, 0.1f));

        forwardAmbient = new Shader("animated");

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

    public void Render(RenderObject object) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        object.RenderAll(forwardAmbient, this);

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);
        glDepthMask(false);
        glDepthFunc(GL_EQUAL);

        glDepthFunc(GL_LESS);
        glDepthMask(true);
        glDisable(GL_BLEND);
    }

    public int GetSamplerSlot(String samplerName) {
        return samplerMap.get(samplerName);
    }
}
