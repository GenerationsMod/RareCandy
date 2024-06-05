package gg.generations.rarecandy.pokeutils.tracs;// automatically generated by the FlatBuffers compiler, do not modify

public class TransitionT {
    private String path;
    private long hasExitTime;
    private float exitTime;
    private float duration;
    private float offset;
    private long canInterrupt;
    private ConditionT[] conditions;
    private long type;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getHasExitTime() {
        return hasExitTime;
    }

    public void setHasExitTime(long hasExitTime) {
        this.hasExitTime = hasExitTime;
    }

    public float getExitTime() {
        return exitTime;
    }

    public void setExitTime(float exitTime) {
        this.exitTime = exitTime;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public float getOffset() {
        return offset;
    }

    public void setOffset(float offset) {
        this.offset = offset;
    }

    public long getCanInterrupt() {
        return canInterrupt;
    }

    public void setCanInterrupt(long canInterrupt) {
        this.canInterrupt = canInterrupt;
    }

    public ConditionT[] getConditions() {
        return conditions;
    }

    public void setConditions(ConditionT[] conditions) {
        this.conditions = conditions;
    }

    public long getType() {
        return type;
    }

    public void setType(long type) {
        this.type = type;
    }


    public TransitionT() {
        this.path = null;
        this.hasExitTime = 0L;
        this.exitTime = 0.0f;
        this.duration = 0.0f;
        this.offset = 0.0f;
        this.canInterrupt = 0L;
        this.conditions = null;
        this.type = 0L;
    }
}

