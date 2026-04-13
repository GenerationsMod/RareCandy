package gg.generations.rarecandy.renderer.animation;

import gg.generations.rarecandy.renderer.storage.SSBOBuffer;
import org.joml.Vector2f;

public record Transform(Vector2f scale, Vector2f offset) {
    public static final Transform DEFAULT = new Transform();
    private static final Transform TEMP = new Transform();

    public static final Vector2f DEFAULT_SCALE = new Vector2f(1f, 1f);
    public static final Vector2f DEFAULT_OFFSET = new Vector2f(0f, 0f);

    public Transform() {
        this(new Vector2f());
    }

    public Transform(Vector2f offset) {
        this(new Vector2f(1f, 1f), offset);
    }

    public boolean isUnit() {
        return offset.x == 0f && offset.y == 0f && scale.x == 1f && scale.y == 1f;
    }

    public void upload(SSBOBuffer buffer) {
        buffer.put(scale);
        buffer.put(offset);
    }

    public static Transform combine(Transform main, Transform next) {
        if(main == null && next == null) return DEFAULT;
        else if(main != null && next == null) return main;
        else if(main == null) return next;
        else {
            main.scale().mul(next.scale(), TEMP.scale());
            main.offset().mul(next.scale(), TEMP.offset()).add(next.offset(), TEMP.offset());

            return TEMP;
        }
    }
}





















