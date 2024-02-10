package gg.generations.rarecandy.arceus.model;

import gg.generations.rarecandy.arceus.model.lowlevel.RenderData;
import org.joml.Matrix4f;

public interface RenderingInstance {

    Model getModel();

    Material getMaterial();

    Matrix4f getTransform();
}
