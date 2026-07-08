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
        this.variantId = -1;

        if (variant == null || canvas.loadedModel == null) return;

        this.variantId = canvas.loadedModel.variantNameToId.getOrDefault(variant, -1);
        if (variantId != -1 && canvas.loadedModelInstance != null) {
            canvas.loadedModelInstance.setVariant(variantId);
        }
    }

    public void setMesh(String mesh) {
        this.mesh = mesh;
        this.meshId = -1;

        if (mesh == null || canvas.loadedModel == null) return;

        this.meshId = canvas.loadedModel.meshNameToId.getOrDefault(mesh, -1);
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
