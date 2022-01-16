package cf.hydos.animationRendering.engine.components;

import cf.hydos.animationRendering.engine.core.Input;
import cf.hydos.animationRendering.engine.core.Vector3f;

public class FreeMove extends GameComponent {
    private final float m_speed;
    private final int m_forwardKey;
    private final int m_backKey;
    private final int m_leftKey;
    private final int m_rightKey;

    public FreeMove(float speed) {
        this(speed, Input.KEY_W, Input.KEY_S, Input.KEY_A, Input.KEY_D);
    }

    public FreeMove(float speed, int forwardKey, int backKey, int leftKey, int rightKey) {
        this.m_speed = speed;
        this.m_forwardKey = forwardKey;
        this.m_backKey = backKey;
        this.m_leftKey = leftKey;
        this.m_rightKey = rightKey;
    }

    @Override
    public void Input(float delta) {
        float movAmt = m_speed * delta;

        if (Input.GetKey(m_forwardKey))
            Move(GetTransform().GetRot().GetForward(), movAmt);
        if (Input.GetKey(m_backKey))
            Move(GetTransform().GetRot().GetForward(), -movAmt);
        if (Input.GetKey(m_leftKey))
            Move(GetTransform().GetRot().GetLeft(), movAmt);
        if (Input.GetKey(m_rightKey))
            Move(GetTransform().GetRot().GetRight(), movAmt);
    }

    private void Move(Vector3f dir, float amt) {
        GetTransform().SetPos(GetTransform().GetPos().Add(dir.Mul(amt)));
    }
}
