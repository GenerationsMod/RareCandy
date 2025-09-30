package gg.generations.rarecandy.renderer.pipeline.util;

/**
 * Defines the scope lifetime of pipeline resource bindings.
 * <ul>
 *     <li>GLOBAL — Per-frame or per-pass. Shared across all objects and materials.</li>
 *     <li>INSTANCE — Per-material. Shared across objects using the same material.</li>
 *     <li>MODEL — Per-object or per-draw. Unique for each rendered instance.</li>
 * </ul>
 */
public enum Scope {
    GLOBAL,
    INSTANCE,
    MODEL
}
