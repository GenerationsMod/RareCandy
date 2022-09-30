package com.pixelmongenerations.pkl.scene.objects;

public class Armature implements SceneObject {

    private final String name;

    public Armature(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
