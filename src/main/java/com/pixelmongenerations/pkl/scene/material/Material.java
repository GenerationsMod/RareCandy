package com.pixelmongenerations.pkl.scene.material;

import com.pixelmongenerations.pkl.reader.TextureReference;
import com.pixelmongenerations.rarecandy.loading.Texture;

public class Material {

    public final TextureReference diffuseTextureReference;
    private Texture diffuseTexture;

    public Material(TextureReference diffuseTexture) {
        this.diffuseTextureReference = diffuseTexture;
    }

    public Texture getDiffuseTexture() {
        if(diffuseTexture == null) this.diffuseTexture = new Texture(diffuseTextureReference);
        return diffuseTexture;
    }
}
