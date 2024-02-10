package gg.generations.rarecandy.arceus.core;

import gg.generations.rarecandy.arceus.model.Material;
import gg.generations.rarecandy.arceus.model.Model;
import gg.generations.rarecandy.arceus.model.RenderingInstance;
import gg.generations.rarecandy.arceus.model.SmartObject;
import gg.generations.rarecandy.arceus.model.lowlevel.RenderData;
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

    private final RareCandyScene<RenderingInstance> scene;
    private final List<SmartObject> updatableObjects = new ArrayList<>();
    private final Map<ShaderProgram, Map<Model, Map<Material, Map<VertexData, List<RenderingInstance>>>>> instanceMap = new HashMap<>();
    private final Map<RenderData, Boolean> modelHasNoInstanceVariants = new HashMap<>(); // TODO: check if the instances share the same material as the model. if so, do the rendering thing faster TM

    public DefaultRenderGraph(RareCandyScene<RenderingInstance> scene) {
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
                program.updateModelUniforms(model); // TODO: add this

                for (var materialEntry : modelEntry.getValue().entrySet()) {
                    var material = materialEntry.getKey();
                    program.preMaterial().accept(material);

                    program.updateMaterialUniforms(model, material);

                    for (var layoutEntry : materialEntry.getValue().entrySet()) {
                        layoutEntry.getKey().bind();
                        data.bind();

                        for (var instance : layoutEntry.getValue()) {
                            program.updateInstanceUniforms(instance, material, model);
                            glDrawElements(data.mode.glType, data.indexCount, data.indexType.glType, 0);
                        }
                    }

                    program.postMaterial().accept(material);
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
        instanceMap.computeIfAbsent(instance.getMaterial().getProgram(), layout -> new HashMap<>())
                .computeIfAbsent(instance.getModel(), shaderProgram -> new HashMap<>())
                .computeIfAbsent(instance.getMaterial(), material -> new HashMap<>())
                .computeIfAbsent(instance.getModel().data().vertexData, program -> new ArrayList<>())
                .add(instance);
    }

    private void removeInstance(RenderingInstance instance) {
        if (instance instanceof SmartObject) updatableObjects.remove(instance);
        instanceMap.get(instance.getMaterial().getProgram())
                .get(instance.getModel())
                .get(instance.getMaterial())
                .get(instance.getModel().data().vertexData)
                .remove(instance);
    }
}