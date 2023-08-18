package gg.generations.rarecandy.arceus.model.lowlevel;

/**
 * "Who thought OpenGL global state was a good idea?" -hydos 18/08/2023
 */
public interface Bindable {

    void bind();

    void unbind();
}
