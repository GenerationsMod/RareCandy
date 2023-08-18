package gg.generations.pokeutils;

public record VariantReference(String material, Boolean hide) {
    public VariantReference fillIn(VariantReference filler) {
        var newMaterial = material;
        var newHide = hide;

        if (newMaterial == null) newMaterial = filler.material;
        if (newHide == null) newHide = filler.hide != null ? filler.hide : false;

        return new VariantReference(newMaterial, newHide);
    }

    public VariantReference fillIn() {
        return fillIn(this);
    }
}
