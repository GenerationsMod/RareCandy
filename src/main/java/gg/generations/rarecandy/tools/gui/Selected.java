package gg.generations.rarecandy.tools.gui;

public class Selected {
    private final RareCandyCanvas canvas;
    private String variant;
    private int variantId = -1;
    private String mesh;
    private int meshId = -1;

    public Selected(RareCandyCanvas canvas) {
        this.canvas = canvas;
    }


    public void setVariant(String variant) {
        this.variant = variant;
        this.variantId = canvas.loadedModel.variantNameToId.get(variant);
        canvas.loadedModelInstance.setVariant(variantId);
    }

    public void setMesh(String mesh) {
        this.mesh = mesh;

        this.meshId = canvas.loadedModel.meshNameToId.get(mesh);
    }

    public String getVariant() {
        return variant;
    }

    public int getVariantId() {
        return variantId;
    }

    public String getMesh() {
        return mesh;
    }

    public int getMeshId() {
        return meshId;
    }
}
