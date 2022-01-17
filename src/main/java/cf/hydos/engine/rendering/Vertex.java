package cf.hydos.engine.rendering;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class Vertex {
    public static final int SIZE = 11;

    private Vector3f pos;
    private Vector2f texCoord;
    private Vector3f normal;
    private Vector3f tangent;

    public Vertex(Vector3f pos) {
        this(pos, new Vector2f(0, 0));
    }

    public Vertex(Vector3f pos, Vector2f texCoord) {
        this(pos, texCoord, new Vector3f(0, 0, 0));
    }

    public Vertex(Vector3f pos, Vector2f texCoord, Vector3f normal) {
        this(pos, texCoord, normal, new Vector3f(0, 0, 0));
    }

    public Vertex(Vector3f pos, Vector2f texCoord, Vector3f normal, Vector3f tangent) {
        this.pos = pos;
        this.texCoord = texCoord;
        this.normal = normal;
        this.tangent = tangent;
    }

    public Vector3f GetTangent() {
        return tangent;
    }

    public void SetTangent(Vector3f tangent) {
        this.tangent = tangent;
    }

    public Vector3f GetPos() {
        return pos;
    }

    public void SetPos(Vector3f pos) {
        this.pos = pos;
    }

    public Vector2f GetTexCoord() {
        return texCoord;
    }

    public void SetTexCoord(Vector2f texCoord) {
        this.texCoord = texCoord;
    }

    public Vector3f GetNormal() {
        return normal;
    }

    public void SetNormal(Vector3f normal) {
        this.normal = normal;
    }
}
