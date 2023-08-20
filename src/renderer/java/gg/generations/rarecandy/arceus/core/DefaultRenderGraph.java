package gg.generations.rarecandy.arceus.core;

import gg.generations.rarecandy.arceus.model.Model;
import gg.generations.rarecandy.arceus.model.RenderingInstance;
import gg.generations.rarecandy.arceus.model.SmartObject;
import gg.generations.rarecandy.arceus.model.lowlevel.VertexData;
import gg.generations.rarecandy.legacy.pipeline.ShaderProgram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11C.glDrawElements;

/**
 * simple (and probably naive) Render Graph for handling objects in a scene.
 */
public class DefaultRenderGraph {

    private final RareCandyScene scene;
    private final List<SmartObject> updatableObjects = new ArrayList<>();
    private final Map<ShaderProgram, Map<Model, Map<VertexData, List<RenderingInstance>>>> instanceMap = new HashMap<>();
    private final Map<Model, Boolean> modelHasNoInstanceVariants = new HashMap<>(); // TODO: check if the instances share the same material as the model. if so, do the rendering thing faster TM

    public DefaultRenderGraph(RareCandyScene scene) {
        this.scene = scene;
    }

    public void render() {
        if (scene.isDirty()) updateCache();
        updatableObjects.forEach(SmartObject::update);

        for (var shaderEntry : instanceMap.entrySet()) {
            var program = shaderEntry.getKey();
            program.bind();
            program.updateSharedUniforms();

            for (var modelEntry : shaderEntry.getValue().entrySet()) {
                var model = modelEntry.getKey();
                var data = model.data();
                data.bind();
                program.updateModelUniforms(model); // TODO: add this

                for (var layoutEntry : modelEntry.getValue().entrySet()) {
                    layoutEntry.getKey().bind();

                    for (var instance : layoutEntry.getValue()) {
                        program.updateInstanceUniforms(instance, model);
                        glDrawElements(data.mode.glType, data.indexCount, data.indexType.glType, 0);
                    }
                }
            }
        }
    }

    /**
     * Update the renderers storage objects, so it doesn't de-sync with the scene.
     */
    private void updateCache() {
        scene.removedInstances.forEach(this::removeInstance);
        scene.addedInstances.forEach(this::addInstance);
        scene.markClean();
    }

    private void addInstance(RenderingInstance instance) {
        if (instance instanceof SmartObject object) updatableObjects.add(object);
        instanceMap.computeIfAbsent(instance.getModel().data().layout, layout -> new HashMap<>())
                .computeIfAbsent(instance.getModel().program(), shaderProgram -> new HashMap<>())
                .computeIfAbsent(instance.getModel(), program -> new ArrayList<>())
                .add(instance);
    }

    private void removeInstance(RenderingInstance instance) {
        if (instance instanceof SmartObject) updatableObjects.remove(instance);
        instanceMap.get(instance.getModel().data().layout)
                .get(instance.getModel().program())
                .get(instance.getModel())
                .remove(instance);
    }
}
