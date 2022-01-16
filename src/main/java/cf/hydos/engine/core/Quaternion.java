package cf.hydos.engine.core;

import org.lwjgl.assimp.AIQuaternion;

public class Quaternion {
    private float m_x;
    private float m_y;
    private float m_z;
    private float m_w;

    public Quaternion(float x, float y, float z, float w) {
        this.m_x = x;
        this.m_y = y;
        this.m_z = z;
        this.m_w = w;
    }

    public float length() {
        return (float) Math.sqrt(m_x * m_x + m_y * m_y + m_z * m_z + m_w * m_w);
    }

    public Quaternion normalize() {
        float length = length();

        return new Quaternion(m_x / length, m_y / length, m_z / length, m_w / length);
    }

    public Quaternion mul(float r) {
        return new Quaternion(m_x * r, m_y * r, m_z * r, m_w * r);
    }

    public Quaternion sub(Quaternion r) {
        return new Quaternion(m_x - r.x(), m_y - r.y(), m_z - r.z(), m_w - r.w());
    }

    public Quaternion add(Quaternion r) {
        return new Quaternion(m_x + r.x(), m_y + r.y(), m_z + r.z(), m_w + r.w());
    }

    public float dot(Quaternion r) {
        return m_x * r.x() + m_y * r.y() + m_z * r.z() + m_w * r.w();
    }

    public Quaternion nlerp(Quaternion dest, float lerpFactor, boolean shortest) {
        Quaternion correctedDest = dest;

        if (shortest && this.dot(dest) < 0)
            correctedDest = new Quaternion(-dest.x(), -dest.y(), -dest.z(), -dest.w());

        return correctedDest.sub(this).mul(lerpFactor).add(this).normalize();
    }

    public Quaternion slerp(Quaternion dest, float lerpFactor, boolean shortest) {
        final float EPSILON = 1e3f;

        float cos = this.dot(dest);
        Quaternion correctedDest = dest;

        if (shortest && cos < 0) {
            cos = -cos;
            correctedDest = new Quaternion(-dest.x(), -dest.y(), -dest.z(), -dest.w());
        }

        if (Math.abs(cos) >= 1 - EPSILON)
            return nlerp(correctedDest, lerpFactor, false);

        float sin = (float) Math.sqrt(1.0f - cos * cos);
        float angle = (float) Math.atan2(sin, cos);
        float invSin = 1.0f / sin;

        float srcFactor = (float) Math.sin((1.0f - lerpFactor) * angle) * invSin;
        float destFactor = (float) Math.sin((lerpFactor) * angle) * invSin;

        return this.mul(srcFactor).add(correctedDest.mul(destFactor));
    }

    public Quaternion set(float x, float y, float z, float w) {
        this.m_x = x;
        this.m_y = y;
        this.m_z = z;
        this.m_w = w;
        return this;
    }

    public Quaternion set(Quaternion r) {
        set(r.x(), r.y(), r.z(), r.w());
        return this;
    }

    public float x() {
        return m_x;
    }

    public float y() {
        return m_y;
    }

    public float z() {
        return m_z;
    }

    public float w() {
        return m_w;
    }
}
