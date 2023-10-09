package gg.generationsmod.rarecandy.model.material;

public enum ShadingMethod {
    /**
     * Flat shading. Shading is done on per-face base, diffuse only. Also known as 'faceted shading'
     */
    FLAT,
    /**
     * Simple Gouraud shading
     */
    GOURAUD,
    /**
     * Phong-Shading
     */
    PHONG,
    /**
     * Phong-Blinn-Shading
     */
    BLINN,
    /**
     * Toon-Shading per pixel. Also known as 'comic' shader
     */
    TOON,
    /**
     * OrenNayar-Shading per pixel. Extension to standard Lambertian shading, taking the roughness of the material into account
     */
    OREN_NAYAR,
    /**
     * Minnaert-Shading per pixel. Extension to standard Lambertian shading, taking the 'darkness' of the material into account
     */
    MINNAERT,
    /**
     * CookTorrance-Shading per pixel. Special shader for metallic surfaces
     */
    COOK_TORRANCE,
    /**
     * No shading at all. Constant light influence of 1.0. Also known as "Unlit"
     */
    NO_SHADING,
    /**
     * Fresnel shading
     */
    FRESNEL,
    /**
     * Physically Based Rendering.
     */
    PBR_BRDF,
}